package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.properties.PropertiesLoader;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FX_Setup extends Application {

    @Override
    public void start(Stage stage) {

        stage.setTitle("Goo2Tool Setup");

        VBox fileLocationsVBox = new VBox();

        Label baseWOG2Label = new Label("Base WoG2 Installation");
        baseWOG2Label.setPrefWidth(160);
        Label baseWOG2Directory = new Label(PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory());
        baseWOG2Directory.setPrefWidth(160);
        Button changeBaseWOG2DirectoryButton = new Button("...");
        changeBaseWOG2DirectoryButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World of Goo 2 executable", "World Of Goo 2.exe"));
            File baseWOG2File = fileChooser.showOpenDialog(stage).getParentFile();
            if (baseWOG2File == null) return;
            PropertiesLoader.getProperties().setBaseWorldOfGoo2Directory(baseWOG2File.getAbsolutePath());
            baseWOG2Directory.setText(PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory());
            try {
                PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory().isEmpty() &&
                    !PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory().isEmpty() &&
                    !PropertiesLoader.getProperties().getProfileDirectory().isEmpty()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });
        HBox baseWOG2 = new HBox(baseWOG2Label, baseWOG2Directory, changeBaseWOG2DirectoryButton);

        Label customWOG2Label = new Label("Custom WoG2 Installation");
        customWOG2Label.setPrefWidth(160);
        Label customWOG2Directory = new Label(PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory());
        customWOG2Directory.setPrefWidth(160);
        Button changeCustomWOG2DirectoryButton = new Button("...");
        changeCustomWOG2DirectoryButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World of Goo 2 executable", "World Of Goo 2.exe"));
            File customWOG2File = fileChooser.showOpenDialog(stage).getParentFile();
            PropertiesLoader.getProperties().setCustomWorldOfGoo2Directory(customWOG2File.getAbsolutePath());
            customWOG2Directory.setText(PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory());
            try {
                PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory().isEmpty() &&
                    !PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory().isEmpty() &&
                    !PropertiesLoader.getProperties().getProfileDirectory().isEmpty()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });
        HBox customWOG2 = new HBox(customWOG2Label, customWOG2Directory, changeCustomWOG2DirectoryButton);

        Label profileLabel = new Label("Profile");
        profileLabel.setPrefWidth(160);
        Label profileDirectory = new Label(PropertiesLoader.getProperties().getProfileDirectory());
        profileDirectory.setPrefWidth(160);
        Button changeProfileDirectoryButton = new Button("...");
        changeProfileDirectoryButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World of Goo 2 save file", "wog2_1.dat"));
            File profileFile = fileChooser.showOpenDialog(stage).getParentFile();
            PropertiesLoader.getProperties().setProfileDirectory(profileFile.getAbsolutePath());
            profileDirectory.setText(PropertiesLoader.getProperties().getProfileDirectory());
            try {
                PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory().isEmpty() &&
                    !PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory().isEmpty() &&
                    !PropertiesLoader.getProperties().getProfileDirectory().isEmpty()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });
        HBox profile = new HBox(profileLabel, profileDirectory, changeProfileDirectoryButton);

        fileLocationsVBox.getChildren().addAll(baseWOG2, customWOG2, profile);

        fileLocationsVBox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(fileLocationsVBox);

        stage.setScene(scene);
        stage.show();

    }

}
