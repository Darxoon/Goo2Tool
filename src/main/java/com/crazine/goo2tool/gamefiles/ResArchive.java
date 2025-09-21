package com.crazine.goo2tool.gamefiles;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.gui.util.CustomFileChooser;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public interface ResArchive extends Closeable {
    
    public static record ResFile(String path, byte[] content) {}
    
    public Optional<byte[]> getFileContent(String path) throws IOException;
    public Iterable<ResFile> getAllFiles() throws UncheckedIOException;
    public int fileCount() throws IOException;
    
    public default Optional<String> getFileText(String path) throws IOException {
        return getFileContent(path).map(content -> new String(content, StandardCharsets.UTF_8));
    }
    
    // ResArchive factory
    public static ResArchive loadOrSetupVanilla(Stage stage) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        String baseWOG2 = properties.getBaseWorldOfGoo2Directory();
        
        // Try mounting AppImage
        if (Platform.getCurrent() == Platform.LINUX) {
            Path baseWog2Path = Path.of(baseWOG2);
            
            if (baseWOG2.endsWith(".AppImage") && Files.isRegularFile(baseWog2Path)) {
                return AppImageResArchive.open(baseWog2Path);
            }
        }
        
        // Find and open res.goo file
        String resGooPath = properties.getResGooPath();
        File resGooFile;
        
        if (!resGooPath.isEmpty() && Files.exists(Path.of(resGooPath))) {
            // Try resGooFile property if it exists
            resGooFile = new File(resGooPath);
        } else if (Files.exists(Path.of(baseWOG2, "game/res.goo"))) {
            // Try base res.goo file if it exists
            Path baseFile = Path.of(baseWOG2, "game/res.goo");
            
            if (properties.isSteam()) {
                Path newFile = Path.of(baseWOG2, "game/res.goo_backup");
                Files.move(baseFile, newFile, StandardCopyOption.REPLACE_EXISTING);
                properties.setResGooPath(newFile.toString());
                resGooFile = newFile.toFile();
            } else {
                resGooFile = baseFile.toFile();
            }
        } else {
            // Prompt the user to pick a custom res.goo file
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setContentText("Could not find res.goo file as it appears to have been renamed or moved.\n\n"
                    + "Please pick the new location of the file instead.");
            alert.showAndWait();
            
            ExtensionFilter filter = new ExtensionFilter("res.goo Archive", "*.*");
            Path initialDir = Path.of(baseWOG2, "game");
            
            Path chosenFile = CustomFileChooser.chooseFile(stage,
                    "Please open res.goo archive", initialDir, filter);
            
            PropertiesLoader.getProperties().setResGooPath(chosenFile.toString());
            resGooFile = chosenFile.toFile();
        }
        
        PropertiesLoader.saveProperties();
        return new ZipResArchive(resGooFile);
    }
    
}
