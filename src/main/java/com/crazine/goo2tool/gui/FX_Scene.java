package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.functional.FistyInstaller;
import com.crazine.goo2tool.functional.save.SaveGui;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FX_Scene {

    private static Stage stage;
    
    private static Scene scene;
    public static Scene getScene() {
        return scene;
    }


    public static void buildScene(Stage stage) {

        FX_Scene.stage = stage;
        
        // Create tabs
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

        // Create buttons at the bottom
        HBox hBox = new HBox();
        
        Properties properties = PropertiesLoader.getProperties();
        
        // TODO: auto detect if FistyLoader is already installed at setup
        if (properties.getFistyVersion() == null || properties.getFistyVersion().compareTo(FistyInstaller.FISTY_VERSION) < 0) {
            Button installFistyButton = properties.getFistyVersion() == null
                    ? new Button("Install FistyLoader")
                    : new Button("Update FistyLoader");
            
            boolean disabled = switch (Platform.getCurrent()) {
                case WINDOWS -> !properties.isSteam();
                case MAC -> true;
                case LINUX -> !properties.isSteam() && !properties.isProton();
            };
            
            installFistyButton.setOnAction(event -> {
                Optional<ButtonType> result = FX_Alert.show("FistyLoader",
                        "FistyLoader is an exe mod for World of Goo 2 which is necessary "
                        + "to use custom goo balls. Some mods might require this.\n\n"
                        + "Do you want to install?",
                        ButtonType.YES, ButtonType.NO);
                
                if (result.isEmpty() || result.get() == ButtonType.NO)
                    return;
                
                try {
                    FistyInstaller.installFisty();
                    PropertiesLoader.saveProperties();
                } catch (IOException e) {
                    FX_Alarm.error(e);
                    return;
                }
                
                FX_Alert.show("FistyLoader", "Successfully installed.", ButtonType.OK);
                hBox.getChildren().remove(installFistyButton);
            });
            
            installFistyButton.setDisable(disabled);
            hBox.getChildren().add(installFistyButton);
        }
        
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            save();
        });
        
        Button saveAndPlayButton = new Button("Save and Launch World of Goo 2!");
        saveAndPlayButton.setOnAction(event -> {
            saveAndPlay();
        });
        
        hBox.getChildren().addAll(saveButton, saveAndPlayButton);
        
        hBox.setPadding(new Insets(0, 10, 10, 10));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        VBox vBox = new VBox(FX_Menu.getMenuBar(), tabPane, hBox);

        scene = new Scene(vBox);
        scene.getStylesheets().add("style.css");

    }
    
    public static void save() {
        // TODO: verify dependencies between mods (especially FistyLoader)
        SaveGui.save(stage);
    }
    
    public static void saveAndPlay() {
        Optional<Property<SaveGui.Result>> finished = SaveGui.save(stage);
        
        if (finished.isEmpty())
            return;
                
        finished.get().addListener((observable, oldValue, newValue) -> {
            if (newValue != SaveGui.Result.Success)
                return;
            
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
                    ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", customLaunchCommand);
                    process = processBuilder.start();
                    break;
            }
        } else if (PropertiesLoader.getProperties().isSteam()) {
            if (Platform.getCurrent() == Platform.LINUX) {
                // X11 specific issue for some reason?
                ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/env", "steam", "steam://rungameid/3385670");
                processBuilder.start();
            } else {
                Main_Application.openUrl("steam://rungameid/3385670");
            }
        } else {
            // TODO: test this
            Path customWog2 = Path.of(PropertiesLoader.getProperties().getTargetWog2Directory());
            
            // directly launch executable
            Path gameExecutable = switch (Platform.getCurrent()) {
                case WINDOWS -> customWog2.resolve("World of Goo 2.exe");
                case MAC -> customWog2.resolve("MacOS/WorldOfGoo2");
                case LINUX -> customWog2.resolve("WorldOfGoo2");
            };
            
            if (!Files.isExecutable(gameExecutable))
                throw new IOException("File '" + gameExecutable + "' is not executable!");
            
            ProcessBuilder processBuilder = new ProcessBuilder(gameExecutable.toString());
            processBuilder.start();
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
