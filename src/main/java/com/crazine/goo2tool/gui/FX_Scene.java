package com.crazine.goo2tool.gui;

// import com.crazine.goo2tool.functional.Save;
import com.crazine.goo2tool.functional.Save;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FX_Scene {

    private static Scene scene;
    public static Scene getScene() {
        return scene;
    }


    public static void buildScene(Stage stage) {

        TabPane tabPane = new TabPane();

        Tab profileTab = new Tab("Profile");
        profileTab.setContent(FX_Profile.getProfileView());
        tabPane.getTabs().add(profileTab);

        Tab modsTab = new Tab("Mods");
        modsTab.setContent(FX_Mods.getModView());
        tabPane.getTabs().add(modsTab);

        Tab optionsTab = new Tab("Options");
        optionsTab.setContent(FX_Options.getOptionsView());
        tabPane.getTabs().add(optionsTab);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> Save.save());
        Button saveAndPlayButton = new Button("Save and Launch World of Goo 2!");
        HBox hBox = new HBox(saveButton, saveAndPlayButton);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        VBox vBox = new VBox(FX_Menu.getMenuBar(), tabPane, hBox);

        scene = new Scene(vBox);

    }
}
