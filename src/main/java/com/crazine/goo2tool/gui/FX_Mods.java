package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.addinFile.Goo2mod.ModType;
import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.gui.util.CustomFileChooser;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FX_Mods {

    private static Stage stage;
    
    private static final VBox modView = new VBox();
    public static VBox getModView() {
        return modView;
    }


    private static final TableView<Goo2mod> modTableView = new TableView<>();
    public static TableView<Goo2mod> getModTableView() {
        return modTableView;
    }


    public static void buildModView(Stage stage) {
        
        FX_Mods.stage = stage;

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
                        e.printStackTrace();
                        
                        Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
                        dialog.setContentText("Failed loading mod \"" + file.getName() + "\":\n\n" + e.getMessage());
                        dialog.show();
                    }
                }
                
                success = true;
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        Label modInfo = new Label("Addins higher on the list have priority and " +
                "can override files from addins lower on the list.");
        Label modInfo2 = new Label("New levels will appear in the Editor Menu on the title screen.");

        VBox infoVBox = new VBox();
        infoVBox.getChildren().addAll(modInfo, modInfo2);
        infoVBox.setPadding(new Insets(10, 0, 0, 0));
        infoVBox.setSpacing(15);


        Button installNewAddinButton = new Button("Install new addin...");
        installNewAddinButton.setMinWidth(120);
        installNewAddinButton.setOnAction(event -> installAddin());
        Button checkForUpdatesButton = new Button("Check for Updates...");
        checkForUpdatesButton.setMinWidth(120);
        checkForUpdatesButton.setDisable(true);
        Hyperlink findMoreAddinsLink = new Hyperlink("Find more addins");
        findMoreAddinsLink.setDisable(true);
        findMoreAddinsLink.setMinWidth(120);
        findMoreAddinsLink.setAlignment(Pos.CENTER);
        findMoreAddinsLink.setPadding(new Insets(-2, 0, -2, 0));
        findMoreAddinsLink.setOnAction(event -> findMoreAddinsLink.setVisited(false));

        installNewAddinButton.prefWidthProperty().bind(checkForUpdatesButton.widthProperty());
        findMoreAddinsLink.prefWidthProperty().bind(checkForUpdatesButton.widthProperty());
        
        VBox buttonsVBox = new VBox();
        buttonsVBox.getChildren().addAll(installNewAddinButton, checkForUpdatesButton, findMoreAddinsLink);
        buttonsVBox.setSpacing(5);

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(infoVBox);
        headerPane.setRight(buttonsVBox);
        modView.getChildren().add(headerPane);


        Button moveUp = new Button("Move Up");
        moveUp.setDisable(modTableView.getSelectionModel().isEmpty());
        moveUp.setOnAction(event -> {
            ObservableList<Goo2mod> items = modTableView.getItems();
            
            Goo2mod selected = modTableView.getSelectionModel().getSelectedItem();
            int index = items.indexOf(selected);
            items.remove(index);
            items.add(index - 1, selected);
            modTableView.getSelectionModel().select(selected);
        });
        
        Button moveDown = new Button("Move Down");
        moveDown.setDisable(modTableView.getSelectionModel().isEmpty());
        moveDown.setOnAction(event -> {
            ObservableList<Goo2mod> items = modTableView.getItems();
            
            Goo2mod selected = modTableView.getSelectionModel().getSelectedItem();
            int index = items.indexOf(selected);
            items.remove(index);
            items.add(index + 1, selected);
            modTableView.getSelectionModel().select(selected);
        });
        
        Button enable = new Button("Enable");
        enable.setDisable(modTableView.getSelectionModel().isEmpty());
        enable.setOnAction(event -> {
            Goo2mod selected = modTableView.getSelectionModel().getSelectedItem();
            Optional<AddinConfigEntry> addin = PropertiesLoader.getProperties().getAddin(selected);
            
            if (addin.isPresent()) {
                addin.get().setLoaded(true);
            }
        });
        
        Button disable = new Button("Disable");
        disable.setDisable(modTableView.getSelectionModel().isEmpty());
        disable.setOnAction(event -> {
            Goo2mod selected = modTableView.getSelectionModel().getSelectedItem();
            Optional<AddinConfigEntry> addin = PropertiesLoader.getProperties().getAddin(selected);
            
            if (addin.isPresent()) {
                addin.get().setLoaded(false);
            }
        });
        
        Button uninstall = new Button("Uninstall");
        uninstall.setDisable(modTableView.getSelectionModel().isEmpty());
        uninstall.setOnAction(event -> {
            Goo2mod selected = modTableView.getSelectionModel().getSelectedItem();
            
            Optional<ButtonType> result = FX_Alert.show("Goo2Tool", String.format("""
                    You are trying to uninstall "%s".
                    Uninstalling a mod cannot be undone.
                    Do you want to continue?
                    """, selected.getName()), IconLoader.getConduit(), ButtonType.YES, ButtonType.NO);
            
            if (result.isPresent() && result.get() == ButtonType.YES) {
                PropertiesLoader.uninstallGoo2mod(selected);
            }
        });

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
                        Optional<AddinConfigEntry> addin = PropertiesLoader.getProperties().getAddin(getTableRow().getItem());
                        
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
        type.setCellValueFactory(features -> {
            ModType modType = features.getValue().getType();
            return new ReadOnlyStringWrapper(modType.getName());
        });
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

        // TODO: is this the best resize policy?
        modTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(160);

        modTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                descriptionArea.setText("");
                
                enable.disableProperty().unbind();
                enable.setDisable(true);
                disable.disableProperty().unbind();
                disable.setDisable(true);
                
                moveUp.setDisable(true);
                moveDown.setDisable(true);
                uninstall.setDisable(true);
                return;
            }
            
            descriptionArea.setText(newValue.getDescription());
            
            Optional<AddinConfigEntry> addin = PropertiesLoader.getProperties().getAddin(newValue);
            if (addin.isPresent()) {
                uninstall.setDisable(false);
                
                enable.disableProperty().bind(addin.get().loadedProperty());
                disable.disableProperty().bind(addin.get().loadedProperty().not());
                
                int index = modTableView.getItems().indexOf(newValue);
                moveUp.setDisable(index <= 0);
                moveDown.setDisable(index >= modTableView.getItems().size() - 1);
            }
        });

        Button propertiesButton = new Button("Properties");
        propertiesButton.setDisable(true);

        HBox box = new HBox();
        box.setSpacing(10);
        box.getChildren().addAll(moveUp, moveDown, enable, disable, uninstall);
        box.setAlignment(Pos.CENTER_RIGHT);

        BorderPane stackPane = new BorderPane();
        stackPane.setLeft(propertiesButton);
        stackPane.setRight(box);

        modView.getChildren().addAll(modTableView, descriptionArea, stackPane);

    }

    public static void installAddin() {
        Optional<Path> goomodFile;
        try {
            ExtensionFilter filter = new ExtensionFilter(
                    "World of Goo 2 addin (*.goo2mod)", "*.goo2mod");
            goomodFile = CustomFileChooser.openFile(stage, "Open addin to install", filter);
        } catch (IOException e) {
            FX_Alarm.error(e);
            return;
        }
        
        if (goomodFile.isEmpty()) return;
        
        try {
            PropertiesLoader.loadGoo2mod(goomodFile.get().toFile());
        } catch (IOException e) {
            e.printStackTrace();
            Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setContentText("Failed to open addin: " + e.getMessage());
            
            if (IconLoader.getConduit() != null) {
                Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(IconLoader.getConduit());
            }
            
            dialog.show();
        }
    }
    
}
