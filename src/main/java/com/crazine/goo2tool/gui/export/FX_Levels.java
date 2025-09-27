package com.crazine.goo2tool.gui.export;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.functional.export.ExportGui;
import com.crazine.goo2tool.functional.export.addininfocache.AddinInfoCache;
import com.crazine.goo2tool.functional.export.addininfocache.AddinInfoCacheLoader;
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.gui.export.FX_ExportDialog.AddinInfo;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FX_Levels {
    
    private static Logger logger = LoggerFactory.getLogger(FX_Levels.class);
    
    private static record LevelFile(String id, String title, Path path) {}
    
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
        List<LevelFile> customLevels;
        try {
            
            customLevels = Files.list(profileDir.resolve("levels"))
                .map(path -> getLevelFile(path))
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
        
        ObservableList<LevelFile> levels = FXCollections.observableArrayList(customLevels);
        ListView<LevelFile> levelsView = new ListView<>(levels);
        levelsView.getStyleClass().addAll("listView");
        
        levelsView.setCellFactory(listView -> {
            return new ListCell<>() {
                
                @Override
                public void updateItem(LevelFile item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item != null) {
                        setText(item.title());
                    }
                }
                
            };
        });
        
        borderPane.setCenter(levelsView);
        BorderPane.setMargin(levelsView, new Insets(5, 0, 10, 0));
        
        Button okButton = new Button("OK");
        okButton.setOnAction(event -> {
            LevelFile levelFile = levelsView.getSelectionModel().getSelectedItem();
            stage.close();
            
            AddinInfoCache cache = null;
            Optional<AddinInfo> cachedInfo = Optional.empty();
            try {
                cache = AddinInfoCacheLoader.getOrInit();
                cachedInfo = cache.getEntry(levelFile.id(), levelFile.title());
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
            
            ObjectProperty<Optional<AddinInfo>> addinInfoProperty = FX_ExportDialog.show(originalStage, levelFile.title(), cachedInfo);
            
            AddinInfoCache cache2 = cache;
            addinInfoProperty.addListener(observable -> {
                if (addinInfoProperty.get().isEmpty())
                    return;
                
                AddinInfo addinInfo = addinInfoProperty.get().get();
                
                // Save addinInfo for future export dialogs
                if (cache2 != null) {
                    cache2.addEntry(levelFile.id(), levelFile.title(), addinInfo);
                    
                    try {
                        AddinInfoCacheLoader.save();
                    } catch (IOException e) {
                        FX_Alarm.error(e);
                    }
                }
                
                // Save Dialog
                // TODO: migrate to CustomFileChooser
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WoG2 Addin", "*.goo2mod"));
                fileChooser.setInitialFileName(addinInfo.modId() + ".goo2mod");
                File outFile = fileChooser.showSaveDialog(stage);
                
                logger.info("Exporting level " + levelFile.title() + ": " + levelFile.path());
                ExportGui.exportLevel(stage, addinInfo, levelFile.path(), outFile.toPath());
            });
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
    
    private static LevelFile getLevelFile(Path levelPath) {
        try {
            String fileContent = Files.readString(levelPath);
            Level level = LevelLoader.loadLevel(fileContent);
            
            return new LevelFile(level.getUuid(), level.getTitle(), levelPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
}
