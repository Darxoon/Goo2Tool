package com.crazine.goo2tool.functional;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Stream;
import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.AddinReader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.addinFile.AddinReader.Resource;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.ResArchive.ResFile;
import com.crazine.goo2tool.gamefiles.islands.IslandFileLoader;
import com.crazine.goo2tool.gamefiles.islands.Islands;
import com.crazine.goo2tool.gui.FX_Alarm;
import com.crazine.goo2tool.gui.FX_Profile;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.saveFile.SaveFileLoader;
import com.crazine.goo2tool.saveFile.WOG2SaveData;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;

class SaveTask extends Task<Void> {
    private final Stage stage;

    SaveTask(Stage stage) {
        this.stage = stage;
    }

    @Override
    protected Void call() {
        try (ResArchive res = ResArchive.loadOrSetupVanilla(stage)) {
            save(res);
        } catch (Exception e) {
            Platform.runLater(() -> {
                FX_Alarm.error(e);
            });
        }
        return (Void) null;
    }
    
    private void save(ResArchive res) throws Exception {
        
        // Export properties
        PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());

        // Merge original res folder
        updateTitle("Validating original WoG2");
        copyMissingOriginalFiles(res);
        
        // TODO: make goo2tool restore files that were overwritten by addins
        // try {
        //     int i = 0;
        //     for (ResArchive.ResFile file : res.getAllFiles()) {
        //         updateProgress(i++, res.fileCount());
                
        //         boolean shouldReplace = true;
        //         File customFile = new File(customWOG2 + "/game/" + file.path());
                
        //         if (customFile.exists()) {
        //             try (FileInputStream stream = new FileInputStream(customFile)) {
        //                 byte[] newContent = stream.readAllBytes();
        //                 shouldReplace = Arrays.equals(content, newContent);
        //             }
        //         }
                
        //         if (shouldReplace) {
        //             Files.createDirectories(customFile.toPath().getParent());
        //             Files.write(customFile.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        //         }
        //     }
        // } catch (IOException e) {
        //     FX_Alarm.error(e);
        // }

        // Retrieve mods
        ArrayList<Goo2mod> goo2mods = new ArrayList<>();
        File goo2modsDirectory = new File(PropertiesLoader.getGoo2ToolPath() + "/addins");
        File[] files = goo2modsDirectory.listFiles();
        if (files != null) for (File file : files) {
            goo2mods.add(AddinFileLoader.loadGoo2mod(file));
        }


        ArrayList<Goo2mod> goo2modsSorted = new ArrayList<>();
        for (int i = PropertiesLoader.getProperties().getAddins().size() - 1; i >= 0; i--) {
            AddinConfigEntry addin = PropertiesLoader.getProperties().getAddins().get(i);
            if (!addin.isLoaded()) continue;
            for (Goo2mod goo2mod : goo2mods) {
                if (goo2mod.getId().equals(addin.getName())) {
                    goo2modsSorted.add(goo2mod);
                    break;
                }
            }
        }

        // Install mods
        for (Goo2mod goo2mod : goo2modsSorted) {
            updateTitle("Installing addin " + goo2mod.getId());
            installGoo2mod(goo2mod);
        }

        // Update save file
        updateTitle("Updating save file ");
        updateMessage("");

        Islands islands = IslandFileLoader.loadIslands(res);

        File toSaveFile = new File(PropertiesLoader.getProperties().getProfileDirectory() + "/wog2_1.dat");

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
    }
    
    private void copyMissingOriginalFiles(ResArchive res) throws IOException {
        String baseWOG2 = PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory();
        String customWOG2 = PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory();
        File baseFile = new File(baseWOG2);

        // count files (for progress reporting)
        long fileCount;

        try (Stream<Path> stream = Files.walk(baseFile.toPath())) {
            fileCount = stream.parallel()
                    .filter(p -> !p.toFile().isDirectory())
                    .count() + res.fileCount();
        } catch (IOException e) {
            Platform.runLater(() -> {
                FX_Alarm.error(e);
            });
            fileCount = 0;
        }

        // traverse actual base folder
        long tracker = 0;
        
        Stack<File> baseFiles = new Stack<>();
        baseFiles.add(baseFile);
        
        while (!baseFiles.isEmpty()) {

            tracker++;

            updateProgress(tracker, fileCount);

            File file = baseFiles.pop();
            
            String uniquePath = file.getPath().substring(Math.min(file.getPath().length(), baseWOG2.length() + 1));
            Path customPath = Paths.get(customWOG2, uniquePath);
            
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                assert fileArray != null;
                
                if (!uniquePath.replaceAll("\\\\", "/").equals("game")) {
                    baseFiles.addAll(Arrays.asList(fileArray));
                }
            }

            updateMessage(uniquePath);

            boolean shouldReplace = !Files.exists(customPath);
            if (!shouldReplace) {
                if (file.isDirectory() || !file.getPath().contains("game/res/") &&  !file.getPath().contains("game\\res\\")) continue;
                if (Files.mismatch(customPath, file.toPath()) != -1L) {
                    shouldReplace = true;
                }
            }

            // If the file doesn't exist in the new res folder, copy it over
            if (shouldReplace) {
                Files.copy(file.toPath(), customPath, StandardCopyOption.REPLACE_EXISTING);
            }

        }
        
        // traverse res folder
        for (ResFile file : res.getAllFiles()) {
            tracker++;
            updateProgress(tracker, fileCount);
            updateMessage("game/" + file.path());
            
            Path customPath = Paths.get(customWOG2, "game", file.path());
            
            if (!Files.exists(customPath)) {
                Files.createDirectories(customPath.getParent());
                try {
                    Files.write(customPath, file.content(), StandardOpenOption.CREATE_NEW);
                } catch (FileAlreadyExistsException e) {}
            }
        }
    }
    
    private void installGoo2mod(Goo2mod mod) {
        String customWOG2 = PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory();
        
        try (AddinReader addinFile = new AddinReader(mod)) {

            long count = addinFile.getFileCount();
            long i = 0;

            for (Resource resource : addinFile.getAllFiles()) {
                i++;
                updateProgress(i, count);
                
                switch (resource.type()) {
                    case METADATA:
                        break;
                    case COMPILE:
                        throw new IllegalArgumentException("compile/ is not supported yet");
                    case MERGE:
                        throw new IllegalArgumentException("merge/ is not supported yet");
                    case OVERRIDE: {
                        Path customPath = Paths.get(customWOG2, "game", resource.path());
                        
                        if (resource.path().length() > 1) updateMessage(resource.path().substring(1));
                        
                        Files.write(customPath, resource.content(),
                                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            FX_Alarm.error(new RuntimeException("Failed loading the mod " + mod.getName() + ": " + e.getMessage(), e));
        }
    }
    
}