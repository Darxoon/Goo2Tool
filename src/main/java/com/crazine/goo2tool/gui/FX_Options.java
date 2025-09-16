package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.gui.options.FileOptions;
import com.crazine.goo2tool.gui.options.LaunchOptions;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FX_Options {

    private static final ScrollPane optionsView = new ScrollPane();
    public static ScrollPane getOptionsView() {
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
        optionsVBox.setPadding(new Insets(10, 10, 10, 10));
        
        optionsView.setContent(optionsVBox);
        optionsView.setHbarPolicy(ScrollBarPolicy.NEVER);
        optionsView.setFitToWidth(true);

    }


}
