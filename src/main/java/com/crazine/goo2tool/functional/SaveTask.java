package com.crazine.goo2tool.functional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;
import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.AddinReader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.addinFile.AddinReader.Resource;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.ResArchive.ResFile;
import com.crazine.goo2tool.gamefiles.filetable.ResFileTable;
import com.crazine.goo2tool.gamefiles.filetable.ResFileTableLoader;
import com.crazine.goo2tool.gamefiles.filetable.ResFileTable.OverriddenFileEntry;
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.gamefiles.resrc.ResrcLoader;
import com.crazine.goo2tool.gamefiles.resrc.ResrcManifest;
import com.crazine.goo2tool.gamefiles.translation.GameString;
import com.crazine.goo2tool.gamefiles.translation.TextDB;
import com.crazine.goo2tool.gamefiles.translation.TextLoader;
import com.crazine.goo2tool.gamefiles.translation.GameString.LocaleText;
import com.crazine.goo2tool.gui.FX_Alarm;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
        
        Properties properties = PropertiesLoader.getProperties();
        
        // Export properties
        PropertiesLoader.saveProperties();

        // Figure out which files are owned by mods that have been disabled
        // so they can be overwritten with vanilla assets
        List<String> disabledAddinIds = new ArrayList<>();
        for (AddinConfigEntry addin : properties.getAddins()) {
            if (!addin.isLoaded())
                disabledAddinIds.add(addin.getId());
        }
        
        Path fileTablePath = Paths.get(PropertiesLoader.getGoo2ToolPath(), "fileTable.xml");
        ResFileTable table = ResFileTableLoader.loadOrInit(fileTablePath);
        
        // Merge original res folder
        updateTitle("Validating original WoG2");
        if (properties.isSteam()) {
            // the Steam version doesn't use a dedicated custom directory
            // so copying all of the meta files is unnecessary
            extractRes(res, table, 0, res.fileCount());
        } else {
            copyMissingOriginalFiles(res, table);
        }

        // Retrieve mods
        ArrayList<Goo2mod> goo2mods = new ArrayList<>();
        File goo2modsDirectory = new File(PropertiesLoader.getGoo2ToolPath() + "/addins");
        File[] files = goo2modsDirectory.listFiles();
        if (files != null) for (File file : files) {
            goo2mods.add(AddinFileLoader.loadGoo2mod(file));
        }

        ArrayList<Goo2mod> goo2modsSorted = new ArrayList<>();
        for (int i = properties.getAddins().size() - 1; i >= 0; i--) {
            AddinConfigEntry addin = properties.getAddins().get(i);
            if (!addin.isLoaded()) continue;
            for (Goo2mod goo2mod : goo2mods) {
                if (goo2mod.getId().equals(addin.getId())) {
                    goo2modsSorted.add(goo2mod);
                    break;
                }
            }
        }

        // Install mods
        for (Goo2mod goo2mod : goo2modsSorted) {
            updateTitle("Installing addin " + goo2mod.getId());
            installGoo2mod(goo2mod, table);
        }
        
        ResFileTableLoader.save(table, fileTablePath.toFile());

        // Backup save file just for good measure
        Path saveFile = Paths.get(properties.getSaveFilePath());
        Path saveFileBackup = Paths.get(properties.getSaveFilePath().replace(".dat", "_backup.dat"));
        Path saveFileBackup2 = Paths.get(properties.getSaveFilePath().replace(".dat", "_backup2.dat"));
        
        if (Files.isRegularFile(saveFileBackup)) {
            Files.copy(saveFileBackup, saveFileBackup2, StandardCopyOption.REPLACE_EXISTING);
        }
        
        Files.copy(saveFile, saveFileBackup, StandardCopyOption.REPLACE_EXISTING);
        
        // Update save file
        // updateTitle("Updating save file ");
        // updateMessage("");

        // Islands islands = IslandFileLoader.loadIslands(res);

        // File toSaveFile = new File(properties.getProfileDirectory() + "/wog2_1.dat");

        // WOG2SaveData[] data = SaveFileLoader.readSaveFile(toSaveFile);
        // WOG2SaveData wog2SaveData = data[FX_Profile.getProfileSelectionBox().getSelectionModel().getSelectedIndex()];

        // for (int i = 0; i < islands.getIslands().length; i++) {

        //     int numLevels = islands.getIslands()[i].getLevels().length;

        //     for (int j = 0; j < numLevels; j++) {
        //         wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setValid(true);
        //         wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setLocked(false);
        //     }
        //     for (int j = numLevels; j < 20; j++) {
        //         wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setValid(false);
        //         wog2SaveData.getSaveFile().getIslands()[i].getLevels()[j].setLocked(true);
        //     }

        // }

        // SaveFileLoader.writeSaveFile(toSaveFile, data);
    }
    
    private void copyMissingOriginalFiles(ResArchive res, ResFileTable garbageFiles) throws IOException {
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
        extractRes(res, garbageFiles, tracker, fileCount);
    }
    
    private void extractRes(ResArchive res, ResFileTable garbageFiles, long tracker, long fileCount) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        
        String customWOG2 = properties.isSteam()
            ? properties.getBaseWorldOfGoo2Directory()
            : properties.getCustomWorldOfGoo2Directory();
        
        for (ResFile file : res.getAllFiles()) {
            tracker++;
            updateProgress(tracker, fileCount);
            updateMessage("game/" + file.path());
            
            Path customPath = Paths.get(customWOG2, "game", file.path());
            
            if (garbageFiles.hasEntry(file.path())) {
                garbageFiles.removeEntry(file.path());
                Files.createDirectories(customPath.getParent());
                Files.write(customPath, file.content(), StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } else if (!Files.exists(customPath)) {
                Files.createDirectories(customPath.getParent());
                try {
                    Files.write(customPath, file.content(), StandardOpenOption.CREATE_NEW);
                } catch (FileAlreadyExistsException e) {}
            }
        }
    }
    
    private void installGoo2mod(Goo2mod mod, ResFileTable table) {
        Properties properties = PropertiesLoader.getProperties();
        Path customWOG2 = Paths.get(properties.isSteam()
            ? properties.getBaseWorldOfGoo2Directory()
            : properties.getCustomWorldOfGoo2Directory());
        
        try (AddinReader addinFile = new AddinReader(mod)) {
            
            long count = addinFile.getFileCount();
            long i = 0;

            for (Resource resource : addinFile.getAllFiles()) {
                i++;
                updateProgress(i, count);
                
                switch (resource.type()) {
                    case METADATA:
                        if (resource.path().equals("translation.xml")) {
                            writeTranslation(customWOG2, table, resource);
                        }
                        
                        break;
                    case COMPILE:
                        writeCompiledFile(customWOG2, table, mod, resource);
                        break;
                    case OVERRIDE:
                        overrideFile(customWOG2, table, mod, resource);
                        break;
                    case MERGE: {
                        // merging files that have been overriden by previous mods could
                        // lead to weird conflicts, so it's not supported at all
                        if (table.hasEntry(resource.path())) {
                            OverriddenFileEntry entry = table.getEntry(resource.path()).get();
                            
                            if (!entry.getModId().equals("*")) {
                                throw new IllegalArgumentException("Conflict: Trying to merge file "
                                        + resource.path() + " even though it has been overridden by mod "
                                        + entry.getModId() + "previously!");
                            }
                        }
                        
                        Path customPath = customWOG2.resolve("game", resource.path());
                        
                        if (resource.path().endsWith(".wog2")) {
                            mergeWog2(table, resource, customPath);
                        } else if (resource.path().endsWith(".xml")) {
                            mergeXml(table, resource, customPath);
                        } else {
                            throw new IllegalArgumentException("Only allowed files in compile/ are .wog2 and .xml!");
                        }
                        
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            Platform.runLater(() -> {
                Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setContentText("Failed loading the mod \"" + mod.getName() + "\":\n\n" + e.toString());
                dialog.show();
            });
        }
    }
    
    private void writeTranslation(Path customWOG2, ResFileTable table, Resource resource) throws IOException {
        Path localPath = customWOG2.resolve("game/res/properties/translation-local.xml");
        Path intlPath = customWOG2.resolve("game/res/properties/translation-tool-export.xml");
        
        String originalIntlContent = new String(Files.readAllBytes(intlPath), StandardCharsets.UTF_8)
            .replaceAll("& ", "&amp; ");
        TextDB originalIntl = TextLoader.loadText(originalIntlContent);
        TextDB originalLocal = TextLoader.loadText(localPath);
        
        TextDB patch = TextLoader.loadText(resource.content());
        
        for (GameString string : patch.getStrings()) {
            Optional<LocaleText> local = string.getLocal();
            
            if (local.isPresent()) {
                originalLocal.putString(new GameString(string.getId(), local.get()));
            } else {
                originalLocal.removeString(string.getId());
            }
            
            if (string.hasIntl()) {
                originalIntl.putString(new GameString(string.getId(), string.getTexts()));
            } else {
                // TODO: make this use the english text as a fallback
                // for all languages that aren't specified by the mod author
                originalIntl.removeString(string.getId());
            }
        }
        
        table.addEntry("*", "res/properties/translation-local.xml");
        table.addEntry("*", "res/properties/translation-tool-export.xml");
        TextLoader.saveText(originalLocal, localPath.toFile());
        TextLoader.saveText(originalIntl, intlPath.toFile());
    }
    
    private void writeCompiledFile(Path customWOG2, ResFileTable table, Goo2mod mod, Resource resource) throws IOException {
        if (!resource.path().endsWith(".wog2") && !resource.path().endsWith(".xml"))
            throw new IllegalArgumentException("Only allowed files in compile/ are .wog2 and .xml!");
        
        if (resource.path().startsWith("res/levels/") && resource.path().endsWith(".wog2")) {
            String levelName = resource.path().substring("res/levels/".length(), resource.path().length() - 5);
            
            if (mod.getLevel(levelName).isPresent()) {
                // do not copy to customPath/game/res/levels, instead copy into profile/levels
                // so it appears in the level editor menu
                Level level = LevelLoader.loadLevel(resource.contentText());
                System.out.println("Level " + level.getUuid() + ": " + level.getTitle());
                
                Path profileDir = Paths.get(PropertiesLoader.getProperties().getProfileDirectory());
                Path outLevelPath = profileDir.resolve("levels", level.getUuid() + ".wog2");
                
                Files.write(outLevelPath, resource.content(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                return;
            }
        }
            
        Path customPath = customWOG2.resolve("game", resource.path());
        
        if (resource.path().length() > 1) updateMessage(resource.path().substring(1));
        
        table.addEntry(mod.getId(), resource.path());
        
        Files.createDirectories(customPath.getParent());
        Files.write(customPath, resource.content(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    private void overrideFile(Path customWOG2, ResFileTable table, Goo2mod mod, Resource resource) throws IOException {
        if (resource.path().endsWith(".wog2") || resource.path().endsWith(".xml"))
            throw new IllegalArgumentException(".wog2 and .xml are not supported in override/, put them in compile/ instead!");
        
        Path customPath = customWOG2.resolve("game", resource.path());
        
        if (resource.path().length() > 1) updateMessage(resource.path().substring(1));
        
        table.addEntry(mod.getId(), resource.path());
        
        Files.createDirectories(customPath.getParent());
        Files.write(customPath, resource.content(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    private void mergeWog2(ResFileTable table, Resource resource, Path customPath) throws IOException {
        JsonMapper mapper = new JsonMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        JsonNode original = mapper.readTree(customPath.toFile());
        JsonNode patch = mapper.readTree(resource.content());
        
        if (!patch.isObject()) {
            throw new IllegalArgumentException("Json merge file has to be a json object! (at " +  resource.path() + ")");
        }
        
        JsonNode mergeTypeObj = patch.get("__type__");
        if (mergeTypeObj == null) {
            throw new IllegalArgumentException("Json merge file has to contain the property '__type__'! (at "
                    + resource.path() + ")");
        }
        
        if (!mergeTypeObj.isTextual() || !mergeTypeObj.textValue().equals("jsonMerge")) {
            throw new IllegalArgumentException("Property '__type__' has to be of value 'jsonMerge' (at "
                    + resource.path() + ")");
        }
        
        table.addEntry("*", resource.path());
        JsonNode merged = JsonMerge.transformJson(original, (ObjectNode) patch);
        mapper.writeValue(customPath.toFile(), merged);
        
    }
    
    private void mergeXml(ResFileTable table, Resource resource, Path customPath) throws IOException {
        ResrcManifest original = ResrcLoader.loadManifest(customPath);
        ResrcManifest patch = ResrcLoader.loadManifest(resource.content());
        
        table.addEntry("*", resource.path());
        ResrcManifest merged = ResourceXmlMerge.transformResources(original, patch);
        ResrcLoader.saveManifest(merged, customPath.toFile());
    }
    
}