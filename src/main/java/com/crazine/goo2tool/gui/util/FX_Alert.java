package com.crazine.goo2tool.gui.util;

import javafx.scene.image.Image;

import java.util.Optional;

import com.crazine.goo2tool.IconLoader;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class FX_Alert {
    
    public static Optional<ButtonType> info(String title, String content, Image icon, ButtonType... buttonTypes) {
        return show(title, content, icon, "information", buttonTypes);
    }
    
    public static Optional<ButtonType> info(String title, String content, ButtonType... buttonTypes) {
        return show(title, content, IconLoader.getConduit(), "information", buttonTypes);
    }
    
    public static Optional<ButtonType> warn(String title, String content, Image icon, ButtonType... buttonTypes) {
        return show(title, content, icon, "warning", buttonTypes);
    }
    
    public static Optional<ButtonType> warn(String title, String content, ButtonType... buttonTypes) {
        return show(title, content, IconLoader.getConduit(), "warning", buttonTypes);
    }
    
    private static Optional<ButtonType> show(String title, String content, Image icon, String styleClass, ButtonType... buttonTypes) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(content);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        dialog.getDialogPane().getStyleClass().add("alert");
        
        dialog.getDialogPane().getStyleClass().add(styleClass);
        
        if (icon != null) {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(icon);
        }
        
        return dialog.showAndWait();
    }
    
}
