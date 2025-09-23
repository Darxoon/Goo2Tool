package com.crazine.goo2tool.gui.options;

import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.gui.Main_Application;
import com.crazine.goo2tool.gui.util.CustomFileChooser;
import com.crazine.goo2tool.gui.util.FX_Alarm;
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
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOptions {
    
    private static Logger logger = LoggerFactory.getLogger(FileOptions.class);
    
    private Stage stage;
    private boolean launchMainApplication;
    private GridPane contents;
    
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
            case WINDOWS -> new ExtensionFilter("World of Goo 2 executable", "World Of Goo 2.exe");
            case LINUX -> new ExtensionFilter("World of Goo 2 executable (WorldOfGoo2, *.exe, *.AppImage)",
                    "*.exe", "WorldOfGoo2", "*.AppImage");
            case MAC -> null;
        };
        
        String baseDir = PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory();
        createSetting(contents, 0, "Base WoG2 Installation", baseDir, exeFilter, true, path -> {
            PropertiesLoader.getProperties().setBaseWorldOfGoo2Directory(path);
        });

        if (!PropertiesLoader.getProperties().isSteam()) {
            String customDir = PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory();
            createSetting(contents, 1, "Custom WoG2 Installation", customDir, null, false, path -> {
                PropertiesLoader.getProperties().setCustomWorldOfGoo2Directory(path);
            });
        }

        String profileDir = PropertiesLoader.getProperties().getProfileDirectory();
        createSetting(contents, 2, "Profile", profileDir, null, false, path -> {
            PropertiesLoader.getProperties().setProfileDirectory(path);
        });
        
        ExtensionFilter saveFileFilter = new ExtensionFilter("World of Goo 2 save file", "wog2_1.dat", "savegame.dat");
        String saveFileDir = PropertiesLoader.getProperties().getSaveFilePath();
        createSetting(contents, 3, "Save Files", saveFileDir, saveFileFilter, false, path -> {
            PropertiesLoader.getProperties().setSaveFilePath(path);
        });
        
        ExtensionFilter resGooFilter = new ExtensionFilter("res.goo file", "res.goo", "*.*");
        String resGooPath = PropertiesLoader.getProperties().getResGooPath();
        createSetting(contents, 4, "res.goo file", resGooPath, resGooFilter, false, path -> {
            PropertiesLoader.getProperties().setResGooPath(path);
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
                Path initialDir = Path.of(initialValue);
                if (Files.isRegularFile(initialDir))
                    initialDir = initialDir.getParent();
                
                try {
                    chosenFile = CustomFileChooser.chooseFile(stage,
                            "Please choose location", initialDir, filter).toFile();
                } catch (IOException e) {
                    FX_Alarm.error(e);
                    return;
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(initialValue));
                chosenFile = directoryChooser.showDialog(stage);
            }
            
            if (chosenFile == null) return;
            
            if (useParent) {
                chosenFile = chosenFile.getParentFile();
                if (chosenFile == null) return;
            }
            
            logger.debug("Set property {} to '{}'", labelText, chosenFile.getAbsolutePath());
            dirLabel.setText(chosenFile.getAbsolutePath());
            dirLabel.getTooltip().setText(chosenFile.getAbsolutePath());
            
            onChange.accept(chosenFile.getAbsolutePath());
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
        
        grid.addRow(rowIndex, label, empty, dirLabel, changeDirButton);
    }
    
}
