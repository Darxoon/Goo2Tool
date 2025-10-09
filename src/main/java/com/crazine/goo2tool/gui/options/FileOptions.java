package com.crazine.goo2tool.gui.options;

import com.crazine.goo2tool.functional.LocateGooDir;
import com.crazine.goo2tool.functional.LocateGooDir.GooDir;
import com.crazine.goo2tool.gui.FX_Setup;
import com.crazine.goo2tool.gui.Main_Application;
import com.crazine.goo2tool.gui.util.CustomFileChooser;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.util.Platform;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOptions {
    
    private static Logger logger = LoggerFactory.getLogger(FileOptions.class);
    
    private Stage stage;
    private boolean launchMainApplication;
    private GridPane contents;
    
    // These have to be defined here otherwise they will get garbage collected and break
    private BooleanExpression notSteamProperty = PropertiesLoader.getProperties().steamProperty().not();
    private BooleanExpression notAppImageProperty = Platform.getCurrent() == Platform.LINUX
            ? PropertiesLoader.getProperties().steamProperty()
            : new ReadOnlyBooleanWrapper(true);
    
    private int rowIndex = 0;
    
    public GridPane getContents() {
        return contents;
    }
    
    public FileOptions(Stage stage, boolean launchMainApplication) {
        this.stage = stage;
        this.launchMainApplication = launchMainApplication;
        
        contents = new GridPane();
        contents.setHgap(8);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setMinWidth(160);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        ColumnConstraints column3 = new ColumnConstraints();
        ColumnConstraints column4 = new ColumnConstraints();
        contents.getColumnConstraints().addAll(column1, column2, column3, column4);
        
        ExtensionFilter exeFilter = switch (Platform.getCurrent()) {
            case WINDOWS -> new ExtensionFilter("World of Goo 2 executable", "*.exe");
            case LINUX -> new ExtensionFilter("World of Goo 2 executable (WorldOfGoo2, *.exe, *.AppImage)",
                    "*.exe", "WorldOfGoo2", "*.AppImage");
            case MAC -> null;
        };
        
        Properties properties = PropertiesLoader.getProperties();
        
        StringProperty baseDir = properties.baseWorldOfGoo2DirectoryProperty();
        createComplexSetting("Base WoG2 Installation", baseDir, exeFilter, true, path -> {
            updateGameVersion(path);
        });

        StringProperty customDir = properties.customWorldOfGoo2DirectoryProperty();
        createConditionalSetting("Custom WoG2 Installation", customDir, null, notSteamProperty);

        StringProperty profileDir = properties.profileDirectoryProperty();
        createSetting("Profile", profileDir, null);
        
        ExtensionFilter saveFileFilter = new ExtensionFilter("World of Goo 2 save file", "*.dat");
        StringProperty saveFileDir = properties.saveFilePathProperty();
        createSetting("Save Files", saveFileDir, saveFileFilter);
        
        ExtensionFilter resGooFilter = new ExtensionFilter("res.goo file", "res.goo", "*.*");
        StringProperty resGooPath = properties.resGooPathProperty();
        createConditionalSetting("res.goo file", resGooPath, resGooFilter, notAppImageProperty);
    }
    
    private void updateGameVersion(String baseWog2) {
        Properties properties = PropertiesLoader.getProperties();
        
        switch (Platform.getCurrent()) {
            case WINDOWS: {
                boolean prevSteam = properties.isSteam();
                Path steamExePath = Path.of(baseWog2, "WorldOfGoo2.exe");
                
                if (Files.isRegularFile(steamExePath)) {
                    // Steam
                    properties.setSteam(true);
                    
                    updateProfileAndSaveFile(prevSteam, false, "Steam version");
                    
                    FX_Setup.detectFistyVersion(baseWog2);
                } else {
                    // Standalone windows version
                    properties.setSteam(false);
                    
                    updateProfileAndSaveFile(prevSteam, false, "Standalone version");
                    
                    properties.setFistyVersion(null);
                }
                break;
            }
            case MAC:
                break;
            case LINUX: {
                boolean prevSteam = properties.isSteam();
                boolean prevProton = properties.isProton();
                
                Path steamExePath = Path.of(baseWog2, "WorldOfGoo2.exe");
                
                if (Files.isRegularFile(steamExePath)) {
                    // Steam Windows
                    properties.setSteam(true);
                    properties.setProton(true);
                    
                    updateProfileAndSaveFile(prevSteam, prevProton, "Windows Steam version");
                    
                    FX_Setup.detectFistyVersion(baseWog2);
                } else if (baseWog2.endsWith(".AppImage")) {
                    // AppImage
                    properties.setSteam(false);
                    properties.setProton(false);
                    
                    updateProfileAndSaveFile(prevSteam, prevProton, "Linux AppImage");
                    
                    properties.setFistyVersion(null);
                } else {
                    // Steam Linux
                    properties.setSteam(true);
                    properties.setProton(false);
                    
                    updateProfileAndSaveFile(prevSteam, prevProton, "native Steam version");
                    
                    properties.setFistyVersion(null);
                }
                
                break;
            }
        }
    }
    
    private void updateProfileAndSaveFile(boolean prevSteam, boolean prevProton, String platformName) {
        Properties properties = PropertiesLoader.getProperties();
        
        if (prevSteam != properties.isSteam() || prevProton != properties.isProton()) {
            Optional<GooDir> located = properties.isSteam()
                    ? LocateGooDir.locateWog2() : Optional.empty();
            
            try {
                properties.setProfileDirectory(FX_Setup.getProfileDirectory(stage, located));
            } catch (IOException e) {
                properties.setProfileDirectory("");
                FX_Alarm.error(e);
            }
            try {
                properties.setSaveFilePath(FX_Setup.getSaveFilePath(stage, located));
            } catch (IOException e) {
                properties.setSaveFilePath("");
                FX_Alarm.error(e);
            }
            
            if (Platform.getCurrent() != Platform.LINUX || !prevSteam || !properties.isSteam()) {
                properties.setResGooPath("");
            }
            
            FX_Alert.info("Goo2Tool",
                    "Reinitialized other properties for " + platformName,
                    ButtonType.OK);
        }
    }
    
    // If filter is null, will open a directory picker
    private void createComplexSetting(String labelText, StringProperty initialValue,
            ExtensionFilter filter, boolean useParent, Consumer<String> onChange) {
        createSettingRaw(labelText, initialValue, filter, useParent, null, onChange);
    }
    
    private void createSetting(String labelText, StringProperty initialValue,
            ExtensionFilter filter) {
        createSettingRaw(labelText, initialValue, filter, false, null, null);
    }
    
    private void createConditionalSetting(String labelText, StringProperty initialValue,
            ExtensionFilter filter, BooleanExpression condition) {
        createSettingRaw(labelText, initialValue, filter, false, condition, null);
    }
    
    private void createSettingRaw(String labelText, StringProperty initialValue, ExtensionFilter filter,
            boolean useParent, BooleanExpression condition, Consumer<String> onChange) {
        Label label = new Label(labelText);
        // label.setPrefWidth(160);
        label.setPadding(new Insets(4, 0, 0, 0));
        
        Region empty = new Region();
        
        Label dirLabel = new Label();
        dirLabel.textProperty().bind(initialValue);
        // dirLabel.setPrefWidth(200);
        dirLabel.setPadding(new Insets(4, 0, 0, 0));
        dirLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(initialValue);
        dirLabel.setTooltip(tooltip);
        
        Button changeDirButton = new Button("...");
        changeDirButton.setOnAction(event -> {
            File chosenFile;
            try {
                if (filter != null) {
                    // Open file picker
                    Path initialDir = Path.of(initialValue.get());
                    if (Files.isRegularFile(initialDir))
                        initialDir = initialDir.getParent();
                    
                    Optional<Path> chosenPath = CustomFileChooser.openFile(stage,
                            "Please choose location", initialDir, filter);
                    
                    if (chosenPath.isEmpty())
                        return;
                    
                    chosenFile = chosenPath.get().toFile();
                } else {
                    // Open dir picker
                    Optional<Path> chosenPath = CustomFileChooser.chooseDirectory(stage,
                            "Please choose location", Path.of(initialValue.get()));
                    
                    if (chosenPath.isEmpty())
                        return;
                    
                    chosenFile = chosenPath.get().toFile();
                }
            } catch (IOException e) {
                FX_Alarm.error(e);
                return;
            }
            
            if (chosenFile == null) return;
            
            // Dirty hack to support AppImage
            if (useParent && !chosenFile.toString().endsWith(".AppImage")) {
                chosenFile = chosenFile.getParentFile();
                if (chosenFile == null) return;
            }
            
            logger.debug("Set property {} to '{}'", labelText, chosenFile.getAbsolutePath());
            
            initialValue.set(chosenFile.getAbsolutePath());
            
            if (onChange != null) {
                onChange.accept(chosenFile.getAbsolutePath());
            }
            
            try {
                PropertiesLoader.saveProperties();
            } catch (IOException e) {
                FX_Alarm.error(e);
            }
            
            if (launchMainApplication && PropertiesLoader.allImportantInitialized()) {
                stage.hide();
                new Main_Application().start(stage);
            }
        });
        
        int currentRowIndex = rowIndex;
        
        Node[] rowChildren = new Node[] { label, empty, dirLabel, changeDirButton };
        contents.addRow(currentRowIndex, rowChildren);
        
        if (condition != null) {
            
            logger.debug("Condition for {} is {}", labelText, condition.get());
            
            if (condition.get() == false) {
                contents.getChildren().removeAll(rowChildren);
            }
            
            condition.addListener((observable, oldValue, newValue) -> {
                logger.debug("Condition for {} changed from {} to {}", labelText, oldValue, newValue);
                
                if (oldValue == false && newValue == true) {
                    logger.debug("Condition for {} is {}", labelText, condition.get());
                    contents.getChildren().addAll(rowChildren);
                } else if (oldValue == true && newValue == false) {
                    logger.debug("Condition for {} is {}", labelText, condition.get());
                    contents.getChildren().removeAll(rowChildren);
                }
            });
            
        }
        
        rowIndex++;
    }
    
}
