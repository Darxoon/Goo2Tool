package com.crazine.goo2tool.functional;

import com.crazine.goo2tool.Main;
import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.gui.FX_Alarm;
import com.crazine.goo2tool.gui.FX_Profile;
import com.crazine.goo2tool.islands.IslandFileLoader;
import com.crazine.goo2tool.islands.Islands;
import com.crazine.goo2tool.properties.Addin;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.saveFile.SaveFileLoader;
import com.crazine.goo2tool.saveFile.WOG2SaveData;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class Save {

    public static BooleanProperty save(Stage originalStage) {

        Stage stage = new Stage();
        stage.initOwner(originalStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        Task<Void> task = new Task<>() {

            @Override
            protected Void call() {

                // Export properties
                try {
                    PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
                } catch (IOException e) {
                    FX_Alarm.error(e);
                }

                // Merge original res folder
                updateTitle("Validating original WoG2");
                String baseWOG2 = PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory();
                String customWOG2 = PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory();
                File baseFile = new File(baseWOG2);

                long fileCount;

                try (Stream<Path> stream = Files.walk(baseFile.toPath())) {
                    fileCount = stream.parallel()
                            .filter(p -> !p.toFile().isDirectory())
                            .count();
                } catch (IOException e) {
                    FX_Alarm.error(e);
                    fileCount = 0;
                }

                Stack<File> baseFiles = new Stack<>();
                baseFiles.add(baseFile);
                long tracker = 0;
                while (!baseFiles.isEmpty()) {

                    tracker++;

                    updateProgress(tracker, fileCount);

                    File file = baseFiles.pop();
                    if (file.isDirectory()) {
                        File[] fileArray = file.listFiles();
                        assert fileArray != null;
                        baseFiles.addAll(Arrays.asList(fileArray));
                    }

                    String uniquePath = file.getPath().substring(baseWOG2.length());
                    Path customPath = Path.of(customWOG2 + uniquePath);

                    if (uniquePath.length() > 10) updateMessage(uniquePath.substring(10));

                    boolean shouldReplace = !Files.exists(customPath);
                    if (!shouldReplace) {
                        if (file.isDirectory() || !file.getPath().contains("game/res/") &&  !file.getPath().contains("game\\res\\")) continue;
                        try {
                            if (Files.mismatch(customPath, file.toPath()) != -1L) {
                                shouldReplace = true;
                            }
                        } catch (IOException e) {
                            FX_Alarm.error(e);
                        }
                    }

                    // If the file doesn't exist in the new res folder, copy it over
                    if (shouldReplace) {
                        try {
                            Files.copy(file.toPath(), customPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            FX_Alarm.error(e);
                        }
                    }

                }

                ArrayList<Goo2mod> goo2mods = new ArrayList<>();
                File goo2modsDirectory = new File(PropertiesLoader.getGoo2ToolPath() + "/addins");
                File[] files = goo2modsDirectory.listFiles();
                if (files != null) for (File file : files) {
                    try {
                        goo2mods.add(AddinFileLoader.loadGoo2mod(file));
                    } catch (IOException e) {
                        FX_Alarm.error(e);
                    }
                }


                ArrayList<Goo2mod> goo2modsSorted = new ArrayList<>();
                for (int i = PropertiesLoader.getProperties().getAddins().size() - 1; i >= 0; i--) {
                    Addin addin = PropertiesLoader.getProperties().getAddins().get(i);
                    if (!addin.isLoaded()) continue;
                    for (Goo2mod goo2mod : goo2mods) {
                        if (goo2mod.getId().equals(addin.getName())) {
                            goo2modsSorted.add(goo2mod);
                            break;
                        }
                    }
                }

                for (Goo2mod goo2mod : goo2modsSorted) {

                    updateTitle("Installing addin " + goo2mod.getId());

                    try (ZipFile addinFile = new ZipFile(goo2mod.getFile().getPath())) {

                        long count = addinFile.stream().count();

                        AtomicLong i = new AtomicLong();

                        addinFile.entries().asIterator().forEachRemaining(zipEntry -> {
                            i.getAndIncrement();
                            updateProgress(i.get(), count);

                            if (!zipEntry.getName().contains("/") || zipEntry.getName().indexOf("/",
                                    zipEntry.getName().indexOf("/") + 1) == -1) return;

                            if (zipEntry.isDirectory()) return;

                            String uniquePath = zipEntry.getName().substring(zipEntry.getName()
                                    .indexOf("/", zipEntry.getName().indexOf("/") + 1));
                            Path customPath = Path.of(customWOG2 + "/game" + uniquePath);


                            if (uniquePath.length() > 1) updateMessage(uniquePath.substring(1));

                            // If the file doesn't exist in the new res folder, create it
                            if (!Files.exists(customPath)) {
                                try {
                                    Files.createFile(customPath);
                                } catch (IOException e) {
                                    FX_Alarm.error(e);
                                }
                            }

                            try {
                                if (Files.isWritable(customPath)) {
                                    Files.copy(addinFile.getInputStream(zipEntry), customPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                FX_Alarm.error(e);
                            }

                        });

                    } catch (IOException e) {
                        FX_Alarm.error(e);
                    }

                }

                // Update save file
                File islandFile = new File(customWOG2 + "/game/res/islands/islands.wog2");
                try {

                    updateTitle("Updating save file ");
                    updateMessage("");

                    Islands islands = IslandFileLoader.loadIslands(islandFile);

                    File toSaveFile = new File(PropertiesLoader.getProperties().getProfileDirectory() + "/wog2_1.dat");

                    try {

                        WOG2SaveData[] data = SaveFileLoader.readSaveFile(toSaveFile);
                        WOG2SaveData wog2SaveData = data[FX_Profile.getProfileSelectionBox().getSelectionModel().getSelectedIndex()];

                        for (int i = 0; i < islands.getIslands().length; i++) {

                            int numLevels = islands.getIslands()[i].getLevels().length;

                            for (int j = 0; j < numLevels; j++) {
                                wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setValid(true);
                                wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setLocked(false);
                            }
                            for (int j = numLevels; j < 20; j++) {
                                wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setValid(false);
                                wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setLocked(true);
                            }

                        }

                        SaveFileLoader.writeSaveFile(toSaveFile, data);

                    } catch (IOException e) {
                        FX_Alarm.error(e);
                    }

                } catch (IOException e) {
                    FX_Alarm.error(e);
                }

                return null;
            }

        };

        stage.setWidth(400);
        stage.setHeight(200);

        stage.setTitle("Building your World of Goo 2");

        File codeLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        while (!codeLocation.getName().equals("Goo2Tool") && !codeLocation.getName().equals("Goo2Tool-master")) {
            codeLocation = codeLocation.getParentFile();
        }
        String projectLocation = codeLocation.getPath().replaceAll("\\\\", "/");
        Image image = new Image(projectLocation + "/terrain.png");
        stage.getIcons().add(image);

        stage.setAlwaysOnTop(true);


        Label contentLabel = new Label();
        contentLabel.textProperty().bind(task.messageProperty());
        contentLabel.setAlignment(Pos.CENTER);
        contentLabel.setWrapText(true);
        contentLabel.setTextAlignment(TextAlignment.CENTER);

        VBox totalVBox = new VBox();
        totalVBox.getChildren().addAll(contentLabel);
        totalVBox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label();
        headerLabel.textProperty().bind(new SimpleStringProperty("Current task: ").concat(task.titleProperty()));
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.prefWidthProperty().bind(stage.widthProperty());
        VBox header = new VBox();
        header.getChildren().addAll(headerLabel, progressBar);
        header.setSpacing(10);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setTop(header);
        borderPane.setCenter(totalVBox);

        Scene scene = new Scene(borderPane);

        stage.setScene(scene);

        stage.show();
        stage.setAlwaysOnTop(true);

        BooleanProperty finished = new SimpleBooleanProperty();

        stage.setOnCloseRequest(event -> {
            finished.set(true);
            task.cancel();
        });

        new Thread(task).start();

        task.setOnSucceeded(event -> {
            finished.set(true);
            stage.close();
        });
        task.setOnFailed(event -> {
            finished.set(true);
            stage.close();
        });
        task.setOnCancelled(event -> {
            finished.set(true);
            stage.close();
        });

        return finished;

    }

}
