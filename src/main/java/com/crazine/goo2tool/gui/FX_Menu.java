package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.gui.util.CustomAlert;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

public class FX_Menu {

    private static final MenuBar menuBar = new MenuBar();
    public static MenuBar getMenuBar() {
        return menuBar;
    }


    public static void buildMenuBar(Stage stage) {

        menuBar.prefWidthProperty().bind(stage.widthProperty());

        Menu fileMenu = new Menu("File");
        
        MenuItem install = new MenuItem("Install new addin...");
        install.setOnAction(event -> FX_Mods.installAddin());
        
        MenuItem create = new MenuItem("Package level as goo2mod...");
        // TODO
        
        MenuItem save = new MenuItem("Save");
        save.setOnAction(event -> FX_Scene.save());
        MenuItem saveAndPlay = new MenuItem("Save and Launch World of Goo 2!");
        saveAndPlay.setOnAction(event -> FX_Scene.saveAndPlay());
        
        fileMenu.getItems().addAll(install, new SeparatorMenuItem(), create, new SeparatorMenuItem(), save, saveAndPlay);
        
        Menu helpMenu = new Menu("Help");
        
        MenuItem openWebsite = new MenuItem("Open Website...");
        openWebsite.setOnAction(event -> Main_Application.openUrl("https://github.com/Darxoon/Goo2Tool"));
        
        MenuItem about = new MenuItem("About Goo2Tool");
        about.setOnAction(event -> {
            CustomAlert.show("About Goo2Tool", """
                    Goo2Tool version 1.0
                    World of Goo 2 mod loader
                    
                    Made by Crazine and Darxoon
                    """, IconLoader.getConduit(), ButtonType.OK);
        });
        
        helpMenu.getItems().addAll(openWebsite, about);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

    }


}
