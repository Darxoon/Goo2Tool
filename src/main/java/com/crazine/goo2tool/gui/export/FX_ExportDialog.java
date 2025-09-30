package com.crazine.goo2tool.gui.export;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.util.IconLoader;
import com.crazine.goo2tool.util.VersionNumber;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FX_ExportDialog {
    
    private static final VersionNumber DEFAULT_VERSION = new VersionNumber(1, 0);
    
    public static record AddinInfo(String modId, String name, VersionNumber version,
            String author, String description, boolean embedThumbnail) {}
    
    // Same as AddinInfo but consists of JavaFX Properties
    private static class AddinPropertyInfo {
        public StringProperty modId = new SimpleStringProperty("");
        public StringProperty name = new SimpleStringProperty("");
        public StringProperty version = new SimpleStringProperty("");
        public StringProperty author = new SimpleStringProperty("");
        public StringProperty description = new SimpleStringProperty("");
        public BooleanProperty embedThumbnail = new SimpleBooleanProperty(true);
        
        public AddinInfo toAddinInfo() throws NumberFormatException {
            VersionNumber version = this.version.get().isBlank()
                    ? DEFAULT_VERSION : VersionNumber.fromString(this.version.get());
            
            return new AddinInfo(modId.get(), name.get(), version, author.get(),
                    description.get(), embedThumbnail.get());
        }
        
        public static AddinPropertyInfo fromAddinInfo(AddinInfo addinInfo) {
            AddinPropertyInfo result = new AddinPropertyInfo();
            
            result.modId.set(addinInfo.modId());
            result.name.set(addinInfo.name());
            
            if (!DEFAULT_VERSION.equals(addinInfo.version()))
                result.version.set(addinInfo.version().toString());
            
            result.author.set(addinInfo.author());
            result.description.set(addinInfo.description());
            result.embedThumbnail.set(addinInfo.embedThumbnail());
            
            return result;
        }
    }
    
    private static final double VERTIAL_GAP = 4;
    
    public static ObjectProperty<Optional<AddinInfo>> show(Stage originalStage, String levelName, Optional<AddinInfo> startingValues) {
        
        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setWidth(400);
        stage.setHeight(400);
        
        stage.setTitle("Package level as .goo2mod");
        
        stage.getIcons().add(IconLoader.getTerrain());
        
        // create UI
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setTop(new Label("Please input information to your addin:"));
        
        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(VERTIAL_GAP);
        
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        
        gridPane.getColumnConstraints().addAll(column1, column2);
        
        // Add grid options
        AddinPropertyInfo addinInfo;
        if (startingValues.isPresent()) {
            addinInfo = AddinPropertyInfo.fromAddinInfo(startingValues.get());
        } else {
            addinInfo = new AddinPropertyInfo();
            addinInfo.name.set(levelName);
        }
        
        AtomicInteger row = new AtomicInteger(0);
        
        addGridOption(gridPane, row, addinInfo.name, "Mod Name", "");
        addGridOption(gridPane, row, addinInfo.author, "Author Name", "");
        addGridOption(gridPane, row, addinInfo.modId, "Mod ID", "author.ModName");
        addGridOption(gridPane, row, addinInfo.version, "Version", "1.0");
        
        // Add other options
        Label descriptionLabel = new Label("Description");
        TextArea description = new TextArea();
        description.setPrefHeight(111);
        description.textProperty().bindBidirectional(addinInfo.description);
        VBox.setMargin(description, new Insets(0, 0, VERTIAL_GAP, 0));
        
        CheckBox embedThumbnail = new CheckBox("Embed Thumbnail in addin file");
        embedThumbnail.selectedProperty().bindBidirectional(addinInfo.embedThumbnail);
        
        VBox content = new VBox(gridPane, descriptionLabel, description, embedThumbnail);
        content.setSpacing(VERTIAL_GAP);
        
        // Buttons
        ObjectProperty<Optional<AddinInfo>> result = new SimpleObjectProperty<>();
        
        Button okButton = new Button("Export");
        okButton.setOnAction(event -> {
            try {
                result.set(Optional.of(addinInfo.toAddinInfo()));
            } catch (NumberFormatException e) {
                FX_Alert.error("Goo2Tool",
                        String.format("Invalid version number '%s'", addinInfo.version.get()),
                        ButtonType.OK);
                
                return;
            }
            
            stage.close();
        });
        
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
        
        Scene scene = new Scene(borderPane, 2, 2);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();
        
        return result;
    }
    
    private static void addGridOption(GridPane gridPane, AtomicInteger row, StringProperty property, String label, String placeholder) {
        int rowValue = row.getAndIncrement();
        
        TextField field = new TextField();
        field.textProperty().bindBidirectional(property);
        
        if (!placeholder.isEmpty())
            field.setPromptText(placeholder);
        
        gridPane.add(new Label(label), 0, rowValue);
        gridPane.add(field, 1, rowValue);
        
        // Make sure the first TextField is focused and not the ButtonBar
        if (rowValue == 0) {
            Platform.runLater(field::requestFocus);
        }
    }
    
}
