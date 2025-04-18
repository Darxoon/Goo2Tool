package com.crazine.goo2tool;

import com.crazine.goo2tool.gui.FX_Setup;
import com.crazine.goo2tool.gui.Main_Application;
import com.crazine.goo2tool.properties.PropertiesLoader;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) {

        PropertiesLoader.init();

        if (!PropertiesLoader.allImportantInitialized()) {
            Application.launch(FX_Setup.class, args);
        } else {
            Application.launch(Main_Application.class, args);
        }

    }

}