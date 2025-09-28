package com.crazine.goo2tool.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.VersionNumber;
import com.crazine.goo2tool.functional.FistyInstaller;
import com.crazine.goo2tool.functional.LocateGooDir;
import com.crazine.goo2tool.functional.LocateGooDir.GooDir;
import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.gui.util.CustomFileChooser;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.application.Application;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FX_Setup extends Application {

    private static Logger logger = LoggerFactory.getLogger(FX_Setup.class);
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Goo2Tool Setup");
        
        try {
            IconLoader.init();
        } catch (IOException e) {
            FX_Alarm.error(e);
        }
        
        Properties properties = PropertiesLoader.getProperties();
        
        // try auto detecting wog2 dir
        Optional<GooDir> located = LocateGooDir.locateWog2();
        
        // setup wizard
        if (!PropertiesLoader.isValidBaseWog2(properties.getBaseWorldOfGoo2Directory())) {
            try {
                properties.setBaseWorldOfGoo2Directory(getBaseDirectory(stage, IconLoader.getConduit(), located));
                PropertiesLoader.saveProperties();
            } catch (IOException e) {
                FX_Alarm.error(e);
                return;
            }
        }
        
        if (!PropertiesLoader.isValidDir(properties.getProfileDirectory())) {
            try {
                properties.setProfileDirectory(getProfileDirectory(stage, IconLoader.getConduit(), located));
                PropertiesLoader.saveProperties();
            } catch (IOException e) {
                FX_Alarm.error(e);
                return;
            }
        }
        
        if (!PropertiesLoader.isValidDir(properties.getSaveFilePath())) {
            try {
                properties.setSaveFilePath(getSaveFilePath(stage, IconLoader.getConduit(), located));
                PropertiesLoader.saveProperties();
            } catch (IOException e) {
                FX_Alarm.error(e);
                return;
            }
        }
        
        // continue to application
        new Main_Application().start(stage);
    }
    
    private String getBaseDirectory(Stage stage, Image icon, Optional<GooDir> gooDir) throws IOException {
        if (gooDir.isPresent()) {
            // ask if detected dir is okay
            String path = gooDir.get().path().toString();
            
            ButtonType buttonNo = new ButtonType("Pick installation manually", ButtonData.NO);
            ButtonType buttonYes = new ButtonType("Proceed", ButtonData.YES);
            
            Optional<ButtonType> result = FX_Alert.info("Goo2Tool Setup", String.format("""
                    Found World of Goo 2 installation at
                    '%s'.
                    Would you like to proceed?
                    """, path),
                    icon, buttonNo, buttonYes);
            
            if (result.isPresent() && result.get() == buttonYes) {
                boolean isSteam = gooDir.get().steamDir().isPresent();
                PropertiesLoader.getProperties().setSteam(isSteam);
                
                if (Platform.getCurrent() == Platform.LINUX && isSteam) {
                    boolean isProton = Files.isRegularFile(Paths.get(path, "WorldOfGoo2.exe"));
                    PropertiesLoader.getProperties().setProton(isProton);
                } else {
                    PropertiesLoader.getProperties().setProton(false);
                }
                
                detectFistyVersion(path);
                
                return path;
            }
        } else {
            // show manual prompt if Wog2 dir could not be found
            ButtonType buttonType = new ButtonType("OK", ButtonData.OK_DONE);
            
            if (Platform.getCurrent() == Platform.LINUX) {
                FX_Alert.info("Goo2Tool Setup", """
                        Could not determine default World of Goo 2 installation.
                        Please pick one yourself.
                        
                        Note: The Linux .AppImage is not supported yet.
                        Either use the Steam release or the Windows
                        DRM-free version.
                        """, icon, buttonType);
            } else {
                FX_Alert.info("Goo2Tool Setup", """
                        Could not determine default World of Goo 2 installation.
                        Please pick one yourself.
                        """, icon, buttonType);
            }
        }
        
        PropertiesLoader.getProperties().setSteam(false);
        PropertiesLoader.getProperties().setProton(false);
        PropertiesLoader.getProperties().setFistyVersion(null);
        
        return switch (Platform.getCurrent()) {
            case WINDOWS -> {
                ExtensionFilter exeFilter = new ExtensionFilter("World of Goo 2 executable",
                        "World Of Goo 2.exe", "WorldOfGoo2.exe");
                Optional<Path> file = CustomFileChooser.openFile(stage, "Please choose World of Goo 2 installation", exeFilter);
                
                if (file.isEmpty()) {
                    FX_Alert.info("Goo2Tool Setup",
                            "No World of Goo 2 installation has been chosen. Exiting.",
                            ButtonType.OK);
                    
                    System.exit(0);
                }
                
                // Steam version's executable does not contain spaces, standalone version does
                if (file.get().endsWith("WorldOfGoo2.exe")) {
                    PropertiesLoader.getProperties().setSteam(true);
                    detectFistyVersion(file.get().getParent().toString());
                }
                
                yield file.get().getParent().toString();
            }
            case MAC -> {
                Optional<Path> file = CustomFileChooser.chooseDirectory(stage, "Please choose World of Goo 2 installation");
                
                if (file.isEmpty()) {
                    FX_Alert.info("Goo2Tool Setup",
                            "No World of Goo 2 installation has been chosen. Exiting.",
                            ButtonType.OK);
                    
                    System.exit(0);
                }
                
                yield file.get().toString();
            }
            case LINUX -> {
                ExtensionFilter exeFilter = new ExtensionFilter("World of Goo 2 executable (WorldOfGoo2, *.exe, *.AppImage)",
                        "*.exe", "WorldOfGoo2", "*.AppImage");
                
                Optional<Path> file = CustomFileChooser.openFile(stage, "Please choose World of Goo 2 installation", exeFilter);
                
                if (file.isEmpty()) {
                    FX_Alert.info("Goo2Tool Setup",
                            "No World of Goo 2 installation has been chosen. Exiting.",
                            ButtonType.OK);
                    
                    System.exit(0);
                }
                
                if (file.get().endsWith("WorldOfGoo2.exe")) {
                    PropertiesLoader.getProperties().setSteam(true);
                    PropertiesLoader.getProperties().setProton(true);
                    
                    detectFistyVersion(file.get().getParent().toString());
                }
                // TODO: detect native Steam version
                
                String fileString = file.get().toString();
                
                if (fileString.endsWith(".exe")) {
                    yield file.get().getParent().toString();
                } else {
                    yield fileString;
                }
            }
        };
    }
    
    public static void detectFistyVersion(String baseWog2) {
        Properties properties = PropertiesLoader.getProperties();
        properties.setFistyVersion(null);
        
        // Make sure platform is supported by fisty
        switch (Platform.getCurrent()) {
            case WINDOWS:
                if (!properties.isSteam())
                    return;
                break;
            case MAC:
                // FistyLoader is not supported on mac
                properties.setFistyVersion(null);
                return;
            case LINUX:
                if (!properties.isSteam() || !properties.isProton())
                    return;
                break;
        }
        
        Path exePath = Paths.get(baseWog2, "WorldOfGoo2.exe");
        
        if (!Files.isRegularFile(exePath))
            return;
        
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            FX_Alarm.error(e);
            return;
        }
        
        VersionNumber fistyVersion;
        try {
            byte[] exeContent = Files.readAllBytes(exePath);
            String exeHash = hashFile(digest, exeContent);
            
            fistyVersion = FistyInstaller.FISTY_WOG2_STEAM_HASHES.get(exeHash);
        } catch (IOException e) {
            FX_Alarm.error(e);
            return;
        }
        
        properties.setFistyVersion(fistyVersion);
        
        if (fistyVersion != null) {
            FX_Alert.info("Goo2Tool setup",
                    "Detected that FistyLoader " + fistyVersion + " is installed",
                    ButtonType.OK);
        }
    }
    
    private static String hashFile(MessageDigest digest, byte[] fileContent) {
        byte[] hashBytes = digest.digest(fileContent);
        return HexFormat.of().formatHex(hashBytes);
    }
    
    private static final String STEAM_WINEPFX_PROFILE_DIR =
        "steamapps/compatdata/3385670/pfx/drive_c/users/steamuser/AppData/Local/2DBoy/WorldOfGoo2";
    
    public static String getProfileDirectory(Stage stage, Image icon, Optional<GooDir> gooDir) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        
        // try auto detecting
        Path profileDir = switch (Platform.getCurrent()) {
            case WINDOWS -> Paths.get(System.getenv("LocalAppData"), "2DBoy/WorldOfGoo2");
            case MAC -> Paths.get(System.getProperty("user.home"), "Library/Application Support/WorldOfGoo2");
            case LINUX -> {
                // try looking in wineprefix
                if (properties.isSteam() && properties.isProton() && gooDir.isPresent()) {
                    if (gooDir.get().steamDir().isEmpty())
                        throw new RuntimeException("Could not find Steam directory");
                    
                    Path steamDir = gooDir.get().steamDir().get();
                    Path steamProfileDir = steamDir.resolve(STEAM_WINEPFX_PROFILE_DIR);
                    
                    if (Files.isDirectory(steamProfileDir))
                        yield steamProfileDir;
                }
                
                yield Paths.get(System.getProperty("user.home"), ".config/WorldOfGoo2");
            }
        };
        
        // show manual prompt if that didn't work
        if (profileDir == null || !Files.isDirectory(profileDir)) {
            Optional<ButtonType> result = FX_Alert.info("Goo2Tool Setup", """
                    Could not determine World of Goo 2 profile folder.
                    If you have launched the game before, please pick it yourself.
                    """, icon, ButtonType.OK, ButtonType.CANCEL);
            
            if (result.isEmpty() || result.get().getButtonData() != ButtonData.OK_DONE)
                return "";
            
            Optional<Path> file = CustomFileChooser.chooseDirectory(stage, "Please choose World of Goo 2 profile");
            
            if (file.isEmpty())
                return "";
            
            return file.get().toString();
        }
        
        if (properties.isSteam() && gooDir.isPresent()) {
            // find steam user profile dir
            Optional<Path> steamProfileDir = Files.list(profileDir)
                .filter(FX_Setup::isSteamProfileDir)
                .findFirst();
            
            if (steamProfileDir.isPresent())
                return steamProfileDir.get().toString();
            else
                return profileDir.toString();
        } else {
            return profileDir.toString();
        }
        
    }
    
    private static boolean isSteamProfileDir(Path child) {
        if (!Files.isDirectory(child))
            return false;
        
        String fileName = child.getFileName().toString();
        
        return !fileName.equals("sentry")
            && !fileName.equals("tmp")
            && !fileName.equals("levels");
    }
    
    public static String getSaveFilePath(Stage stage, Image icon, Optional<GooDir> gooDir) throws IOException {
        if (gooDir.isPresent() && PropertiesLoader.getProperties().isSteam()) {
            Optional<String> steamProfile = getSteamProfileDirectory(gooDir);
            
            if (!steamProfile.isPresent()) {
                Path steamDir = gooDir.get().steamDir().get();
                throw new IOException("No Steam save file found in " + steamDir.resolve("userdata"));
            }
            
            return steamProfile.get();
        } else {
            Path saveFilePath = Paths.get(PropertiesLoader.getProperties().getProfileDirectory(), "wog2_1.dat");
            return saveFilePath.toString();
        }
    }
    
    private static Optional<String> getSteamProfileDirectory(Optional<GooDir> gooDir) throws IOException {
        Path steamDir = gooDir.get().steamDir().get();
        Path steamUserdata = steamDir.resolve("userdata");
        
        if (!Files.isDirectory(steamUserdata))
            return Optional.empty();
        
        Optional<Path> steamProfile = Files.list(steamUserdata).findFirst();
        logger.debug("steamProfile {}", steamProfile);
        
        if (steamProfile.isEmpty())
            return Optional.empty();
        
        Path savegame = steamProfile.get().resolve("3385670/remote/savegame.dat");
        
        if (Files.isRegularFile(savegame))
            return Optional.of(savegame.toString());
        else
            return Optional.empty();
    }
    
}
