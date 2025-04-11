package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.gui.util.FileOptions;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FX_Setup extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Goo2Tool Setup");

        VBox fileLocationsVBox = new FileOptions(stage, true).getContents();
        fileLocationsVBox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(fileLocationsVBox);

        stage.setScene(scene);
        stage.show();
    }

}
