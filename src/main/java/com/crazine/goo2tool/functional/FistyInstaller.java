package com.crazine.goo2tool.functional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.yamlrecords.RecordConstructor;
import org.yaml.snakeyaml.yamlrecords.RecordConstructor.RecordSubstitute;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSymbol;

public class FistyInstaller {
    
    public static record HookFile(Map<String, Hook> hooks) {}
    public static record Hook(long targetAddr, int byteLength) {}
    
    public static final String BASE_WOG2_STEAM_HASH = "0b39f56b47d6947640ca5c8fba0b91af";
    
    private static final byte[] BALLFACTORY_LOAD_PATCH = new byte[] {
        0x0F, 0x1F, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x44, 0x39, (byte) 0xf7
    };
    
    public static void installFisty() throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        
        assert properties.isSteam();
        
        // Get exe content
        Optional<byte[]> originalExe = getOriginalExe(properties);
        
        if (originalExe.isEmpty())
            return;
        
        // Load resources from classpath
        ClassLoader classLoader = FistyInstaller.class.getClassLoader();
        
        InputStream customCodeStream = classLoader.getResourceAsStream("fistyloader/custom_code.bin");
        byte[] sectionContent = customCodeStream.readAllBytes();
        
        InputStream customCodeSymbolsStream = classLoader.getResourceAsStream("fistyloader/custom_code_symbols.o");
        byte[] customCodeSymbols = customCodeSymbolsStream.readAllBytes();
        
        InputStream hooksStream = classLoader.getResourceAsStream("fistyloader/hooks.yaml");
        String hooksString = new String(hooksStream.readAllBytes(), StandardCharsets.UTF_8);
        
        // Patch section table
        ByteBuffer buffer = ByteBuffer.wrap(originalExe.get());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put(0x3c8, ".fisty".getBytes()); // section name
        buffer.putInt(0x3ec, 0xE0000000); // rwx permissions
        
        buffer.putShort(0x128_d43a, (short) 0); // i have honestly no idea what these are
        buffer.putShort(0x128_d482, (short) 0);
        buffer.putShort(0x128_d4ca, (short) 0);
        
        // Patch custom code section
        int fistySectionSize = buffer.getInt(0x3d8);
        int fistySectionOffset = buffer.getInt(0x3dc);
        
        if (sectionContent.length > fistySectionSize)
            throw new IOException("Content of .fisty section is too large, expected "
                    + fistySectionSize + " but got " + sectionContent.length);
        
        byte[] paddedSectionContent = Arrays.copyOf(sectionContent, fistySectionSize);
        buffer.put(fistySectionOffset, paddedSectionContent);
        
        // Direct patches
        writeBytes(buffer, 0x20eab5, BALLFACTORY_LOAD_PATCH);
        
        // Inject hooks
        ElfFile elfFile = ElfFile.from(customCodeSymbols);
        
        Constructor constructor = new RecordConstructor(HookFile.class);
        
        TypeDescription hookFileDesc = new TypeDescription(HookFile.class);
        hookFileDesc.addPropertyParameters("hooks", Hook.class);
        constructor.addTypeDescription(hookFileDesc);
        
        TypeDescription hookDesc = new TypeDescription(Hook.class);
        hookDesc.substituteProperty(new RecordSubstitute("target_addr", Hook.class, "targetAddr"));
        hookDesc.substituteProperty(new RecordSubstitute("byte_length", Hook.class, "byteLength"));
        constructor.addTypeDescription(hookDesc);
        
        Yaml yaml = new Yaml(constructor);
        HookFile hookFile = yaml.load(hooksString);
        
        for (Entry<String, Hook> entry : hookFile.hooks().entrySet()) {
            String hookName = entry.getKey();
            Hook hook = entry.getValue();
            
            if (hook.targetAddr < 0x1000 || hook.targetAddr >= 0x1000 + 0x85AE00) {
                throw new IOException("Hook to addr 0x" + Long.toHexString(hook.targetAddr) + " is outside of .text section");
            }
            
            ElfSymbol symbol = elfFile.getELFSymbol(hookName);
            int relativeTargetAddress = (int) (symbol.st_value - (hook.targetAddr + 5));
            
            if (hook.byteLength < 5) {
                throw new IOException("Hook must be at least 5 bytes long, not " + hook.byteLength);
            }
            
            byte[] bytes = new byte[hook.byteLength];
            bytes[0] = (byte) 0xe9;
            bytes[1] = (byte) relativeTargetAddress;
            bytes[2] = (byte) (relativeTargetAddress >> 8);
            bytes[3] = (byte) (relativeTargetAddress >> 16);
            bytes[4] = (byte) (relativeTargetAddress >> 24);
            
            writeBytes(buffer, hook.targetAddr, bytes);
        }
        
        // Write result again
        Path outPath = Path.of(properties.getBaseWorldOfGoo2Directory(), "WorldOfGoo2.exe");
        Files.write(outPath, buffer.array(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        
        properties.setFistyVersion("1.1");
    }
    
    private static void writeBytes(ByteBuffer buffer, long virtualAddress, byte[] replacement) {
        int physicalAddress = (int) (virtualAddress - 0x1000L + 0x400L);
        buffer.put(physicalAddress, replacement);
    }
    
    private static Optional<byte[]> getOriginalExe(Properties properties) throws IOException {
        Path originalExePath = Path.of(properties.getBaseWorldOfGoo2Directory(), "WorldOfGoo2.exe");
        byte[] originalExe = Files.readAllBytes(originalExePath);
        
        // Check file hash
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        
        String hash = hashFile(digest, originalExe);
        
        if (hash.equals(BASE_WOG2_STEAM_HASH)) {
            // Backup game
            Path backupPath = originalExePath.resolveSibling("WorldOfGoo2_backup.exe");
            
            Files.write(backupPath, originalExe,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        
            return Optional.of(originalExe);
        } else {
            // Try restoring game from backup
            Optional<byte[]> backupExe = getBackupExe(digest, properties);
            
            if (backupExe.isEmpty()) {
                Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setContentText(
                        "Your WorldOfGoo2.exe file is either outdated or modified.\n"
                        + "If you have installed FistyLoader previously, please restore "
                        + "the game's executable to the original version and try again.\n\n"
                        + "If your game is outdated or otherwise of a wrong version, "
                        + "please update or reinstall the game and try again.");
                
                if (IconLoader.getConduit() != null) {
                    Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    dialogStage.getIcons().add(IconLoader.getConduit());
                }
                
                dialog.show();
                return Optional.empty();
            }
            
            return backupExe;
        }
    }
    
    private static Optional<byte[]> getBackupExe(MessageDigest digest, Properties properties) throws IOException {
        Path backupExePath = Path.of(properties.getBaseWorldOfGoo2Directory(), "WorldOfGoo2_backup.exe");
        
        if (!Files.isRegularFile(backupExePath))
            return Optional.empty();
        
        byte[] backupExe = Files.readAllBytes(backupExePath);
        String hash = hashFile(digest, backupExe);
        
        if (hash.equals(BASE_WOG2_STEAM_HASH)) {
            return Optional.of(backupExe);
        } else {
            return Optional.empty();
        }
    }
    
    private static String hashFile(MessageDigest digest, byte[] fileContent) {
        byte[] hashBytes = digest.digest(fileContent);
        return HexFormat.of().formatHex(hashBytes);
    }
    
}
