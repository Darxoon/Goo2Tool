package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.properties.Addin;
import com.crazine.goo2tool.properties.PropertiesLoader;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Arrays;

public class FX_Mods {

    private static final VBox modView = new VBox();
    public static VBox getModView() {
        return modView;
    }


    private static final TableView<Goo2mod> modTableView = new TableView<>();
    public static TableView<Goo2mod> getModTableView() {
        return modTableView;
    }


    public static void buildModView(Stage stage) {

        modView.setPadding(new Insets(10, 10, 10, 10));
        modView.setSpacing(5);

        Button enable = new Button("Enable");
        Button disable = new Button("Disable");
        Button uninstall = new Button("Uninstall");

        TableColumn<Goo2mod, Boolean> enabled = new TableColumn<>();
        enabled.setCellFactory(param -> {

            CheckBox checkBox = new CheckBox();
            TableCell<Goo2mod, Boolean> cell = new TableCell<>() {

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    for (Addin addin : PropertiesLoader.getProperties().getAddins()) {
                        if (getTableRow().getItem() != null && addin.getName().equals(getTableRow().getItem().getId())) {
                            checkBox.setSelected(addin.isLoaded());
                            enable.setDisable(!addin.isLoaded());
                            disable.setDisable(addin.isLoaded());
                        }
                    }
                }
            };
            checkBox.setPrefSize(20, 20);
            checkBox.setOnAction(event -> {
                cell.setItem(checkBox.isSelected());
                for (int i = 0; i < PropertiesLoader.getProperties().getAddins().length; i++) {
                    Addin addin = PropertiesLoader.getProperties().getAddins()[i];
                    if (addin.getName().equals(cell.getTableView().getItems().get(cell.getIndex()).getId())) {
                        addin.setLoaded(cell.getItem());
                    }
                }
            });
            checkBox.visibleProperty().bind(cell.emptyProperty().not());

            cell.setGraphic(checkBox);

            return cell;

        });
        enabled.setMinWidth(24);
        enabled.setMaxWidth(24);
        enabled.setReorderable(false);
        enabled.setSortable(false);
        modTableView.getColumns().add(enabled);

        TableColumn<Goo2mod, String> addinName = new TableColumn<>("Addin Name");
        addinName.setCellValueFactory(new PropertyValueFactory<>("name"));
        addinName.setReorderable(false);
        addinName.setSortable(false);
        modTableView.getColumns().add(addinName);
        addinName.setPrefWidth(300);

        TableColumn<Goo2mod, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        type.setReorderable(false);
        type.setSortable(false);
        modTableView.getColumns().add(type);
        type.setPrefWidth(150);

        TableColumn<Goo2mod, Double> version = new TableColumn<>("Version");
        version.setCellValueFactory(new PropertyValueFactory<>("version"));
        version.setReorderable(false);
        version.setSortable(false);
        modTableView.getColumns().add(version);
        version.setPrefWidth(150);

        TableColumn<Goo2mod, String> author = new TableColumn<>("Author");
        author.setCellValueFactory(new PropertyValueFactory<>("author"));
        author.setReorderable(false);
        author.setSortable(false);
        modTableView.getColumns().add(author);
        author.setPrefWidth(300);

        modTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setEditable(false);

        Button propertiesButton = new Button("Properties");
        HBox hBox = new HBox();
        hBox.getChildren().add(propertiesButton);
        hBox.setAlignment(Pos.CENTER_LEFT);

        HBox box = new HBox();
        box.getChildren().addAll(enable, disable, uninstall);
        box.setAlignment(Pos.CENTER_RIGHT);

        BorderPane stackPane = new BorderPane();
        stackPane.setLeft(propertiesButton);
        stackPane.setRight(box);

        modView.getChildren().addAll(modTableView, descriptionArea, stackPane);

    }

}
