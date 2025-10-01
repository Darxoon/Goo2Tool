package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.islands.IslandFileLoader;
import com.crazine.goo2tool.gamefiles.islands.Islands;
import com.crazine.goo2tool.gamefiles.savefile.SaveFileLoader;
import com.crazine.goo2tool.gamefiles.savefile.WOG2SaveData;
import com.crazine.goo2tool.gamefiles.savefile.WOG2SaveData.WOG2SaveFileLevel;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;

public class FX_Profile {
    
    private static class CompletedLevelFactory implements Callback<CellDataFeatures<WOG2SaveFileLevel,String>, ObservableValue<String>> {

        private String propertyName;
        
        public CompletedLevelFactory(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public ObservableValue<String> call(CellDataFeatures<WOG2SaveFileLevel, String> level) {
            if (level.getValue().getCompleteState() != 0) {
                PropertyValueFactory<WOG2SaveFileLevel, String> factory = new PropertyValueFactory<>(propertyName);
                return factory.call(level);
            } else {
                return new ReadOnlyStringWrapper("-");
            }
        }
        
    }

    private static final VBox profileView = new VBox();
    public static VBox getProfileView() {
        return profileView;
    }


    private static final TableView<WOG2SaveFileLevel> levelTableView = new TableView<>();
    public static TableView<WOG2SaveFileLevel> getLevelTableView() {
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
            // TODO: Update this when saveFilePath changes
            if (PropertiesLoader.getProperties().getSaveFilePath() == null)
                return;
            
            File toSaveFile = new File(PropertiesLoader.getProperties().getSaveFilePath());
            try (ResArchive res = ResArchive.loadOrSetupVanilla(stage)) {
                Islands islands = IslandFileLoader.loadIslands(res);

                WOG2SaveData[] data = SaveFileLoader.readSaveFile(toSaveFile);
                levelTableView.getItems().clear();
                int j = 0;
                for (WOG2SaveData.WOG2SaveFileIsland island : data[newValue.intValue()].getSaveFile().getIslands()) {
                    int i = 0;
                    for (WOG2SaveFileLevel level : island.getLevels()) {
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

        TableColumn<WOG2SaveFileLevel, String> levelNameColumn = new TableColumn<>("Level");
        levelNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelNameColumn.setReorderable(false);
        levelTableView.getColumns().add(levelNameColumn);
        levelNameColumn.setPrefWidth(400);


        TableColumn<WOG2SaveFileLevel, String> mostBallsColumn = new TableColumn<>("Most Balls");
        mostBallsColumn.setCellValueFactory(new CompletedLevelFactory("bestBalls"));
        mostBallsColumn.setReorderable(false);
        levelTableView.getColumns().add(mostBallsColumn);
        mostBallsColumn.setPrefWidth(100);

        TableColumn<WOG2SaveFileLevel, String> leastMovesColumn = new TableColumn<>("Least Moves");
        leastMovesColumn.setCellValueFactory(new CompletedLevelFactory("bestMoves"));
        leastMovesColumn.setReorderable(false);
        levelTableView.getColumns().add(leastMovesColumn);
        leastMovesColumn.setPrefWidth(100);

        TableColumn<WOG2SaveFileLevel, String> leastTimeColumn = new TableColumn<>("Least Time");
        leastTimeColumn.setCellValueFactory(new CompletedLevelFactory("bestTime"));
        leastTimeColumn.setReorderable(false);
        levelTableView.getColumns().add(leastTimeColumn);
        leastTimeColumn.setPrefWidth(100);

        TableColumn<WOG2SaveFileLevel, String> totalTimeColumn = new TableColumn<>("Total Time");
        totalTimeColumn.setCellValueFactory(new CompletedLevelFactory("totalTime"));
        totalTimeColumn.setReorderable(false);
        levelTableView.getColumns().add(totalTimeColumn);
        totalTimeColumn.setPrefWidth(100);

        TableColumn<WOG2SaveFileLevel, Integer> attemptsColumn = new TableColumn<>("Attempts");
        attemptsColumn.setCellValueFactory(new PropertyValueFactory<>("attempts"));
        attemptsColumn.setReorderable(false);
        levelTableView.getColumns().add(attemptsColumn);
        attemptsColumn.setPrefWidth(100);

        // TODO: is this the best resize policy?
        levelTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        levelNameColumn.setMaxWidth(10000);

        profileView.getChildren().add(levelTableView);

    }

}
