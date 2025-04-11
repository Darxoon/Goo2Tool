package com.crazine.goo2tool.gui;

import java.io.File;
import java.util.Optional;

import com.crazine.goo2tool.Platform;
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
        
        String projectLocation = Main_Application.getProjectLocation();
        Image icon = new Image(projectLocation + "/conduit.png");
        
        Properties properties = PropertiesLoader.getProperties();
        
        // setup wizard
        if (properties.getBaseWorldOfGoo2Directory().isEmpty()) {
            properties.setBaseWorldOfGoo2Directory(getBaseDirectory(stage, icon));
        }
        
        if (properties.getProfileDirectory().isEmpty()) {
            properties.setProfileDirectory(getProfileDirectory(stage, icon));
        }
        
        // continue to application
        new Main_Application().start(stage);
    }
    
    private String getBaseDirectory(Stage stage, Image icon) {
        // try auto detecting
        switch (Platform.getCurrent()) {
            case WINDOWS:
                File exeFile = new File(System.getenv("PROGRAMFILES") + "/World of Goo 2/World of Goo 2.exe");
                
                if (exeFile.exists() && exeFile.isFile())
                    return exeFile.getParent();
                break;
            case MAC:
                File appFile = new File("/Applications/World of Goo 2.app");
                
                if (appFile.exists() && appFile.isDirectory())
                    return appFile.getParentFile().getAbsolutePath();
                break;
            case LINUX:
                // Linux version is an AppImage, so it could be installed anywhere
                break;
        }
        
        // show manual prompt if that didn't work
        ButtonType buttonType = new ButtonType("OK", ButtonData.OK_DONE);
        CustomAlert.show("Goo2Tool Setup", """
                Could not determine default World of Goo 2 installation.
                Please pick one yourself.
                """, icon, buttonType);
        
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
                FileChooser fileChooser = new FileChooser();
                ExtensionFilter exeFilter = new ExtensionFilter("World of Goo 2 executable", "*.AppImage");
                fileChooser.getExtensionFilters().add(exeFilter);
                
                File file = fileChooser.showOpenDialog(stage);
                yield file.getAbsolutePath();
            }
        };
    }
    
    private String getProfileDirectory(Stage stage, Image icon) {
        // try auto detecting
        File profileDir = new File(switch (Platform.getCurrent()) {
            case WINDOWS -> System.getenv("LocalAppData") + "/2DBoy/WorldOfGoo2";
            case MAC -> System.getProperty("user.home") + "/Library/Application Support/WorldOfGoo2";
            case LINUX -> null; // TODO: what is the correct directory?
        });
        
        if (profileDir.exists() && profileDir.isDirectory()) {
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
    
}
