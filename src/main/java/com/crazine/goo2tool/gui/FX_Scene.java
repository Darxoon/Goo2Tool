package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.functional.SaveGui;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class FX_Scene {

    private static Stage stage;
    
    private static Scene scene;
    public static Scene getScene() {
        return scene;
    }


    public static void buildScene(Stage stage) {

        FX_Scene.stage = stage;
        
        TabPane tabPane = new TabPane();
        tabPane.prefHeightProperty().bind(stage.heightProperty());

        Tab profileTab = new Tab("Profile");
        profileTab.setContent(FX_Profile.getProfileView());
        profileTab.setClosable(false);
        tabPane.getTabs().add(profileTab);

        Tab modsTab = new Tab("Mods");
        modsTab.setContent(FX_Mods.getModView());
        modsTab.setClosable(false);
        tabPane.getTabs().add(modsTab);

        Tab optionsTab = new Tab("Options");
        optionsTab.setContent(FX_Options.getOptionsView());
        optionsTab.setClosable(false);
        tabPane.getTabs().add(optionsTab);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            save();
        });
        
        Button saveAndPlayButton = new Button("Save and Launch World of Goo 2!");
        saveAndPlayButton.setOnAction(event -> {
            saveAndPlay();
        });
        HBox hBox = new HBox(saveButton, saveAndPlayButton);
        hBox.setPadding(new Insets(0, 10, 10, 10));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        VBox vBox = new VBox(FX_Menu.getMenuBar(), tabPane, hBox);

        scene = new Scene(vBox);

    }
    
    public static void save() {
        ResArchive res;
        try {
            res = ResArchive.loadOrSetupVanilla(stage);
        } catch (IOException e) {
            FX_Alarm.error(e);
            return;
        }
        
        SaveGui.save(stage, res);
    }
    
    public static void saveAndPlay() {
        ResArchive res;
        try {
            res = ResArchive.loadOrSetupVanilla(stage);
        } catch (IOException e) {
            FX_Alarm.error(e);
            return;
        }
        
        Optional<BooleanProperty> finished = SaveGui.save(stage, res);
        if (finished.isEmpty())
            return;
        
        finished.get().addListener((observable, oldValue, newValue) -> {
            try {
                launchGame();
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
        });
    }
    
    private static void launchGame() throws IOException {
        String customLaunchCommand = PropertiesLoader.getProperties().getLaunchCommand();
        Process process = null;
        
        if (!customLaunchCommand.isEmpty()) {
            switch (Platform.getCurrent()) {
                case WINDOWS:
                    // TODO: use cmd.exe for this probably
                    break;
                case MAC:
                case LINUX:
                    ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/sh", "-c", customLaunchCommand);
                    process = processBuilder.start();
                    break;
            }
        } else {
            // directly launch executable
            File customWOG2Dir = new File(PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory());
            File[] children = customWOG2Dir.listFiles();
            if (children == null) return;
            for (File child : children) {
                if (child.isDirectory()) continue;
                if (Files.isExecutable(child.toPath())) {
                    ProcessBuilder processBuilder = new ProcessBuilder(child.getPath());
                    process = processBuilder.start();
                    break;
                }
            }
        }
        
        final Process process2 = process;
        if (process2 != null) {
            process2.onExit().thenRun(() -> {
                if (process2.exitValue() != 0) {
                    FX_Alarm.error(new RuntimeException("Game terminated with exit code " + process2.exitValue()));
                }
            });
        }
    }
}
