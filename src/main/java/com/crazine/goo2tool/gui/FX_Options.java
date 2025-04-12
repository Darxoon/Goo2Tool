package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.gui.util.FileOptions;
import com.crazine.goo2tool.gui.util.LaunchOptions;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FX_Options {

    private static final VBox optionsView = new VBox();
    public static VBox getOptionsView() {
        return optionsView;
    }


    public static void buildOptionsView(Stage stage) {

        GridPane fileLocations = new FileOptions(stage, false).getContents();
        
        TitledPane fileLocationsPane = new TitledPane("Places", fileLocations);
        fileLocationsPane.setCollapsible(false);
        fileLocationsPane.setMaxWidth(1000);
        
        GridPane launchOptions = new LaunchOptions(PropertiesLoader.getProperties()).getContents();
        
        TitledPane launchOptionsPane = new TitledPane("Launch Options", launchOptions);
        launchOptionsPane.setCollapsible(false);
        launchOptionsPane.setMaxWidth(600);
        
        VBox optionsVBox = new VBox(fileLocationsPane, launchOptionsPane);
        optionsVBox.setSpacing(10);
        
        optionsView.getChildren().add(optionsVBox);

        optionsView.setPadding(new Insets(10, 10, 10, 10));

    }


}
