package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.saveFile.WOG2SaveData;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FX_Profile {

    private static final VBox profileView = new VBox();
    public static VBox getProfileView() {
        return profileView;
    }


    private static final TableView<WOG2SaveData.WOG2SaveFileLevel> levelTableView = new TableView<>();
    public static TableView<WOG2SaveData.WOG2SaveFileLevel> getLevelTableView() {
        return levelTableView;
    }


    public static void buildProfileView(Stage stage) {

        profileView.prefHeightProperty().bind(stage.heightProperty());
        profileView.setPadding(new Insets(10, 10, 10, 10));
        profileView.setSpacing(5);


        FlowPane profileHeaderPane = new FlowPane();
        profileHeaderPane.prefWidthProperty().bind(stage.widthProperty());
        profileHeaderPane.setPrefHeight(30);


        Label selectProfile = new Label("Select Profile:");
        profileHeaderPane.getChildren().add(selectProfile);
        ComboBox<String> profileSelectionBox = new ComboBox<>();
        for (int i = 0; i < 3; i++) {
            profileSelectionBox.getItems().add("Profile " + i);
        }
        profileSelectionBox.getSelectionModel().select(0 );
        profileHeaderPane.getChildren().add(profileSelectionBox);


        Button refreshButton = new Button("Refresh");
        profileHeaderPane.getChildren().add(refreshButton);


        profileView.getChildren().add(profileHeaderPane);



        levelTableView.setRowFactory(param -> {
            TableRow<WOG2SaveData.WOG2SaveFileLevel> row = new TableRow<>();
            return row;
        });

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, String> levelNameColumn = new TableColumn<>("Level");
        levelNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<WOG2SaveData.WOG2SaveFileLevel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<WOG2SaveData.WOG2SaveFileLevel, String> param) {

                return new ObservableValueBase<String>() {
                    @Override
                    public String getValue() {
                        return "Level";
                    }
                };

            }
        });
        levelTableView.getColumns().add(levelNameColumn);


        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> mostBallsColumn = new TableColumn<>("Most Balls");
        mostBallsColumn.setCellValueFactory(new PropertyValueFactory<>("bestBalls"));
        levelTableView.getColumns().add(mostBallsColumn);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> leastMovesColumn = new TableColumn<>("Least Moves");
        leastMovesColumn.setCellValueFactory(new PropertyValueFactory<>("bestMoves"));
        levelTableView.getColumns().add(leastMovesColumn);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> leastTimeColumn = new TableColumn<>("Least Time");
        leastTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bestTime"));
        levelTableView.getColumns().add(leastTimeColumn);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> totalTimeColumn = new TableColumn<>("Total Time");
        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        levelTableView.getColumns().add(totalTimeColumn);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> attemptsColumn = new TableColumn<>("Attempts");
        attemptsColumn.setCellValueFactory(new PropertyValueFactory<>("attempts"));
        levelTableView.getColumns().add(attemptsColumn);

        profileView.getChildren().add(levelTableView);

    }

}
