package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.PropertiesLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class Main_Application extends Application {

    private static Main_Application mainApplication;
    
    @Override
    public void start(Stage stage) {
        mainApplication = this;
        
        try {
            IconLoader.init();
        } catch (IOException e) {
            FX_Alarm.error(e);
        }
        
        FX_Scene.buildScene(stage);
        FX_Menu.buildMenuBar(stage);
        FX_Profile.buildProfileView(stage);
        FX_Mods.buildModView(stage);
        FX_Options.buildOptionsView(stage);

        stage.setWidth(960);
        stage.setHeight(540);

        Scene scene = FX_Scene.getScene();
        stage.setTitle("World of Goo 2 Tool");
        stage.setScene(scene);
        stage.setMinWidth(530);
        stage.setMinHeight(300);
        
        if (IconLoader.getConduit() != null)
            stage.getIcons().add(IconLoader.getConduit());
        

        File goomodsFile = new File(PropertiesLoader.getGoo2ToolPath() + "/addins");
        try {
            Files.createDirectory(goomodsFile.toPath());
        } catch (FileAlreadyExistsException e) {
            // that's ok, just ignore
        } catch (IOException e) {
            // if it can't create the addins folder, then all of the other
            // mod management likely won't work either, so just abort
            FX_Alarm.error(e);
            return;
        }
        
        File[] children = goomodsFile.listFiles();
        if (children != null) {
            for (File goomodFile : children) {

                Goo2mod goo2mod;
                try {
                    goo2mod = AddinFileLoader.loadGoo2mod(goomodFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
                    dialog.setContentText("Failed loading the mod \"" + goomodFile.getName() + "\":\n\n" + e.toString());
                    dialog.show();
                    continue;
                }
                
                if (goo2mod != null) {
                    boolean modNotInConfig = PropertiesLoader.getProperties().getAddins().stream()
                            .noneMatch(addin -> addin.getId().equals(goo2mod.getId()));
                    if (modNotInConfig) {
                        AddinConfigEntry addin2 = new AddinConfigEntry();
                        addin2.setId(goo2mod.getId());
                        addin2.setLoaded(false);
                        PropertiesLoader.getProperties().getAddins().add(addin2);
                    }
                    
                    FX_Mods.getModTableView().getItems().add(goo2mod);
                }
    
            }
        }
        
        stage.show();

    }
    
    public static void openUrl(String url) {
        mainApplication.getHostServices().showDocument(url);
    }
    
}
