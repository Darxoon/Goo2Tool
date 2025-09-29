package com.crazine.goo2tool.gui.util;

import javafx.scene.image.Image;

import java.util.Optional;

import com.crazine.goo2tool.util.IconLoader;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class FX_Alert {
    
    public static Optional<ButtonType> info(String title, String content, Optional<ButtonType> defaultButton, ButtonType... buttonTypes) {
        return show(title, content, IconLoader.getConduit(), "information", defaultButton, buttonTypes);
    }
    
    public static Optional<ButtonType> info(String title, String content, ButtonType... buttonTypes) {
        return show(title, content, IconLoader.getConduit(), "information", Optional.empty(), buttonTypes);
    }
    
    public static Optional<ButtonType> warn(String title, String content, Optional<ButtonType> defaultButton, ButtonType... buttonTypes) {
        return show(title, content, IconLoader.getConduit(), "warning", defaultButton, buttonTypes);
    }
    
    public static Optional<ButtonType> warn(String title, String content, ButtonType... buttonTypes) {
        return show(title, content, IconLoader.getConduit(), "warning", Optional.empty(), buttonTypes);
    }
    
    private static Optional<ButtonType> show(String title, String content, Image icon, String styleClass,
            Optional<ButtonType> defaultButtonType, ButtonType... buttonTypes) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(content);
        
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        
        if (defaultButtonType.isPresent()) {
            Platform.runLater(() -> {
                for (ButtonType buttonType : buttonTypes) {
                    if (buttonType == defaultButtonType.get())
                        continue;
                    
                    Button button = (Button) dialog.getDialogPane().lookupButton(buttonType);
                    button.setDefaultButton(false);
                }
                
                dialog.getDialogPane().lookupButton(defaultButtonType.get()).requestFocus();;
            });
        }
        
        dialog.getDialogPane().getStyleClass().add("alert");
        dialog.getDialogPane().getStyleClass().add(styleClass);
        
        if (icon != null) {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(icon);
        }
        
        return dialog.showAndWait();
    }
    
}
