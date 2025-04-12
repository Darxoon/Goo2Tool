package com.crazine.goo2tool.gui.util;

import com.crazine.goo2tool.properties.Properties;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class LaunchOptions {
    
    private GridPane contents;
    
    public GridPane getContents() {
        return contents;
    }
    
    public LaunchOptions(Properties properties) {
        contents = new GridPane();
        contents.setHgap(8);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setMinWidth(160);
        ColumnConstraints column2 = new ColumnConstraints();
        column1.setMinWidth(10);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.ALWAYS);
        contents.getColumnConstraints().addAll(column1, column2, column3);
        
        createSetting(contents, 0, "Launch Command (optional)", properties.launchCommandProperty());
    }
    
    private void createSetting(GridPane grid, int rowIndex, String labelText, StringProperty contentProperty) {
        Label label = new Label(labelText);
        label.setPadding(new Insets(4, 0, 4, 0));
        
        Region empty = new Region();
        
        TextField contentField = new TextField();
        contentField.textProperty().bindBidirectional(contentProperty);
        contentField.setPadding(new Insets(4, 0, 4, 0));
        
        grid.addRow(rowIndex, label, empty, contentField);
    }
    
}
