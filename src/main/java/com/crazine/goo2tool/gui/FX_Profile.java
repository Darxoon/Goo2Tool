package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.islands.IslandFileLoader;
import com.crazine.goo2tool.islands.Islands;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.saveFile.SaveFileLoader;
import com.crazine.goo2tool.saveFile.WOG2SaveData;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FX_Profile {

    private static final VBox profileView = new VBox();
    public static VBox getProfileView() {
        return profileView;
    }


    private static final TableView<WOG2SaveData.WOG2SaveFileLevel> levelTableView = new TableView<>();
    public static TableView<WOG2SaveData.WOG2SaveFileLevel> getLevelTableView() {
        return levelTableView;
    }


    private static final ComboBox<String> profileSelectionBox = new ComboBox<>();
    public static ComboBox<String> getProfileSelectionBox() {
        return profileSelectionBox;
    }


    public static void buildProfileView(Stage stage) {

        profileView.prefHeightProperty().bind(stage.heightProperty());
        profileView.setPadding(new Insets(10, 10, 10, 10));
        profileView.setSpacing(5);

        levelTableView.prefHeightProperty().bind(profileView.heightProperty());

        BorderPane profileHeaderPane = new BorderPane();
        profileHeaderPane.prefWidthProperty().bind(stage.widthProperty());
        profileHeaderPane.setPrefHeight(30);

        Label selectProfile = new Label("Select Profile:");
        for (int i = 0; i < 3; i++) {
            profileSelectionBox.getItems().add("Profile " + i);
        }
        profileSelectionBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            File toSaveFile = new File(PropertiesLoader.getProperties().getProfileDirectory() + "/wog2_1.dat");
            try {

                Islands islands = IslandFileLoader.loadIslands(stage);

                WOG2SaveData[] data = SaveFileLoader.readSaveFile(toSaveFile);
                levelTableView.getItems().clear();
                int j = 0;
                for (WOG2SaveData.WOG2SaveFileIsland island : data[newValue.intValue()].getSaveFile().getIslands()) {
                    int i = 0;
                    for (WOG2SaveData.WOG2SaveFileLevel level : island.getLevels()) {
                        if (!level.isValid()) continue;

                        if (islands.getIslands()[j].getLevels().length <= i) continue;

                        level.setName(islands.getIslands()[j].getLevels()[i].getLevel());

                        levelTableView.getItems().add(level);

                        i++;
                    }
                    j++;
                }

            } catch (IOException e) {
                FX_Alarm.error(e);
            }
        });
        profileSelectionBox.getSelectionModel().select(0);

        HBox profileBox = new HBox();
        profileBox.getChildren().addAll(selectProfile, profileSelectionBox);
        profileBox.setSpacing(5);

        profileHeaderPane.setLeft(profileBox);

        Button refreshButton = new Button("Refresh");
        profileHeaderPane.setRight(refreshButton);

        profileView.getChildren().add(profileHeaderPane);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, String> levelNameColumn = new TableColumn<>("Level");
        levelNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelNameColumn.setReorderable(false);
        levelTableView.getColumns().add(levelNameColumn);
        levelNameColumn.setPrefWidth(400);


        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> mostBallsColumn = new TableColumn<>("Most Balls");
        mostBallsColumn.setCellValueFactory(new PropertyValueFactory<>("bestBalls"));
        mostBallsColumn.setReorderable(false);
        levelTableView.getColumns().add(mostBallsColumn);
        mostBallsColumn.setPrefWidth(100);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> leastMovesColumn = new TableColumn<>("Least Moves");
        leastMovesColumn.setCellValueFactory(new PropertyValueFactory<>("bestMoves"));
        leastMovesColumn.setReorderable(false);
        levelTableView.getColumns().add(leastMovesColumn);
        leastMovesColumn.setPrefWidth(100);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> leastTimeColumn = new TableColumn<>("Least Time");
        leastTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bestTime"));
        leastTimeColumn.setReorderable(false);
        levelTableView.getColumns().add(leastTimeColumn);
        leastTimeColumn.setPrefWidth(100);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> totalTimeColumn = new TableColumn<>("Total Time");
        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        totalTimeColumn.setReorderable(false);
        levelTableView.getColumns().add(totalTimeColumn);
        totalTimeColumn.setPrefWidth(100);

        TableColumn<WOG2SaveData.WOG2SaveFileLevel, Integer> attemptsColumn = new TableColumn<>("Attempts");
        attemptsColumn.setCellValueFactory(new PropertyValueFactory<>("attempts"));
        attemptsColumn.setReorderable(false);
        levelTableView.getColumns().add(attemptsColumn);
        attemptsColumn.setPrefWidth(100);

        levelTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        levelNameColumn.setMaxWidth(10000);

        profileView.getChildren().add(levelTableView);

    }

}
