package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.gui.util.FileOptions;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FX_Options {

    private static final VBox optionsView = new VBox();
    public static VBox getOptionsView() {
        return optionsView;
    }


    public static void buildOptionsView(Stage stage) {

        VBox fileLocationsVBox = new FileOptions(stage, false).getContents();
        optionsView.getChildren().add(fileLocationsVBox);

        optionsView.setPadding(new Insets(10, 10, 10, 10));

    }


}
