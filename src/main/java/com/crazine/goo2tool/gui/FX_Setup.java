package com.crazine.goo2tool.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.functional.LocateGooDir;
import com.crazine.goo2tool.functional.LocateGooDir.GooDir;
import com.crazine.goo2tool.gui.util.CustomAlert;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.application.Application;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FX_Setup extends Application {

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
        if (!PropertiesLoader.isValidDir(properties.getBaseWorldOfGoo2Directory())) {
            properties.setBaseWorldOfGoo2Directory(getBaseDirectory(stage, IconLoader.getConduit(), located));
            
            try {
                PropertiesLoader.saveProperties();
            } catch (IOException e) {
                FX_Alarm.error(e);
                return;
            }
        }
        
        if (!PropertiesLoader.isValidDir(properties.getProfileDirectory())) {
            properties.setProfileDirectory(getProfileDirectory(stage, IconLoader.getConduit(), located));
            
            try {
                PropertiesLoader.saveProperties();
            } catch (IOException e) {
                FX_Alarm.error(e);
                return;
            }
        }
        
        // continue to application
        new Main_Application().start(stage);
    }
    
    private String getBaseDirectory(Stage stage, Image icon, Optional<GooDir> gooDir) {
        if (gooDir.isPresent()) {
            // ask if detected dir is okay
            String path = gooDir.get().path().toString();
            
            ButtonType buttonNo = new ButtonType("Pick installation manually", ButtonData.NO);
            ButtonType buttonYes = new ButtonType("Proceed", ButtonData.YES);
            
            Optional<ButtonType> result = CustomAlert.show("Goo2Tool Setup", String.format("""
                    Found World of Goo 2 installation at '%s'.
                    Would you like to proceed?
                    """, path),
                    icon, buttonNo, buttonYes);
            
            if (result.isPresent() && result.get() == buttonYes) {
                PropertiesLoader.getProperties().setSteam(gooDir.get().steamDir().isPresent());
                return path;
            }
        } else {
            // show manual prompt if Wog2 dir could not be found
            ButtonType buttonType = new ButtonType("OK", ButtonData.OK_DONE);
            
            if (Platform.getCurrent() == Platform.LINUX) {
                CustomAlert.show("Goo2Tool Setup", """
                        Could not determine default World of Goo 2 installation.
                        Please pick one yourself.
                        
                        Note: The Linux .AppImage is not supported yet.
                        Either use the Steam release or the Windows
                        DRM-free version.
                        """, icon, buttonType);
            } else {
                CustomAlert.show("Goo2Tool Setup", """
                        Could not determine default World of Goo 2 installation.
                        Please pick one yourself.
                        """, icon, buttonType);
            }
        }
        
        // TODO: this might not be true
        PropertiesLoader.getProperties().setSteam(false);
        
        return switch (Platform.getCurrent()) {
            case WINDOWS -> {
                FileChooser fileChooser = new FileChooser();
                ExtensionFilter exeFilter = new ExtensionFilter("World of Goo 2 executable", "World Of Goo 2.exe");
                fileChooser.getExtensionFilters().add(exeFilter);
                
                File file = fileChooser.showOpenDialog(stage);
                yield file.getParentFile().getAbsolutePath();
            }
            case MAC -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(stage);
                yield file.getAbsolutePath();
            }
            case LINUX -> {
                // TODO: try figuring out how to use xdg_desktop_portal for this
                FileChooser fileChooser = new FileChooser();
                ExtensionFilter exeFilter = new ExtensionFilter("World of Goo 2 executable", "*.exe", "WorldOfGoo2");
                fileChooser.getExtensionFilters().add(exeFilter);
                
                File file = fileChooser.showOpenDialog(stage);
                
                if (file.getAbsolutePath().endsWith(".exe")) {
                    yield file.getParentFile().getAbsolutePath();
                } else {
                    yield file.getAbsolutePath();
                }
            }
        };
    }
    
    private String getProfileDirectory(Stage stage, Image icon, Optional<GooDir> gooDir) {
        // try auto detecting Steam
        if (gooDir.isPresent() && PropertiesLoader.getProperties().isSteam()) {
            try {
                Optional<String> steamProfile = getSteamProfileDirectory(gooDir);
                
                if (steamProfile.isPresent())
                    return steamProfile.get();
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
        }
        
        // try auto detecting
        File profileDir = switch (Platform.getCurrent()) {
            case WINDOWS -> new File(System.getenv("LocalAppData") + "/2DBoy/WorldOfGoo2");
            case MAC -> new File(System.getProperty("user.home") + "/Library/Application Support/WorldOfGoo2");
            case LINUX -> null; // TODO: what is the correct directory?
        };
        
        if (profileDir != null && profileDir.exists() && profileDir.isDirectory()) {
            return profileDir.getAbsolutePath();
        }
        
        // show manual prompt if that didn't work
        Optional<ButtonType> result = CustomAlert.show("Goo2Tool Setup", """
                Could not determine World of Goo 2 profile folder.
                If you have launched the game before, please pick it yourself.
                """, icon, ButtonType.OK, ButtonType.CANCEL);
        
        if (result.isEmpty() || result.get().getButtonData() != ButtonData.OK_DONE)
            return "";
        
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(stage);
        return file.getAbsolutePath();
    }
    
    private Optional<String> getSteamProfileDirectory(Optional<GooDir> gooDir) throws IOException {
        Path steamDir = gooDir.get().steamDir().get();
        Path steamUserdata = steamDir.resolve("userdata");
        
        if (!Files.isDirectory(steamUserdata))
            return Optional.empty();
        
        Optional<Path> steamProfile = Files.list(steamUserdata).findFirst();
        System.out.println(steamProfile);
        
        if (steamProfile.isEmpty())
            return Optional.empty();
        
        Path savegame = steamProfile.get().resolve("3385670/remote/savegame.dat");
        
        if (Files.isRegularFile(savegame))
            return Optional.of(savegame.toString());
        else
            return Optional.empty();
    }
    
}
