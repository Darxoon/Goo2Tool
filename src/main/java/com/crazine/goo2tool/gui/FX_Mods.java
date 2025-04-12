package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.PropertiesLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FX_Mods {

    private static final VBox modView = new VBox();
    public static VBox getModView() {
        return modView;
    }


    private static final TableView<Goo2mod> modTableView = new TableView<>();
    public static TableView<Goo2mod> getModTableView() {
        return modTableView;
    }


    public static void buildModView(Stage stage) {

        modView.setPadding(new Insets(10, 10, 10, 10));
        modView.setSpacing(5);
        modView.prefHeightProperty().bind(stage.heightProperty());

        modTableView.prefHeightProperty().bind(modView.heightProperty().subtract(200));

        modView.setOnDragOver(event -> {
            if (event.getGestureSource() != modView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            
            event.consume();
        });
        
        modView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                
                for (File file : files) {
                    try {
                        PropertiesLoader.loadGoo2mod(file);
                    } catch (IOException e) {
                        FX_Alarm.error(new RuntimeException("Failed loading mod " + file.getName() + ": " + e.getMessage(), e));
                    }
                }
                
                success = true;
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        Label modInfo = new Label("Addins higher on the list have priority and " +
                "can override files from addins lower on the list.");
        Label modInfo2 = new Label("New levels...   ...   ...   ...   ...What?");

        VBox infoVBox = new VBox();
        infoVBox.getChildren().addAll(modInfo, modInfo2);
        infoVBox.setPadding(new Insets(10, 0, 0, 0));
        infoVBox.setSpacing(15);


        Button installNewAddinButton = new Button("Install new addin...");
        installNewAddinButton.setPrefWidth(120);
        installNewAddinButton.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World of Goo 2 mod file", "*.goo2mod"));
            File goomodFile = fileChooser.showOpenDialog(stage);
            if (goomodFile == null) return;
            
            try {
                PropertiesLoader.loadGoo2mod(goomodFile);
            } catch (IOException e) {
                FX_Alarm.error(e);
            }

        });
        Button checkForUpdatesButton = new Button("Check for Updates...");
        checkForUpdatesButton.setPrefWidth(120);
        Hyperlink findMoreAddinsLink = new Hyperlink("Find more addins");
        findMoreAddinsLink.setPrefWidth(120);
        findMoreAddinsLink.setAlignment(Pos.CENTER);
        findMoreAddinsLink.setPadding(new Insets(-2, 0, -2, 0));
        findMoreAddinsLink.setOnAction(event -> findMoreAddinsLink.setVisited(false));

        VBox buttonsVBox = new VBox();
        buttonsVBox.getChildren().addAll(installNewAddinButton, checkForUpdatesButton, findMoreAddinsLink);
        buttonsVBox.setSpacing(5);

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(infoVBox);
        headerPane.setRight(buttonsVBox);
        modView.getChildren().add(headerPane);


        Button moveUp = new Button("Move Up");
        Button moveDown = new Button("Move Down");
        
        Button enable = new Button("Enable");
        // enable.onActionProperty().addListener(observable -> {
        //     System.out.println("Enabling");
        // });
        
        Button disable = new Button("Disable");
        Button uninstall = new Button("Uninstall");

        TableColumn<Goo2mod, Boolean> enabled = new TableColumn<>();
        enabled.setCellFactory(param -> {

            TableCell<Goo2mod, Boolean> cell = new TableCell<>() {

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    CheckBox checkBox = new CheckBox();
                    
                    checkBox.setPrefSize(20, 20);
                    checkBox.setOnAction(event -> {
                        setItem(checkBox.isSelected());
                    });
                    checkBox.visibleProperty().bind(emptyProperty().not());

                    setGraphic(checkBox);
                    
                    if (getTableRow().getItem() != null) {
                        Optional<AddinConfigEntry> addin = PropertiesLoader.getProperties().getAddin(getTableRow().getItem().getId());
                        
                        if (addin.isPresent()) {
                            checkBox.selectedProperty().bindBidirectional(addin.get().loadedProperty());
                        }
                    }
                }
                
            };

            return cell;

        });
        enabled.setMinWidth(24);
        enabled.setMaxWidth(24);
        enabled.setReorderable(false);
        enabled.setSortable(false);
        modTableView.getColumns().add(enabled);

        TableColumn<Goo2mod, String> addinName = new TableColumn<>("Addin Name");
        addinName.setCellValueFactory(new PropertyValueFactory<>("name"));
        addinName.setReorderable(false);
        addinName.setSortable(false);
        modTableView.getColumns().add(addinName);
        addinName.setPrefWidth(300);

        TableColumn<Goo2mod, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        type.setReorderable(false);
        type.setSortable(false);
        modTableView.getColumns().add(type);
        type.setPrefWidth(150);

        TableColumn<Goo2mod, Double> version = new TableColumn<>("Version");
        version.setCellValueFactory(new PropertyValueFactory<>("version"));
        version.setReorderable(false);
        version.setSortable(false);
        modTableView.getColumns().add(version);
        version.setPrefWidth(150);

        TableColumn<Goo2mod, String> author = new TableColumn<>("Author");
        author.setCellValueFactory(new PropertyValueFactory<>("author"));
        author.setReorderable(false);
        author.setSortable(false);
        modTableView.getColumns().add(author);
        author.setPrefWidth(300);

        modTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(160);

        modTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            descriptionArea.setText(newValue.getDescription());
            
            Optional<AddinConfigEntry> addin = PropertiesLoader.getProperties().getAddin(newValue.getId());
            if (addin.isPresent()) {
                enable.disableProperty().bind(addin.get().loadedProperty());
                disable.disableProperty().bind(addin.get().loadedProperty().not());
            }
        });

        Button propertiesButton = new Button("Properties");

        HBox box = new HBox();
        box.setSpacing(10);
        box.getChildren().addAll(moveUp, moveDown, enable, disable, uninstall);
        box.setAlignment(Pos.CENTER_RIGHT);

        BorderPane stackPane = new BorderPane();
        stackPane.setLeft(propertiesButton);
        stackPane.setRight(box);

        modView.getChildren().addAll(modTableView, descriptionArea, stackPane);

    }

}
