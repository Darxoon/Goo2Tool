package com.crazine.goo2tool.gui.export;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.crazine.goo2tool.util.IconLoader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FX_DependencyPrompt {
    
    public static enum DependencyType {
        REEXPORT,
        REQUIRE,
    }
    
    private static final double VERTIAL_GAP = 4;
    
    public static ObjectProperty<Optional<DependencyType>> show(Stage originalStage, String dependencyModName) {
        
        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setTitle("Package level as .goo2mod");
        
        stage.getIcons().add(IconLoader.getTerrain());
        
        // TODO: handle case when dependencyModName is too long for a single line
        stage.setWidth(480);
        stage.setMinWidth(200);
        stage.setHeight(190);
        stage.setMinHeight(148);
        
        // create UI
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));
        
        Label title = new Label("Your mod is using assets from the mod \"" + dependencyModName + "\". What do you want to do?");
        title.setWrapText(true);
        borderPane.setTop(title);
        
        BooleanProperty buttonDisabled = new SimpleBooleanProperty(true);
        AtomicReference<DependencyType> dependencyType = new AtomicReference<>(null);
        
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton reexport = new RadioButton("Reexport assets in your .goo2mod");
        reexport.setWrapText(true);
        reexport.setToggleGroup(toggleGroup);
        reexport.setOnAction(event -> {
            buttonDisabled.set(false);
            dependencyType.set(DependencyType.REEXPORT);
        });
        
        RadioButton require = new RadioButton("Add \"" + dependencyModName + "\" as dependency (will require user to install it manually)");
        require.setWrapText(true);
        require.setToggleGroup(toggleGroup);
        require.setOnAction(event -> {
            buttonDisabled.set(false);
            dependencyType.set(DependencyType.REQUIRE);
        });
        
        VBox content = new VBox(reexport, require);
        content.setSpacing(VERTIAL_GAP);
        
        // Buttons
        ObjectProperty<Optional<DependencyType>> result = new SimpleObjectProperty<>();
        
        Button okButton = new Button("Continue");
        okButton.setOnAction(event -> {
            stage.close();
            result.set(Optional.of(dependencyType.get()));
        });
        okButton.disableProperty().bind(buttonDisabled);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            stage.close();
            result.set(Optional.empty());
        });
        
        ButtonBar buttons = new ButtonBar();
        buttons.getButtons().addAll(okButton, cancelButton);
        borderPane.setBottom(buttons);
        
        BorderPane.setMargin(content, new Insets(VERTIAL_GAP * 2, 0, VERTIAL_GAP, 0));
        borderPane.setCenter(content);
        borderPane.setCenterShape(true);
        
        Scene scene = new Scene(borderPane, 2, 2);
        scene.getStylesheets().add("style.css");
        
        stage.setScene(scene);
        stage.show();
        
        return result;
    }
    
}
