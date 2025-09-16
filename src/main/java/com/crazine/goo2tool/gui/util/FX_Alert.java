package com.crazine.goo2tool.gui.util;

import javafx.scene.image.Image;

import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class FX_Alert {
    
    public static Optional<ButtonType> show(String title, String content, Image icon, ButtonType... buttonTypes) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(content);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        dialog.getDialogPane().getStyleClass().add("alert");
        
        // TODO: make this customizable
        dialog.getDialogPane().getStyleClass().add("information");
        
        if (icon != null) {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(icon);
        }
        
        return dialog.showAndWait();
    }
    
    public static Optional<ButtonType> show(String title, String content, ButtonType... buttonTypes) {
        return show(title, content, null, buttonTypes);
    }
    
}
