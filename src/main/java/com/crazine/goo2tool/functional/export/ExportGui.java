package com.crazine.goo2tool.functional.export;

import java.nio.file.Path;
import java.util.Optional;

import com.crazine.goo2tool.gui.export.FX_ExportDialog.AddinInfo;
import com.crazine.goo2tool.util.IconLoader;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExportGui {

    public static enum Result {
        Success,
        Fail,
        Canceled,
    }
    
    public static Optional<Property<Result>> exportLevel(Stage originalStage, AddinInfo addinInfo, Path levelPath, Path outPath) {
        // Loading Screen
        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setWidth(400);
        stage.setHeight(200);

        stage.setTitle("Exporting level");

        stage.getIcons().add(IconLoader.getTerrain());

        stage.setAlwaysOnTop(true);

        ExportTask task = new ExportTask(stage, addinInfo, levelPath, outPath);
        
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

}
