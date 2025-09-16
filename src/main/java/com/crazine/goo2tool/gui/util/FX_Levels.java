package com.crazine.goo2tool.gui.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FX_Levels {
    
    public static void show(Stage originalStage) {
        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setWidth(400);
        stage.setHeight(400);

        stage.setTitle("Package level as .goo2mod");

        stage.getIcons().add(IconLoader.getTerrain());

        stage.setAlwaysOnTop(true);
        
        // collect all levels
        Path profileDir = Path.of(PropertiesLoader.getProperties().getProfileDirectory());
        List<String> customLevels;
        try {
            
            customLevels = Files.list(profileDir.resolve("levels"))
                .map(path -> getLevelName(path))
                .toList();
            
        } catch (IOException e) {
            FX_Alarm.error(e);
            return;
        } catch (UncheckedIOException e) {
            FX_Alarm.error(e.getCause());
            return;
        }
        
        // create UI
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setTop(new Label("Select level to package:"));
        
        ObservableList<String> levels = FXCollections.observableArrayList(customLevels);
        ListView<String> levelsView = new ListView<>(levels);
        levelsView.getStyleClass().addAll("listView");
        
        borderPane.setCenter(levelsView);
        BorderPane.setMargin(levelsView, new Insets(5, 0, 10, 0));
        
        Button okButton = new Button("OK");
        okButton.setOnAction(event -> {
            String level = levelsView.getSelectionModel().getSelectedItem();
            System.out.println(level);
            stage.close();
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            stage.close();
        });
        
        ButtonBar buttons = new ButtonBar();
        buttons.getButtons().addAll(okButton, cancelButton);
        borderPane.setBottom(buttons);
        
        Scene scene = new Scene(borderPane, 2, 2);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();
    }
    
    private static String getLevelName(Path levelPath) {
        try {
            String fileContent = Files.readString(levelPath);
            Level level = LevelLoader.loadLevel(fileContent);
            
            return level.getTitle();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
}
