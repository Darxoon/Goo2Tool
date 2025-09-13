package com.crazine.goo2tool.functional;

import java.io.File;
import java.util.Optional;

import com.crazine.goo2tool.IconLoader;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gui.util.CustomAlert;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SaveGui {

    public static Optional<BooleanProperty> save(Stage originalStage, ResArchive res) {
        Properties properties = PropertiesLoader.getProperties();
        
        if (!properties.isSteam() && properties.getCustomWorldOfGoo2Directory().isEmpty()) {
            Optional<ButtonType> result = CustomAlert.show("Goo2Tool", """
                    Goo2Tool does not modify your existing World of Goo 2 installation.
                    Instead, it copies everything into its own directory first.
                    
                    Please create a new, empty folder in the next dialog or select
                    an existing Goo2Tool directory if you have done this before.
                    """, IconLoader.getConduit(), ButtonType.OK, ButtonType.CANCEL);
            
            if (result.isEmpty() || result.get() != ButtonType.OK)
                return Optional.empty();
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File customDir = directoryChooser.showDialog(originalStage);
            if (customDir == null)
                return Optional.empty();
            properties.setCustomWorldOfGoo2Directory(customDir.getAbsolutePath());
        }
        
        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setWidth(400);
        stage.setHeight(200);

        stage.setTitle("Building your World of Goo 2");

        stage.getIcons().add(IconLoader.getTerrain());

        stage.setAlwaysOnTop(true);

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
        stage.setAlwaysOnTop(true);

        BooleanProperty finished = new SimpleBooleanProperty();

        stage.setOnCloseRequest(event -> {
            finished.set(true);
            task.cancel();
        });

        new Thread(task).start();

        task.setOnSucceeded(event -> {
            finished.set(true);
            stage.close();
        });
        task.setOnFailed(event -> {
            finished.set(true);
            stage.close();
        });
        task.setOnCancelled(event -> {
            finished.set(true);
            stage.close();
        });
        
        return Optional.of(finished);
    }

}
