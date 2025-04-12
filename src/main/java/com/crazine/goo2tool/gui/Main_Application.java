package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.Main;
import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.PropertiesLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class Main_Application extends Application {

    @Override
    public void start(Stage stage) {
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
        String projectLocation = getProjectLocation();
        InputStream iconStream;
        try {
            iconStream = new FileInputStream(projectLocation + "/conduit.png");
        } catch (FileNotFoundException e) {
            FX_Alarm.error(e);
            return;
        }
        Image icon = new Image(iconStream);
        stage.getIcons().add(icon);
        stage.show();


        try {

            File goomodsFile = new File(PropertiesLoader.getGoo2ToolPath() + "/addins");
            if (!Files.exists(goomodsFile.toPath())) Files.createDirectory(goomodsFile.toPath());
            File[] children = goomodsFile.listFiles();
            if (children != null) for (File goomodFile : children) {

                Goo2mod goo2mod = AddinFileLoader.loadGoo2mod(goomodFile);
                if (goo2mod != null) {
                    AddinConfigEntry addin2 = new AddinConfigEntry();
                    addin2.setName(goo2mod.getId());
                    addin2.setLoaded(false);
                    if (PropertiesLoader.getProperties().getAddins().stream().noneMatch(addin -> addin.getName().equals(addin2.getName()))) {
                        PropertiesLoader.getProperties().getAddins().add(addin2);
                    }
                    FX_Mods.getModTableView().getItems().add(goo2mod);
                }

            }

        } catch (IOException e) {
            FX_Alarm.error(e);
        }

    }
    
    public static String getProjectLocation() {
        try {
            File codeLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            while (!codeLocation.getName().equals("Goo2Tool") && !codeLocation.getName().equals("Goo2Tool-master")) {
                codeLocation = codeLocation.getParentFile();
            }
            return codeLocation.getPath().replaceAll("\\\\", "/");
        } catch (URISyntaxException e) {
            FX_Alarm.error(e);
            return "";
        }
    }

}
