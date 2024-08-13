package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.properties.Addin;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.saveFile.SaveFileLoader;
import com.crazine.goo2tool.saveFile.WOG2SaveData;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
        File codeLocation = new File(getClass().getProtectionDomain().getCodeSource().toString().substring(7));
        while (!codeLocation.getPath().substring(codeLocation.getPath().lastIndexOf("\\") + 1).equals("Goo2Tool")
        && !codeLocation.getPath().substring(codeLocation.getPath().lastIndexOf("\\") + 1).equals("Goo2Tool-master")) {
            codeLocation = codeLocation.getParentFile();
        }
        String projectLocation = codeLocation.getPath();
        Image image = new Image(projectLocation + "\\conduit.png");
        stage.getIcons().add(image);
        stage.show();


        try {

            File goomodsFile = new File(PropertiesLoader.getGoo2ToolPath() + "\\addins");
            if (!Files.exists(goomodsFile.toPath())) Files.createDirectory(goomodsFile.toPath());
            File[] children = goomodsFile.listFiles();
            if (children != null) for (File goomodFile : children) {

                Goo2mod goo2mod = AddinFileLoader.loadGoo2mod(goomodFile);
                if (goo2mod != null) {
                    Addin addin2 = new Addin();
                    addin2.setName(goo2mod.getId());
                    addin2.setLoaded(false);
                    if (PropertiesLoader.getProperties().getAddins().stream().noneMatch(addin -> addin.getName().equals(addin2.getName()))) {
                        PropertiesLoader.getProperties().getAddins().add(addin2);
                    }
                    FX_Mods.getModTableView().getItems().add(goo2mod);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
