package com.crazine.goo2tool.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class FX_Alarm {

    public static void error(Exception e) {

        e.printStackTrace();
        Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setContentText(e.toString());
        dialog.show();

    }

}
