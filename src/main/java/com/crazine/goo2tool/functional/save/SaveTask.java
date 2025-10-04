package com.crazine.goo2tool.functional.save;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crazine.goo2tool.addinfile.AddinFileLoader;
import com.crazine.goo2tool.addinfile.AddinReader;
import com.crazine.goo2tool.addinfile.Goo2mod;
import com.crazine.goo2tool.addinfile.AddinReader.Resource;
import com.crazine.goo2tool.addinfile.AddinReader.ResourceType;
import com.crazine.goo2tool.functional.save.filetable.ResFileTable;
import com.crazine.goo2tool.functional.save.filetable.ResFileTableLoader;
import com.crazine.goo2tool.functional.save.filetable.ResFileTable.OverriddenFileEntry;
import com.crazine.goo2tool.functional.save.mergetable.ResrcMergeTable;
import com.crazine.goo2tool.functional.save.mergetable.MergeTableLoader;
import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeEntry;
import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeFile;
import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeValue;
import com.crazine.goo2tool.gamefiles.AppImageResArchive;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.ResArchive.ResFile;
import com.crazine.goo2tool.gamefiles.fistyini.DefaultFistyIni;
import com.crazine.goo2tool.gamefiles.fistyini.FistyIniFile;
import com.crazine.goo2tool.gamefiles.fistyini.FistyIniLoader;
import com.crazine.goo2tool.gamefiles.item.Item;
import com.crazine.goo2tool.gamefiles.item.ItemLoader;
import com.crazine.goo2tool.gamefiles.item.ItemUserVariable;
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelBallInstance;
import com.crazine.goo2tool.gamefiles.level.LevelItem;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.gamefiles.level.LevelStrand;
import com.crazine.goo2tool.gamefiles.resrc.ResrcGroup;
import com.crazine.goo2tool.gamefiles.resrc.ResrcLoader;
import com.crazine.goo2tool.gamefiles.resrc.ResrcManifest;
import com.crazine.goo2tool.gamefiles.translation.GameString;
import com.crazine.goo2tool.gamefiles.translation.TextDB;
import com.crazine.goo2tool.gamefiles.translation.TextLoader;
import com.crazine.goo2tool.gamefiles.translation.GameString.LocaleText;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.gui.util.FX_Alert;
import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.util.HashUtil;
import com.crazine.goo2tool.util.Platform;
import com.crazine.goo2tool.util.VersionNumber;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;

class SaveTask extends Task<Void> {
    
    private static Logger logger = LoggerFactory.getLogger(SaveTask.class);
    
    private final Stage stage;
    
    private boolean success = true;

    SaveTask(Stage stage) {
        this.stage = stage;
    }

    @Override
    protected Void call() {
        try (ResArchive res = ResArchive.loadOrSetupVanilla(stage)) {
            save(res);
        } catch (Exception e) {
            success = false;
            
            runLater(() -> {
                FX_Alarm.error(e);
            });
        }
        
        if (!success)
            throw new RuntimeException("SaveTask failed");
        
        return (Void) null;
    }
    
    private static void runLater(Runnable runnable) {
        javafx.application.Platform.runLater(runnable);
    }
    
    private void save(ResArchive res) throws Exception {
        
        Properties properties = PropertiesLoader.getProperties();

        // Figure out which files are owned by mods that have been disabled
        // so they can be overwritten with vanilla assets
        List<String> disabledAddinIds = new ArrayList<>();
        for (AddinConfigEntry addin : properties.getAddins()) {
            if (!addin.isLoaded())
                disabledAddinIds.add(addin.getId());
        }
        
        Path fileTablePath = Paths.get(PropertiesLoader.getGoo2ToolPath(), "fileTable.xml");
        ResFileTable table = ResFileTableLoader.loadOrInit(fileTablePath);
        
        Path mergeTablePath = Paths.get(PropertiesLoader.getGoo2ToolPath(), "resrcMergeTable.xml");
        ResrcMergeTable mergeTable = MergeTableLoader.loadOrInit(mergeTablePath);
        
        // Merge original res folder
        updateTitle("Validating original WoG2");
        String baseWog2 = properties.getBaseWorldOfGoo2Directory();
        String customWog2 = properties.getTargetWog2Directory();
        
        if (properties.isSteam()) {
            // the Steam version doesn't use a dedicated custom directory
            // so copying all of the meta files is unnecessary
            boolean result = extractRes(res, table, 0, res.fileCount());
            
            if (!result) {
                success = false;
                return;
            }
        } else if (Platform.getCurrent() == Platform.LINUX && baseWog2.endsWith(".AppImage")) {
            // The only important file in the AppImage's root is the WorldOfGoo2 executable
            // so this is the only file that will get extracted
            AppImageResArchive appImage = (AppImageResArchive) res;
            Path executablePath = appImage.getExecutable();
            
            try {
                Files.copy(executablePath, Paths.get(customWog2, "WorldOfGoo2"));
            } catch (FileAlreadyExistsException e) {}
            
            boolean result = extractRes(res, table, 0, res.fileCount());
            
            if (!result) {
                success = false;
                return;
            }
        } else {
            boolean result = copyMissingOriginalFiles(res, table);
            
            if (!result) {
                success = false;
                return;
            }
        }
        
        // Restore all original merge values of mods that are not installed
        logger.debug("Restoring original merge values");
        Set<String> enabledAddinIds = properties.getAddins().stream()
                .filter(addin -> addin.isLoaded())
                .map(addin -> addin.getId())
                .collect(Collectors.toSet());
        
        for (MergeFile<MergeValue> file : mergeTable.getFiles()) {
            Path filePath = Paths.get(customWog2, "game", file.getPath());
            ResrcManifest manifest = ResrcLoader.loadManifest(filePath);
            
            for (MergeEntry<MergeValue> entry : file.getEntries()) {
                if (enabledAddinIds.contains(entry.getModId()))
                    continue;
                
                if (entry.getOriginalValue() == null)
                    continue;
                
                MergeValue originalValue = entry.getOriginalValue();
                
                ResrcGroup group = manifest.getGroup(entry.getGroup()).orElse(null);
                if (group == null) {
                    logger.error("File '{}' does not contain the group '{}'", file.getPath(), entry.getGroup());
                    FX_Alert.error("Goo2Tool",
                            String.format("File '%s' does not contain the group '%s'",
                            file.getPath(), entry.getGroup()),
                            ButtonType.OK);
                    
                    continue;
                }
                
                group.removeResource(entry.getId());
                group.addResources(List.of(originalValue.getSetDefaults(), originalValue.getValue()), true);
            }
            
            file.getEntries().removeIf(entry -> !enabledAddinIds.contains(entry.getModId()) && entry.getOriginalValue() != null);
            ResrcLoader.saveManifest(manifest, filePath.toFile());
        }
        
        // Load ballTable
        logger.debug("Loading ballTable");
        
        FistyIniFile ballTable = null;
        if (properties.getFistyVersion() != null) {
            Path ballTablePath = Paths.get(customWog2, "game/fisty/ballTable.ini");
            
            if (Files.exists(ballTablePath)) {
                String ballTableString = Files.readString(ballTablePath);
                
                if (ballTableString.endsWith("\n\n")) {}
                else if (ballTableString.endsWith("\n"))
                    ballTableString += "\n";
                else
                    ballTableString += "\n\n";
                
                ballTable = FistyIniLoader.loadIni(ballTableString);
            } else {
                ballTable = DefaultFistyIni.generateBallTable();
            }
        }
        
        // Retrieve mods
        List<Goo2mod> goo2modsSorted = AddinFileLoader.loadEnabledAddins();
        
        // Install mods
        for (Goo2mod goo2mod : goo2modsSorted) {
            updateTitle("Installing addin " + goo2mod.getId());
            installGoo2mod(goo2mod, table, mergeTable, ballTable);
        }
        
        // Save ballTable
        if (ballTable != null) {
            Path ballTablePath = Paths.get(customWog2, "game/fisty/ballTable.ini");
            
            Files.createDirectories(ballTablePath.getParent());
            Files.writeString(ballTablePath, ballTable.getSourceFile(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        
        // Calculate hash of all modId="*" files
        for (OverriddenFileEntry entry : table.getEntries()) {
            if (!entry.getModId().equals("*"))
                continue;
            
            Path filePath = Paths.get(customWog2, "game", entry.getPath());
            byte[] content = Files.readAllBytes(filePath);
            String contentHash = HashUtil.getMD5Hash(content);
            
            entry.setHash(contentHash);
        }
        
        // Delete unloaded levels from profile
        Path profilePath = Paths.get(properties.getProfileDirectory());
        
        for (OverriddenFileEntry entry : table.getEntries()) {
            if (entry.getPath().startsWith("$profile/")) {
                String path = entry.getPath().substring("$profile/".length());
                String modId = entry.getModId();
                
                // Check if the mod is loaded
                boolean isLoaded = false;
                for (Goo2mod mod : goo2modsSorted) {
                    if (mod.getId().equals(modId)) {
                        isLoaded = true;
                        break;
                    }
                }
                
                if (!isLoaded) {
                    Path realPath = profilePath.resolve(path);
                    byte[] content = Files.readAllBytes(realPath);
                    String contentHash = HashUtil.getMD5Hash(content);
                    
                    // Only delete level file if it hasn't been modified
                    if (entry.getHash().isEmpty() || entry.getHash().equals(contentHash)) {
                        try {
                            Files.delete(realPath);
                        } catch (NoSuchFileException e) {}
                    } else {
                        logger.info("File '{}' has been manually modified, therefore not deleting",
                                path);
                    }
                    
                    table.removeEntry(entry);
                }
            }
        }
        
        ResFileTableLoader.save(table, fileTablePath);
        MergeTableLoader.save(mergeTable, mergeTablePath);
        
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
    
    private boolean copyMissingOriginalFiles(ResArchive res, ResFileTable garbageFiles) throws IOException {
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
            runLater(() -> {
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
        return extractRes(res, garbageFiles, tracker, fileCount);
    }
    
    private boolean extractRes(ResArchive res, ResFileTable garbageFiles, long tracker, long fileCount) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        
        String customWog2 = properties.getTargetWog2Directory();
        
        logger.info("Extracting res archive ({} files)", res.fileCount());
        
        try {
            for (ResFile file : res.getAllFiles()) {
                tracker++;
                updateProgress(tracker, fileCount);
                updateMessage("game/" + file.path());
                
                Path customPath = Paths.get(customWog2, "game", file.path());
                
                Optional<OverriddenFileEntry> garbageFile = garbageFiles.getEntry(file.path());
                
                if (garbageFile.isPresent()) {
                    byte[] fileContent = file.readContent();
                    
                    garbageFiles.removeEntry(file.path());
                    
                    try {
                        byte[] customFileContent = Files.readAllBytes(customPath);
                        String customFileHash = HashUtil.getMD5Hash(customFileContent);
                        
                        logger.debug("File {} hash {} vs {}",
                            file.path(), garbageFile.get().getHash(), customFileHash);
                        
                        // Prompt the user about file being overwritten
                        if (!customFileHash.equals(garbageFile.get().getHash())) {
                            CountDownLatch latch = new CountDownLatch(1);
                            AtomicBoolean isOk = new AtomicBoolean();
                            
                            runLater(() -> {
                                ButtonType continueButton = new ButtonType("Continue", ButtonData.OK_DONE);
                                
                                Optional<ButtonType> result = FX_Alert.warn("Goo2Tool", 
                                    "The file \"" + file.path() + "\" seems to have been modified by the user.\n\n"
                                    + " If you click Continue, any modifications will be deleted.\n\n"
                                    + "Should you not want to lose existing content, please back up the file"
                                    + " before clicking Continue.",
                                    Optional.of(ButtonType.CANCEL), continueButton, ButtonType.CANCEL);
                                
                                isOk.set(result.isPresent() && result.get() == continueButton);
                                latch.countDown();
                            });
                            
                            try {
                                latch.await();
                            } catch (InterruptedException e) {
                                throw new IOException(e);
                            }
                            
                            if (!isOk.get()) {
                                return false;
                            }
                        }
                        
                        Files.write(customPath, fileContent, StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (NoSuchFileException e) {
                        Files.createDirectories(customPath.getParent());
                        Files.write(customPath, fileContent, StandardOpenOption.CREATE_NEW);
                    }
                } else if (!Files.exists(customPath)) {
                    byte[] fileContent = file.readContent();
                    
                    Files.createDirectories(customPath.getParent());
                    try {
                        Files.write(customPath, fileContent, StandardOpenOption.CREATE_NEW);
                    } catch (FileAlreadyExistsException e) {}
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        
        return true;
    }
    
    private void installGoo2mod(Goo2mod mod, ResFileTable table, ResrcMergeTable mergeTable, FistyIniFile ballTable) {
        logger.info("Installing mod {}", mod.getId());
        
        Properties properties = PropertiesLoader.getProperties();
        String customWog2 = properties.getTargetWog2Directory();
        
        try (AddinReader addinFile = new AddinReader(mod)) {
            
            long count = addinFile.getFileCount();
            long i = 0;
            
            // Check FistyLoader version
            Optional<Goo2mod.Depends> fistyDepends = mod.getDependency("FistyLoader");
            
            if (fistyDepends.isPresent()) {
                if (ballTable == null || properties.getFistyVersion() == null)
                    throw new Exception("Mod '" + mod.getId() + "' requires FistyLoader, which is not installed!");
                
                VersionNumber minVersion = fistyDepends.get().getMinVersion();
                if (minVersion != null && properties.getFistyVersion().compareTo(minVersion) < 0)
                    throw new Exception("Mod '" + mod.getId() + "' requires FistyLoader "
                            + minVersion + " but version " + properties.getFistyVersion() + " is installed!");
                
                VersionNumber maxVersion = fistyDepends.get().getMaxVersion();
                if (maxVersion != null && properties.getFistyVersion().compareTo(maxVersion) > 0)
                    throw new Exception("Mod '" + mod.getId() + "' requires MAXIMUM FistyLoader "
                            + maxVersion + " but version " + properties.getFistyVersion() + " is installed!");
            }
            
            // Load FistyLoader balls.ini
            Map<Integer, Integer> ballIdMap = new HashMap<>();
            if (ballTable != null) {
                Optional<String> modBallsString = addinFile.getFileText("balls.ini");
                
                if (modBallsString.isPresent()) {
                    FistyIniFile modBalls = FistyIniLoader.loadIni(modBallsString.get());
                    List<String> entries = modBalls.getEntries();
                    
                    for (int j = 0; j < entries.size(); j++) {
                        String ballName = entries.get(j);
                        
                        if (ballName.isEmpty())
                            continue;
                        
                        // addBallId returns the actual id of the gooball in the final ballTable.ini
                        // this creates a map of old ballId -> new ballId which will be used to transform
                        // every gooball definition in all level wog2 files
                        ballIdMap.put(j, ballTable.addBallId(ballName, j));
                    }
                }
            }

            // Load main resources
            for (Resource resource : addinFile.getAllFiles()) {
                switch (resource.type()) {
                    case METADATA:
                        updateProgress(++i, count);
                        
                        if (resource.path().equals("translation.xml"))
                            writeTranslation(table, resource);
                        
                        if (resource.path().equals("balls.ini") && fistyDepends.isEmpty()) {
                            throw new Exception("Mod contains 'balls.ini' file even though "
                                    + "FistyLoader is not listed in its dependencies!");
                        }
                        
                        break;
                    case COMPILE: {
                        // Make sure file hasn't been merged before
                        Optional<OverriddenFileEntry> entry = table.getEntry(resource.path());
                        if (entry.isPresent()) {
                            if (entry.get().getModId().equals("*"))
                                throw new IllegalArgumentException("Cannot override compiled file that has been merged before!");
                        }
                        
                        Optional<MergeFile<MergeValue>> mergeFile = mergeTable.getFile(resource.path());
                        if (mergeFile.isPresent()) {
                            if (!mergeFile.get().getEntries().isEmpty())
                                throw new IllegalArgumentException("Cannot override compiled file that has been merged before!");
                            else
                                mergeTable.getFiles().remove(mergeFile.get());
                        }
                        
                        // Levels are handled after this loop
                        if (resource.path().startsWith("res/levels/") && resource.path().endsWith(".wog2")) {
                            logger.trace("Skipping level {} for now", resource.path());
                            break;
                        }
                        
                        updateProgress(++i, count);
                        writeCompiledFile(table, mod, resource);
                        break;
                    }
                    case OVERRIDE:
                        // Files that can be merged aren't allowed in override anyway
                        // so no need to check if this file has been merged before
                        
                        updateProgress(++i, count);
                        overrideFile(table, mod, resource);
                        break;
                    case MERGE: {
                        updateProgress(++i, count);
                        
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
                        
                        if (resource.path().length() > 1)
                            updateMessage(resource.path().substring(1));
            
                        Path customPath = Paths.get(customWog2, "game", resource.path());
                        
                        if (resource.path().endsWith(".wog2")) {
                            mergeWog2(table, resource, customPath);
                        } else if (resource.path().endsWith(".xml")) {
                            if (resource.path().equals("res/properties/fx.xml"))
                                throw new IllegalArgumentException("fx.xml merge not supported yet"
                                        + " (you should probably use res/particles anyway)");
                            
                            MergeFile<MergeValue> file = mergeTable.getOrAddFile(resource.path(),
                                    () -> new MergeFile<>(resource.path()));
                            
                            mergeXml(table, file, resource, customPath, mod.getId());
                        } else {
                            throw new IllegalArgumentException("Only allowed files in compile/ are .wog2 and .xml!");
                        }
                        
                        break;
                    }
                }
            }
            
            // Load levels
            for (Resource resource : addinFile.getAllFiles()) {
                if (resource.type() != ResourceType.COMPILE)
                    continue;
                if (!resource.path().startsWith("res/levels/") || !resource.path().endsWith(".wog2"))
                    continue;
                
                updateProgress(++i, count);
                if (resource.path().length() > 1)
                    updateMessage(resource.path().substring(1));
                
                JsonMapper jsonMapper = new JsonMapper();
                
                ObjectNode levelJson = (ObjectNode) jsonMapper.readTree(resource.contentText());
                Level level = LevelLoader.loadLevel(levelJson);
                
                // Update gooball typeEnum entries based on ballTable
                byte[] outLevel;
                
                if (ballIdMap.isEmpty()) {
                    outLevel = resource.content();
                } else {
                    outLevel = serializeUpdatedLevel(level, levelJson, ballIdMap);
                }
                
                String outLevelHash = HashUtil.getMD5Hash(outLevel);
                
                // Write level either to profile or wog2 dir
                String levelName = resource.path().substring("res/levels/".length(), resource.path().length() - 5);
                Optional<Goo2mod.Level> levelEntry = mod.getLevel(levelName);
                
                if (levelEntry.isPresent()) {
                    writeLevelToProfile(addinFile, level, levelEntry.get(), outLevel);
                    table.addEntry(mod.getId(), outLevelHash, "$profile/levels/" + level.getUuid() + ".wog2");
                } else {
                    table.addEntry(mod.getId(), outLevelHash, resource.path());
                    
                    Path customPath = Paths.get(customWog2, "game", resource.path());
                    
                    Files.createDirectories(customPath.getParent());
                    Files.write(customPath, outLevel,
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            success = false;
            
            runLater(() -> {
                Dialog<ButtonType> dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setContentText("Failed loading the mod \"" + mod.getName() + "\":\n\n" + e.toString());
                dialog.show();
            });
        }
    }
    
    private void writeTranslation(ResFileTable table, Resource resource) throws IOException {
        Path customWog2 = Paths.get(PropertiesLoader.getProperties().getTargetWog2Directory());
        
        Path localPath = customWog2.resolve("game/res/properties/translation-local.xml");
        Path intlPath = customWog2.resolve("game/res/properties/translation-tool-export.xml");
        
        // TODO (priority): Make this use MergeTable too
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
        
        table.addEntry("*", "", "res/properties/translation-local.xml");
        table.addEntry("*", "", "res/properties/translation-tool-export.xml");
        TextLoader.saveText(originalLocal, localPath.toFile());
        TextLoader.saveText(originalIntl, intlPath.toFile());
    }
    
    private void writeCompiledFile(ResFileTable table, Goo2mod mod, Resource resource) throws IOException {
        if (!resource.path().endsWith(".wog2") && !resource.path().endsWith(".xml"))
            throw new IllegalArgumentException("Only allowed files in compile/ are .wog2 and .xml!");
        
        if (resource.path().length() > 1)
            updateMessage(resource.path().substring(1));
        
        String customWog2 = PropertiesLoader.getProperties().getTargetWog2Directory();
        Path customPath = Paths.get(customWog2, "game", resource.path());
        
        String resourceHash = HashUtil.getMD5Hash(resource.content());
        
        table.addEntry(mod.getId(), resourceHash, resource.path());
        
        Files.createDirectories(customPath.getParent());
        Files.write(customPath, resource.content(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    private void writeLevelToProfile(AddinReader addinFile, Level level, Goo2mod.Level levelEntry, byte[] levelContent) throws IOException {
        // do not copy to customPath/game/res/levels, instead copy into profile/levels
        // so it appears in the level editor menu
        logger.info("Copying level {} ({}) to profile", level.getTitle(), level.getUuid());
        
        String profileDir = PropertiesLoader.getProperties().getProfileDirectory();
        Path outLevelPath = Paths.get(profileDir, "levels", level.getUuid() + ".wog2");
        
        Files.write(outLevelPath, levelContent,
            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        
        // write thumbnail if it doesn't yet exist
        if (levelEntry.thumbnail() != null && !levelEntry.thumbnail().isEmpty()) {
            Optional<Resource> thumbnail = addinFile.getFileContent("override/" + levelEntry.thumbnail());
            
            if (thumbnail.isPresent()) {
                Path thumbnailPath = Paths.get(profileDir, "tmp/thumbs-cache", level.getUuid() + ".jpg");
                
                try {
                    Files.write(thumbnailPath, thumbnail.get().content(), StandardOpenOption.CREATE_NEW);
                } catch (FileAlreadyExistsException e) {}
            }
        }
    }
    
    private byte[] serializeUpdatedLevel(Level level, ObjectNode levelJson, Map<Integer, Integer> ballIdMap) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        String customWog2 = properties.getTargetWog2Directory();
        
        JsonMapper jsonMapper = new JsonMapper();
        
        // Collect all items
        Set<String> allItemTypes = level.allItemTypes();
        
        Map<String, Item> itemDefs;
        try {
            itemDefs = allItemTypes.parallelStream()
                .collect(Collectors.toMap(type -> type, type -> {
                    try {
                        // Any other file in the addin will already have been written to the wog2 dir
                        // so the addin file doesn't need to be checked anymore
                        Path itemPath = Paths.get(customWog2, "game/res/items", type + ".wog2");
                        
                        if (!Files.isRegularFile(itemPath))
                            throw new IOException("Could not find item with ID " + type);
                        
                        String itemString = Files.readString(itemPath);
                        return  ItemLoader.loadItemFileAsItem(itemString);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        
        for (LevelBallInstance ballInstance : level.getBalls()) {
            int originalTypeEnum = ballInstance.getTypeEnum();
            ballInstance.setTypeEnum(ballIdMap.getOrDefault(originalTypeEnum, originalTypeEnum));
        }
        
        levelJson.set("balls", jsonMapper.valueToTree(level.getBalls()));
        
        for (LevelStrand strand : level.getStrands()) {
            int originalTypeEnum = strand.getType();
            strand.setType(ballIdMap.getOrDefault(originalTypeEnum, originalTypeEnum));
        }
        
        levelJson.set("strands", jsonMapper.valueToTree(level.getStrands()));
        
        for (LevelItem item : level.getItems()) {
            Item itemDef = itemDefs.get(item.getType());
            List<ItemUserVariable> itemVars = itemDef.getUserVariables();
            
            int count = Math.min(itemVars.size(), item.getUserVariables().size());
            for (int i = 0; i < count; i++) {
                ItemUserVariable itemVar = itemVars.get(i);
                LevelItem.UserVariable value = item.getUserVariables().get(i);
                
                if (itemVar.getType() == ItemUserVariable.TYPE_GOO_BALL) {
                    int oldValue = (int) value.getValue();
                    int newValue = ballIdMap.getOrDefault(oldValue, oldValue);
                    logger.debug("Gooball user var {} with value {} -> {} in item {}", itemVar.getName(), oldValue, newValue, itemDef.getName());
                    value.setValue(newValue);
                }
            }
        }
        
        levelJson.set("items", jsonMapper.valueToTree(level.getItems()));
        
        return LevelLoader.saveLevel(levelJson).getBytes();
    }
    
    private void overrideFile(ResFileTable table, Goo2mod mod, Resource resource) throws IOException {
        if (resource.path().endsWith(".wog2") || resource.path().endsWith(".xml"))
            throw new IllegalArgumentException(".wog2 and .xml are not supported in override/, put them in compile/ instead!");
        
        if (resource.path().length() > 1)
            updateMessage(resource.path().substring(1));
        
        if (mod.isThumbnail(resource.path())) {
            return;
        }
        
        String customWog2 = PropertiesLoader.getProperties().getTargetWog2Directory();
        Path customPath = Paths.get(customWog2, "game", resource.path());
        
        String resourceHash = HashUtil.getMD5Hash(resource.content());
        
        table.addEntry(mod.getId(), resourceHash, resource.path());
        
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
        
        table.addEntry("*", "", resource.path());
        JsonNode merged = JsonMerge.transformJson(original, (ObjectNode) patch);
        mapper.writeValue(customPath.toFile(), merged);
        
    }
    
    private void mergeXml(ResFileTable table, MergeFile<MergeValue> mergeFile, Resource resource, Path customPath, String modId) throws IOException {
        // TODO: cache these
        ResrcManifest original = ResrcLoader.loadManifest(customPath);
        ResrcManifest patch = ResrcLoader.loadManifest(resource.content());
        
        ResrcManifest merged = ResourceXmlMerge.transformResources(original, patch, mergeFile, modId);
        ResrcLoader.saveManifest(merged, customPath.toFile());
    }
    
}