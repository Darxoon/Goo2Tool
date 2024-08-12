package com.crazine.goo2tool.gui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

public class FX_Menu {

    private static final MenuBar menuBar = new MenuBar();
    public static MenuBar getMenuBar() {
        return menuBar;
    }


    public static void buildMenuBar(Stage stage) {

        menuBar.prefWidthProperty().bind(stage.widthProperty());

        Menu fileMenu = new Menu("File");

        menuBar.getMenus().add(fileMenu);

    }


}
