package com.crazine.goo2tool.gui.util;

import com.crazine.goo2tool.util.IconLoader;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class FX_Alarm {

    public static void error(Exception e) {

        e.printStackTrace();
        Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setContentText(e.toString());
        
        if (IconLoader.getConduit() != null) {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(IconLoader.getConduit());
        }
        
        dialog.show();

    }

}
