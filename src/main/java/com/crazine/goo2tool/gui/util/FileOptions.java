package com.crazine.goo2tool.gui.util;

import com.crazine.goo2tool.gui.FX_Alarm;
import com.crazine.goo2tool.gui.Main_Application;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class FileOptions {
    
    private Stage stage;
    private GridPane contents;
    
    public GridPane getContents() {
        return contents;
    }
    
    public FileOptions(Stage stage, boolean launchMainApplication) {
        this.stage = stage;
        
        contents = new GridPane();
        contents.setHgap(8);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setMinWidth(160);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        ColumnConstraints column3 = new ColumnConstraints();
        ColumnConstraints column4 = new ColumnConstraints();
        contents.getColumnConstraints().addAll(column1, column2, column3, column4);
        
        
        ExtensionFilter exeFilter = new ExtensionFilter("World of Goo 2 executable", "World Of Goo 2.exe");
        String baseDir = PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory();
        createSetting(contents, 0, "Base WoG2 Installation", baseDir, exeFilter, true, path -> {
            PropertiesLoader.getProperties().setBaseWorldOfGoo2Directory(path);
            
            try {
                PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
            
            if (launchMainApplication && PropertiesLoader.isAllInitialized()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });

        String customDir = PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory();
        createSetting(contents, 1, "Custom WoG2 Installation", customDir, null, false, path -> {
            PropertiesLoader.getProperties().setCustomWorldOfGoo2Directory(path);
            
            try {
                PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
            
            if (launchMainApplication && PropertiesLoader.isAllInitialized()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });

        ExtensionFilter profileFilter = new ExtensionFilter("World of Goo 2 save file", "wog2_1.dat");
        String profileDir = PropertiesLoader.getProperties().getProfileDirectory();
        createSetting(contents, 2, "Save Files", profileDir, profileFilter, false, path -> {
            PropertiesLoader.getProperties().setProfileDirectory(path);
            
            try {
                PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
            
            if (launchMainApplication && PropertiesLoader.isAllInitialized()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });
    }
    
    // If filter is null, will open a directory picker
    private void createSetting(GridPane grid, int rowIndex, String labelText, String initialValue,
            ExtensionFilter filter, boolean useParent, Consumer<String> onChange) {
        Label label = new Label(labelText);
        // label.setPrefWidth(160);
        label.setPadding(new Insets(4, 0, 0, 0));
        
        Region empty = new Region();
        
        Label dirLabel = new Label(initialValue);
        dirLabel.setTooltip(new Tooltip(initialValue));
        // dirLabel.setPrefWidth(200);
        dirLabel.setPadding(new Insets(4, 0, 0, 0));
        dirLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        
        Button changeDirButton = new Button("...");
        changeDirButton.setOnAction(event -> {
            File chosenFile;
            if (filter != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(filter);
                chosenFile = fileChooser.showOpenDialog(stage);
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                chosenFile = directoryChooser.showDialog(stage);
            }
            
            if (chosenFile == null) return;
            
            if (useParent) {
                chosenFile = chosenFile.getParentFile();
                if (chosenFile == null) return;
            }
            
            System.out.println(labelText + ": " + chosenFile.getAbsolutePath());
            dirLabel.setText(chosenFile.getAbsolutePath());
            dirLabel.getTooltip().setText(chosenFile.getAbsolutePath());
            onChange.accept(chosenFile.getAbsolutePath());
        });
        
        grid.addRow(rowIndex, label, empty, dirLabel, changeDirButton);
    }
    
}
