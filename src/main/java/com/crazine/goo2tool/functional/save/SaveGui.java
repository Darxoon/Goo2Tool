package com.crazine.goo2tool.functional.save;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.addinfile.AddinFileLoader;
import com.crazine.goo2tool.addinfile.Goo2mod;
import com.crazine.goo2tool.gui.util.CustomFileChooser;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.util.IconLoader;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SaveGui {

    public static enum Result {
        Success,
        Fail,
        Canceled,
    }
    
    public static Optional<Property<Result>> save(Stage originalStage) {
        Properties properties = PropertiesLoader.getProperties();
        
        // Set up customWog2 dir if not already
        if (!properties.isSteam() && properties.getCustomWorldOfGoo2Directory().isEmpty()) {
            Optional<ButtonType> result = FX_Alert.info("Goo2Tool", """
                    Goo2Tool does not modify your existing World of Goo 2 installation.
                    Instead, it copies everything into its own directory first.
                    
                    Please create a new, empty folder in the next dialog or select
                    an existing Goo2Tool directory if you have done this before.
                    """, ButtonType.OK, ButtonType.CANCEL);
            
            if (result.isEmpty() || result.get() != ButtonType.OK)
                return Optional.empty();
            
            try {
                Optional<Path> customDir = CustomFileChooser.chooseDirectory(originalStage, "Create new Goo2Tool directory");
                
                if (customDir.isEmpty())
                    return Optional.empty();
                
                properties.setCustomWorldOfGoo2Directory(customDir.get().toString());
            } catch (IOException e) {
                FX_Alarm.error(e);
                return Optional.empty();
            }
        }
        
        // Show warning that the Steam version might break existing modded content
        if (properties.isSteam() && !properties.isSteamWarningShown()) {
            ButtonType buttonConfirm = new ButtonType("Confirm", ButtonData.OK_DONE);
            
            Optional<ButtonType> result = FX_Alert.info("Goo2Tool", """
                    Since the Steam version does not allow being copied \
                    to another location, Goo2Tool has to modify your \
                    existing main installation.
                    
                    Please note that there is some risk of existing \
                    modded content (custom assets in the res folder) \
                    getting lost. Do you consent to the risk?
                    """, buttonConfirm, ButtonType.CANCEL);
            
            if (result.isEmpty() || result.get().getButtonData() != ButtonData.OK_DONE)
                return Optional.empty();
            
            properties.setSteamWarningShown(true);
        }
        
        // Save properties
        try {
            PropertiesLoader.saveProperties();
        } catch (IOException e) {
            FX_Alarm.error(e);
            return Optional.empty();
        }
        
        // Verify dependencies
        List<Goo2mod> enabledGoo2mods;
		try {
			enabledGoo2mods = AddinFileLoader.loadEnabledAddins();
		} catch (IOException e) {
            FX_Alarm.error(e);
            return Optional.empty();
		}
        
        for (Goo2mod mod : enabledGoo2mods) {
            for (Goo2mod.Depends dependency : mod.getDependencies()) {
                // Special case: FistyLoader
                if (dependency.getId().equals("FistyLoader")) {
                    if (!checkFistyDependency(mod, dependency)) {
                        return Optional.empty();
                    }
                } else if (!checkDependency(mod, dependency, enabledGoo2mods)) {
                    return Optional.empty();
                }
            }
        }
        
        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setWidth(400);
        stage.setHeight(200);

        stage.setTitle("Building your World of Goo 2");

        stage.getIcons().add(IconLoader.getTerrain());

        SaveTask task = new SaveTask(stage);
        
        Label contentLabel = new Label();
        contentLabel.textProperty().bind(task.messageProperty());
        contentLabel.setAlignment(Pos.CENTER);
        contentLabel.setWrapText(true);
        contentLabel.setTextAlignment(TextAlignment.CENTER);

        VBox totalVBox = new VBox();
        totalVBox.getChildren().addAll(contentLabel);
        totalVBox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label();
        headerLabel.textProperty().bind(new SimpleStringProperty("Current task: ").concat(task.titleProperty()));
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.prefWidthProperty().bind(stage.widthProperty());
        VBox header = new VBox();
        header.getChildren().addAll(headerLabel, progressBar);
        header.setSpacing(10);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setTop(header);
        borderPane.setCenter(totalVBox);

        Scene scene = new Scene(borderPane);

        stage.setScene(scene);

        stage.show();

        Property<Result> finished = new SimpleObjectProperty<>();

        stage.setOnCloseRequest(event -> {
            finished.setValue(Result.Canceled);
            task.cancel();
        });

        new Thread(task).start();

        task.setOnSucceeded(event -> {
            finished.setValue(Result.Success);
            stage.close();
        });
        task.setOnFailed(event -> {
            finished.setValue(Result.Fail);
            stage.close();
        });
        task.setOnCancelled(event -> {
            finished.setValue(Result.Canceled);
            stage.close();
        });
        
        return Optional.of(finished);
    }

    private static boolean checkFistyDependency(Goo2mod mod, Goo2mod.Depends dependency) {
        Properties properties = PropertiesLoader.getProperties();
        
        if (properties.getFistyVersion() == null) {
            FX_Alert.error("Dependecy Check", String.format(
                    "Addin \"%s\" requires FistyLoader, which is not installed",
                    mod.getId()), ButtonType.OK);
            
            return false;
        }
        
        if (dependency.getMinVersion() != null && dependency.getMinVersion().compareTo(properties.getFistyVersion()) > 0) {
            FX_Alert.error("Dependecy Check", String.format(
                    "Addin \"%s\" requires FistyLoader version %s, although"
                    + " version %s is installed.",
                    mod.getId(), dependency.getMinVersion(), properties.getFistyVersion()),
                    ButtonType.OK);
            
            return false;
        }
        
        if (dependency.getMaxVersion() != null && dependency.getMaxVersion().compareTo(properties.getFistyVersion()) < 0) {
            FX_Alert.error("Dependecy Check", String.format(
                    "Addin \"%s\" requires Maximum FistyLoader version %s, although only"
                    + " version %s is installed.",
                    mod.getId(), dependency.getMaxVersion(), properties.getFistyVersion()),
                    ButtonType.OK);
            
            return false;
        }
        
        return true;
    }
    
    private static boolean checkDependency(Goo2mod mod, Goo2mod.Depends dependency, List<Goo2mod> enabledGoo2mods) {
        Goo2mod dependedAddin = null;
        
        for (Goo2mod current : enabledGoo2mods) {
            if (current.getId().equals(dependency.getId())) {
                dependedAddin = current;
                break;
            }
        }
        
        if (dependedAddin == null) {
            FX_Alert.error("Dependecy Check", String.format(
                    "Addin \"%s\" requires addin \"%s\", which is not installed",
                    mod.getId(), dependency.getId()),
                    ButtonType.OK);
            
            return false;
        }
        
        if (dependency.getMinVersion() != null && dependency.getMinVersion().compareTo(dependedAddin.getVersion()) > 0) {
            FX_Alert.error("Dependecy Check", String.format(
                    "Addin \"%s\" requires addin \"%s\" version %s, although only"
                    + " version %s is installed.",
                    mod.getId(), dependency.getId(), dependency.getMinVersion(), dependedAddin.getVersion()),
                    ButtonType.OK);
            
            return false;
        }
        
        if (dependency.getMaxVersion() != null && dependency.getMaxVersion().compareTo(dependedAddin.getVersion()) < 0) {
            FX_Alert.error("Dependecy Check", String.format(
                    "Addin \"%s\" requires addin \"%s\" with Maximum version %s, although"
                    + " version %s is installed.",
                    mod.getId(), dependency.getId(), dependency.getMaxVersion(), dependedAddin.getVersion()),
                    ButtonType.OK);
            
            return false;
        }
        
        return true;
    }
    
}
