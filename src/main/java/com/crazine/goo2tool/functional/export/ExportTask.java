package com.crazine.goo2tool.functional.export;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crazine.goo2tool.addinfile.Goo2mod;
import com.crazine.goo2tool.addinfile.Goo2mod.ModType;
import com.crazine.goo2tool.functional.FistyInstaller;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.ball.Ball;
import com.crazine.goo2tool.gamefiles.ball.BallLoader;
import com.crazine.goo2tool.gamefiles.ball.Ball.ImageIdInfo;
import com.crazine.goo2tool.gamefiles.environment.*;
import com.crazine.goo2tool.gamefiles.fistyini.*;
import com.crazine.goo2tool.gamefiles.item.*;
import com.crazine.goo2tool.gamefiles.level.*;
import com.crazine.goo2tool.gamefiles.resrc.*;
import com.crazine.goo2tool.gamefiles.translation.*;
import com.crazine.goo2tool.gui.export.FX_ExportDialog.AddinInfo;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.util.VersionNumber;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javafx.concurrent.Task;
import javafx.stage.Stage;

class ExportTask extends Task<Void> {
    
    private static record PathResrc<T extends Resrc>(T resrc, String fullPath) {}
    
    private static enum CompileType {
        LEVEL,
        ENVIRONMENT,
        ITEM,
        BALL,
        // PARTICLE_EFFECT,
        // PARTICLE_SYSTEM,
        // RES_OPTIONS, // ?
    }
    
    private static record CompiledResource(CompileType type, String name, String content) {}
    
    private static enum AssetType {
        AMBIENCE("ambience", "res/ambience/_resources.xml", Resrc.Sound.class,
            new Resrc.SetDefaults("res/ambience/", "AMBIENCE_GLOBAL_")),
        MUSIC("music", "res/music/_resources.xml", Resrc.Sound.class,
            new Resrc.SetDefaults("res/music/", "BGM_")),
        ENVIRONMENT("environments", "res/environments/images/_resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("res/environments/images/", "ENV_BG_")),
        ENVIRONMENT_LUT("environments_lut", "res/environments/luts/_resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("res/environments/luts/", "ENV_LUT_")),
        ITEM("items", "res/items/images/_resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("res/items/images/", "IMAGE_ITEM_")),
        ITEM_PREVIEW(null, null, Resrc.Image.class,
            new Resrc.SetDefaults("res/items/previews/", "")),
        EDITOR_IMAGE("editor", "res/editor/resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("", "")),
        // PARTICLES,
        // SOUND,
        // ANIMATION,
        // TERRAIN,
        // TERRAIN_DECORATION,
        
        // not a real asset type but still belongs in override/
        THUMBNAIL(null, null, null,
            new Resrc.SetDefaults("res/thumbnails/", ""));
        ;
        
        public final String groupId;
        public final String resrcFile;
        public final Class<? extends Resrc> resrcClass;
        public final Resrc.SetDefaults setDefaults;
        
        private AssetType(String groupId, String resrcFile,
                Class<? extends Resrc> resrcClass, Resrc.SetDefaults setDefaults) {
            this.groupId = groupId;
            this.resrcFile = resrcFile;
            this.resrcClass = resrcClass;
            this.setDefaults = setDefaults;
        }
    }
    
    private static record AssetResource(AssetType type, String id, String name, byte[] content) {}
    
    private static Logger logger = LoggerFactory.getLogger(ExportTask.class);
    
    private final Stage stage;
    private final AddinInfo addinInfo;
    private final Path levelPath;
    private final Path outputPath;
    
    private String customWog2;
    private JsonMapper jsonMapper;
    
    private String resrcPathPrefix = "";
    private String resrcIdPrefix = "";
    
    private Map<AssetType, ResrcGroup> customResrcs = new HashMap<>();
    private Map<AssetType, ResrcGroup> originalResrcs = new HashMap<>();
    
    private List<CompiledResource> compiledResources = new ArrayList<>();
    private List<AssetResource> assetResources = new ArrayList<>();
    
    private Set<String> customGooballIds = new HashSet<>();
    
    private boolean success = true;

    ExportTask(Stage stage, AddinInfo addinInfo, Path levelPath, Path outputPath) {
        this.stage = stage;
        this.addinInfo = addinInfo;
        this.levelPath = levelPath;
        this.outputPath = outputPath;
    }

    @Override
    protected Void call() {
        try (ResArchive res = ResArchive.loadOrSetupVanilla(stage)) {
            export(res);
        } catch (Exception e) {
            success = false;
            
            runLater(() -> {
                FX_Alarm.error(e);
            });
        }
        
        if (!success)
            throw new RuntimeException("ExportTask failed");
        
        return (Void) null;
    }
    
    private static void runLater(Runnable runnable) {
        javafx.application.Platform.runLater(runnable);
    }
    
    private void export(ResArchive res) throws Exception {
        Properties properties = PropertiesLoader.getProperties();
        String profileDir = properties.getProfileDirectory();
        
        customWog2 = properties.getTargetWog2Directory();
        jsonMapper = new JsonMapper();
        
        String levelContent = Files.readString(levelPath);
        ObjectNode levelJson = (ObjectNode) jsonMapper.readTree(levelContent);
        Level level = LevelLoader.loadLevel(levelJson);
        
        resrcPathPrefix = addinInfo.modId().replace(".", "_") + "_";
        resrcPathPrefix = resrcPathPrefix.substring(0, 1).toUpperCase() + resrcPathPrefix.substring(1);
        
        resrcIdPrefix = resrcPathPrefix.toUpperCase();
        
        // Set up progress reporting
        Set<String> uniqueItemIds = level.allItemTypes();
        long maxProgress = level.getItems().size() + uniqueItemIds.size() + 4;
        
        Path ballTablePath = Paths.get(customWog2, "game/fisty/ballTable.ini");
        boolean ballTableExists = Files.isRegularFile(ballTablePath);
        
        if (addinInfo.embedThumbnail()) {
            maxProgress++;
        }
        
        if (ballTableExists) {
            Set<Integer> ballTypeEnums = level.allBallTypes();
            maxProgress += ballTypeEnums.size() + level.getItems().size() + 1;
        }
        
        long tracker = 0;
        
        // translation-local.xml
        Optional<byte[]> translationLocalBytes = res.getFileContent("res/properties/translation-local.xml");
        TextDB translationLocal = TextLoader.loadText(translationLocalBytes.get());
        TextDB customTranslationLocal = TextLoader.loadText(Paths.get(customWog2, "game/res/properties/translation-local.xml"));
        
        TextDB textPatch = new TextDB();
        
        for (LevelItem item : level.getItems()) {
            String localizedStringId = item.getLocalizedStringId();
            
            updateProgress(tracker++, maxProgress);
            
            if (localizedStringId.isEmpty())
                continue;
            
            updateMessage(localizedStringId);
            
            // TODO: support for non-english languages
            Optional<GameString> customString = customTranslationLocal.getString(localizedStringId);
            if (customString.isEmpty())
                throw new IOException("Could not find string with id " + localizedStringId);
            
            String customText = customString.get().getLocal().get().getText();
            
            Optional<GameString> originalString = translationLocal.getString(localizedStringId);
            
            if (originalString.isPresent()) {
                String originalText = originalString.get().getLocal().get().getText();
                
                if (!customText.equals(originalText))
                    textPatch.putString(customString.get());
            } else {
                textPatch.putString(customString.get());
            }
        }
        
        // Thumbnail
        String addinThumbnailPath = null;
        
        if (addinInfo.embedThumbnail()) {
            String relativePath = "tmp/thumbs-cache/" + level.getUuid() + ".jpg";
            Path realThumbnailPath = Paths.get(profileDir, relativePath);
            
            updateProgress(tracker++, maxProgress);
            updateMessage(relativePath);
            
            try {
                byte[] thumbnailContent = Files.readAllBytes(realThumbnailPath);
                
                assetResources.add(new AssetResource(AssetType.THUMBNAIL, null, level.getUuid(), thumbnailContent));
                addinThumbnailPath = "res/thumbnails/" + level.getUuid() + ".jpg";
            } catch (NoSuchFileException e) {}
        }
        
        // Load resrc files
        updateProgress(tracker++, maxProgress);
        updateMessage("*/resources.xml");
        
        for (AssetType type : AssetType.values()) {
            if (type.groupId == null || type.resrcFile == null)
                continue;
            
            customResrcs.put(type, loadResrcManifest(type));
            originalResrcs.put(type, loadOriginalResrcManifest(res, type));
        }
        
        // Background
        updateProgress(tracker++, maxProgress);
        updateMessage(level.getBackgroundId());
        
        Optional<String> newBackgroundId = analyzeBackground(res, level.getBackgroundId());
        
        if (newBackgroundId.isPresent())
            levelJson.put("backgroundId", newBackgroundId.get());
        
        // Items
        Map<String, Item> allItems = new HashMap<>();
        Map<String, String> modifiedItemIds = new HashMap<>();
        
        for (String itemId : uniqueItemIds) {
            updateProgress(tracker++, maxProgress);
            updateMessage("Item " + itemId);
            
            Optional<String> newItemId = analyzeItem(res, itemId, allItems);
            
            if (newItemId.isPresent())
                modifiedItemIds.put(itemId, newItemId.get());
        }
        
        for (LevelItem item : level.getItems()) {
            item.setType(modifiedItemIds.getOrDefault(item.getType(), item.getType()));
        }
        
        levelJson.set("items", jsonMapper.valueToTree(level.getItems()));
        
        // Balls
        String outBallsString = null;
        
        if (ballTableExists) {
            updateProgress(tracker++, maxProgress);
            updateMessage("fisty/ballTable.ini");
            
            FistyIniFile ballTable = FistyIniLoader.loadIni(Files.readString(ballTablePath));
            FistyIniFile outBalls = new FistyIniFile("; Mod-specific custom gooballs", List.of());
            
            // Analyze all ballInstances
            Set<Integer> ballTypeEnums = level.allBallTypes();
            Iterable<Integer> sortedBallTypeEnums = () -> ballTypeEnums.stream().sorted().iterator();
            
            for (int typeEnum : sortedBallTypeEnums) {
                updateProgress(tracker++, maxProgress);
                updateMessage("typeEnum " + typeEnum);
            
                Optional<String> customBallId = analyzeGooBall(typeEnum, res, ballTable);
                
                if (customBallId.isPresent()) {
                    logger.trace("Custom gooball {} {}", typeEnum, customBallId.get());
                    
                    int outTypeEnum = outBalls.addBallId(customBallId.get(), typeEnum);
                    assert outTypeEnum == typeEnum;
                }
            }
            
            // Analyze all items for uservars
            for (LevelItem itemInstance : level.getItems()) {
                updateProgress(tracker++, maxProgress);
                updateMessage("Item " + itemInstance.getType());
                
                Item item = allItems.get(itemInstance.getType());
                
                if (item == null)
                    continue;
                
                List<ItemUserVariable> itemVars = item.getUserVariables();
                
                int count = Math.min(itemVars.size(), itemInstance.getUserVariables().size());
                for (int i = 0; i < count; i++) {
                    ItemUserVariable itemVar = itemVars.get(i);
                    
                    if (itemVar.getType() == ItemUserVariable.TYPE_GOO_BALL) {
                        LevelItem.UserVariable value = itemInstance.getUserVariables().get(i);
                        int typeEnum = (int) value.getValue();
                        
                        if (!ballTypeEnums.contains(typeEnum)) {
                            Optional<String> customBallId = analyzeGooBall(typeEnum, res, ballTable);
                            
                            if (customBallId.isPresent()) {
                                logger.trace("Custom gooball (from item) {} {}", typeEnum, customBallId.get());
                    
                                int outTypeEnum = outBalls.addBallId(customBallId.get(), typeEnum);
                                assert outTypeEnum == typeEnum;
                            }
                        }
                    }
                }
            }
            
            if (!outBalls.getEntries().isEmpty()) {
                outBallsString = outBalls.getSourceFile();
            }
        }
        
        
        // Music
        updateProgress(tracker++, maxProgress);
        updateMessage(level.getMusicId());
        
        String newMusicId = analyzeAsset(res, level.getMusicId(), AssetType.MUSIC);
        levelJson.put("musicId", newMusicId);
        
        // Ambience
        updateProgress(tracker++, maxProgress);
        updateMessage(level.getAmbienceId());
        
        String newAmbienceId = analyzeAsset(res, level.getAmbienceId(), AssetType.AMBIENCE);
        levelJson.put("ambienceId", newAmbienceId);
        
        // Serialize level
        String newLevelContent = LevelLoader.saveLevel(levelJson);
        compiledResources.add(new CompiledResource(CompileType.LEVEL, level.getUuid(), newLevelContent));
        
        // Create addin.xml
        Goo2mod mod = new Goo2mod(new VersionNumber(2, 2), addinInfo.modId(), addinInfo.name(), ModType.LEVEL,
                addinInfo.version(), addinInfo.description(), addinInfo.author());
        
        mod.getLevels().add(new Goo2mod.Level(level.getUuid(), addinThumbnailPath));
        
        if (outBallsString != null) {
            mod.getDependencies().add(new Goo2mod.Depends("FistyLoader", FistyInstaller.FISTY_VERSION));
        }
        
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        String addinXml = mapper.writer().withRootName("addin").writeValueAsString(mod);
        
        // Write zip file
        writeZipFile(outputPath, addinXml, textPatch, outBallsString);
    }
    
    private ResrcGroup loadResrcManifest(AssetType type) throws IOException {
        ResrcManifest environmentManifest = ResrcLoader.loadManifest(Paths.get(customWog2, "game", type.resrcFile));
        Optional<ResrcGroup> group = environmentManifest.getGroup(type.groupId);
        
        if (group.isEmpty())
            throw new IOException("Resource Manifest at '" + type.resrcFile + "' is missing resource group '" + type.groupId + "'");
        
        return group.get();
    }
    
    private ResrcGroup loadOriginalResrcManifest(ResArchive res, AssetType type) throws IOException {
        Optional<byte[]> fileContent = res.getFileContent(type.resrcFile);
        
        if (fileContent.isEmpty())
            throw new IOException("Could not find Resource Manifest at '" + type.resrcFile + "'");
        
        ResrcManifest originalEnvironmenManifest = ResrcLoader.loadManifest(fileContent.get());
        Optional<ResrcGroup> group = originalEnvironmenManifest.getGroup(type.groupId);
        
        if (group.isEmpty())
            throw new IOException("Resource Manifest at '" + type.resrcFile
                    + "' is missing resource group '" + type.groupId + "'");
        
        return group.get();
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Resrc> PathResrc<T> getResrc(String id, ResrcGroup group, Class<T> resrcClass) throws IOException {
        Optional<Resrc> resrc = group.getResource(id);
        Optional<String> fullPath = group.getResourcePath(id);
        
        if (resrc.isEmpty() || fullPath.isEmpty())
            throw new IOException("Could not find resource with ID '" + id + "'");
        
        if (!resrcClass.isInstance(resrc.get()))
            throw new IOException("Resource with ID '" + id + "' should be of type " + resrcClass.getSimpleName());
        
        // Type of resrc was just checked above
        return new PathResrc<>((T) resrc.get(), fullPath.get());
    }
    
    private String analyzeAsset(ResArchive res, String id, AssetType type) throws IOException {
        if (id == null || id.isEmpty())
            return id;
        
        // get custom resource
        ResrcGroup group = customResrcs.get(type);
        PathResrc<? extends Resrc> resrc = getResrc(id, group, type.resrcClass);
        
        // get original resource
        ResrcGroup originalGroup = originalResrcs.get(type);
        Optional<Resrc> originalResrc = originalGroup.getResource(id);
        Optional<String> originalResrcPath = originalGroup.getResourcePath(id);
        
        // if original resource is non existant or different, add the asset to output
        Path contentPath = Paths.get(customWog2, "game", resrc.fullPath() + "." + resrc.resrc().fileExtension());
        byte[] content = Files.readAllBytes(contentPath);
        
        if (originalResrc.isPresent() && originalResrcPath.isPresent()) {
            String fullOriginalPath = originalResrcPath.get() + "." + resrc.resrc().fileExtension();
            Optional<byte[]> originalContent = res.getFileContent(fullOriginalPath);
            
            if (originalContent.isEmpty()) {
                throw new IOException("Could not find resource of type " + type.resrcClass.getSimpleName()
                        + " at location '" + fullOriginalPath + "'");
            }
            
            if (!Arrays.equals(content, originalContent.get())) {
                // TODO: id() and path() might be inaccurate if user used a different SetDefaults
                String newId = resrcIdPrefix + resrc.resrc().id();
                String newPath = resrcPathPrefix + resrc.resrc().path();
                
                assetResources.add(new AssetResource(type, newId, newPath, content));
                return type.setDefaults.idprefix() + newId;
            }
        } else {
            assetResources.add(new AssetResource(type, resrc.resrc().id(), resrc.resrc().path(), content));
        }
        
        return id;
    }
    
    private Optional<String> analyzeBackground(ResArchive res, String backgroundId) throws IOException {
        String backgroundText = Files.readString(Paths.get(customWog2, "game/res/environments", backgroundId + ".wog2"));
        Optional<String> originalBackground = res.getFileText("res/environments/" + backgroundId + ".wog2");
        
        ObjectNode backgroundValue = (ObjectNode) jsonMapper.readTree(backgroundText);
        Environment background = EnvironmentLoader.loadBackground(backgroundValue);
        
        boolean backgroundModified = false;
        
        for (Environment.Layer layer : background.getLayers()) {
            String newImageName = analyzeAsset(res, layer.getImageName(), AssetType.ENVIRONMENT);
            
            if (!newImageName.equals(layer.getImageName())) {
                backgroundModified = true;
                layer.setImageName(newImageName);
            }
        }
        
        String newFireLut = analyzeAsset(res, background.getFireLut(), AssetType.ENVIRONMENT_LUT);
        
        if (!newFireLut.equals(background.getFireLut())) {
            backgroundModified = true;
            backgroundValue.put("fireLut", newFireLut);
        }
        
        if (originalBackground.isPresent()) {
            if (backgroundModified || !backgroundText.equals(originalBackground.get())) {
                String newBackgroundId = UUID.randomUUID().toString();
                
                JsonNode serializedLayers = jsonMapper.valueToTree(background.getLayers());
                backgroundValue.put("id", newBackgroundId);
                backgroundValue.set("layers", serializedLayers);
                
                String newBackgroundText = EnvironmentLoader.saveBackground(backgroundValue);
                compiledResources.add(new CompiledResource(CompileType.ENVIRONMENT, newBackgroundId, newBackgroundText));
                
                return Optional.of(newBackgroundId);
            }
        } else {
            JsonNode serializedLayers = jsonMapper.valueToTree(background.getLayers());
            backgroundValue.set("layers", serializedLayers);
            
            String newBackgroundText = EnvironmentLoader.saveBackground(backgroundValue);
            compiledResources.add(new CompiledResource(CompileType.ENVIRONMENT, backgroundId, newBackgroundText));
        }
        
        return Optional.empty();
    }
    
    private Optional<String> analyzeItem(ResArchive res, String itemId, Map<String, Item> allItems) throws IOException {
        String itemFileText = Files.readString(Paths.get(customWog2, "game/res/items", itemId + ".wog2"));
        Optional<String> originalItemFileText = res.getFileText("res/items/" + itemId + ".wog2");
        
        ItemFile itemFile = ItemLoader.loadItemFile(itemFileText);
        
        if (itemFile.getItems().size() != 1)
            throw new IOException("Expected " + itemId + ".wog2 file to contain 1 item, not " + itemFile.getItems().size());
        
        JsonNode itemJson = itemFile.getItems().get(0);
        Item item = ItemLoader.loadItem(itemJson);
        
        allItems.put(itemId, item);
        
        boolean itemModified = false;
        
        for (ItemObject object : item.getObjects()) {
            String newName = analyzeAsset(res, object.getName(), AssetType.ITEM);
            
            if (!newName.equals(object.getName())) {
                itemModified = true;
                object.setName(newName);
            }
        }
        
        // res/items/previews/*.image (does not use a resources.xml file)
        Path previewPath = Paths.get(customWog2, "game/res/items/previews", itemId + ".image");
        byte[] previewContent = null;
        
        if (Files.isRegularFile(previewPath)) {
            previewContent = Files.readAllBytes(previewPath);
        }
        
        if (originalItemFileText.isPresent()) {
            if (itemModified || !itemFileText.equals(originalItemFileText.get())) {
                String newItemId = UUID.randomUUID().toString();
                item.setUuid(newItemId);
                
                ItemFile newItemFile = ItemFile.fromItem(itemJson, item);
                String newItemFileText = ItemLoader.saveItemFile(newItemFile);
                compiledResources.add(new CompiledResource(CompileType.ITEM, newItemId, newItemFileText));
                
                if (previewContent != null) {
                    assetResources.add(new AssetResource(AssetType.ITEM_PREVIEW, null, itemId, previewContent));
                }
                
                return Optional.of(newItemId);
            }
        } else {
            ItemFile newItemFile = ItemFile.fromItem(itemJson, item);
            String newItemFileText = ItemLoader.saveItemFile(newItemFile);
            
            compiledResources.add(new CompiledResource(CompileType.ITEM, itemId, newItemFileText));
            
            if (previewContent != null) {
                assetResources.add(new AssetResource(AssetType.ITEM_PREVIEW, null, itemId, previewContent));
            }
        }
        
        return Optional.empty();
        
    }
    
    private Optional<String> analyzeGooBall(int typeEnum, ResArchive res, FistyIniFile ballTable) throws IOException {
        String ballId = ballTable.getBallName(typeEnum).orElse(null);
        
        if (ballId == null)
            throw new IOException("Could not find gooball with typeEnum " + typeEnum);
        
        boolean ballIdIsCustom = DefaultFistyIni.BALL_TABLE.length <= typeEnum
                || !DefaultFistyIni.BALL_TABLE[typeEnum].equals(ballId);
        
        if (ballIdIsCustom) {
            customGooballIds.add(ballId);
            
            // Check for custom resources that are not in resources.xml
            String ballText = Files.readString(Paths.get(customWog2, "game/res/balls", ballId, "ball.wog2"));
            ObjectNode ballJson = (ObjectNode) jsonMapper.readTree(ballText);
            Ball ball = BallLoader.loadBall(ballJson);
            
            Path resourcesPath = Paths.get(customWog2, "game/res/balls", ballId, "resources.xml");
            ResrcManifest manifest = ResrcLoader.loadManifest(resourcesPath);
            Optional<ResrcGroup> resrcGroup = manifest.getGroup("ball_" + ballId);
            
            if (resrcGroup.isEmpty())
                throw new IOException("Could not find resource group with ID 'ball_" + ballId + "'");
            
            if (ball.getEditorButtonImage() != null) {
                Optional<Resrc> editorButtonImage = resrcGroup.get().getResource(ball.getEditorButtonImage().imageId());
                
                if (editorButtonImage.isEmpty()) {
                    String newEditorButtonImage = analyzeAsset(res, ball.getEditorButtonImage().imageId(), AssetType.EDITOR_IMAGE);
                    
                    logger.trace("Ball {} has new editorButtonImage {}", ballId, newEditorButtonImage);
                    ball.setEditorButtonImage(new ImageIdInfo(newEditorButtonImage));
                }
            }
            
            ballJson.set("editorButtonImage", jsonMapper.valueToTree(ball.getEditorButtonImage()));
            String newBallText = BallLoader.saveBall(ballJson);
            compiledResources.add(new CompiledResource(CompileType.BALL, ballId, newBallText));
            
            return Optional.of(ballId);
        } else {
            return Optional.empty();
        }
    }
    
    private void writeZipFile(Path outputPath, String addinXml, TextDB textPatch, String outBalls) throws IOException {
        FileOutputStream fileOutputStream  = new FileOutputStream(outputPath.toFile());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        
        try (ZipOutputStream zip = new ZipOutputStream(bufferedOutputStream)) {
            // addin.xml
            zip.putNextEntry(new ZipEntry("addin.xml"));
            zip.write(addinXml.getBytes());
            zip.closeEntry();
            
            // translation.xml
            byte[] textPatchBytes = TextLoader.saveText(textPatch);
            
            zip.putNextEntry(new ZipEntry("translation.xml"));
            zip.write(textPatchBytes);
            zip.closeEntry();
            
            // balls.ini
            if (outBalls != null) {
                zip.putNextEntry(new ZipEntry("balls.ini"));
                zip.write(outBalls.getBytes());
                zip.closeEntry();
            }
            
            // compile directory
            for (CompiledResource resource : compiledResources) {
                String entryPath;
                
                switch (resource.type()) {
                    case LEVEL:
                        entryPath = "compile/res/levels/" + resource.name() + ".wog2";
                        break;
                    case ENVIRONMENT:
                        entryPath = "compile/res/environments/" + resource.name() + ".wog2";
                        break;
                    case ITEM:
                        entryPath = "compile/res/items/" + resource.name() + ".wog2";
                        break;
                    case BALL:
                        // Name actually specifies the folder name instead here
                        entryPath = "compile/res/balls/" + resource.name() + "/ball.wog2";
                        break;
                    default:
                        throw new RuntimeException("CompileType " + resource.type() + " not implemented");
                }
                
                zip.putNextEntry(new ZipEntry(entryPath));
                zip.write(resource.content().getBytes());
                zip.closeEntry();
            }
            
            // resrc.xml files
            for (AssetType type : AssetType.values()) {
                if (type.groupId == null || type.resrcFile == null)
                    continue;
                
                List<Resrc> resources = new ArrayList<>();
                resources.add(type.setDefaults);
                
                for (AssetResource asset : assetResources) {
                    if (asset.type() != type)
                        continue;
                    
                    if (type.resrcClass == Resrc.Image.class) {
                        resources.add(new Resrc.Image(asset.id(), asset.name()));
                    } else if (type.resrcClass == Resrc.Sound.class) {
                        resources.add(new Resrc.Sound(asset.id(), asset.name(), true, "Music"));
                    } else {
                        throw new RuntimeException("Resource type " + type.resrcClass.getSimpleName() + " not implemented");
                    }
                }
                
                if (resources.stream().noneMatch(resrc -> !(resrc instanceof Resrc.SetDefaults)))
                    continue;
                
                ResrcGroup group = new ResrcGroup(type.groupId, resources);
                ResrcManifest manifest = new ResrcManifest(group);
                
                zip.putNextEntry(new ZipEntry("merge/" + type.resrcFile));
                zip.write(ResrcLoader.saveManifest(manifest));
                zip.closeEntry();
            }
            
            // override directory
            for (AssetResource asset : assetResources) {
                Resrc.SetDefaults setDefaults = asset.type().setDefaults;
                
                String fileExtension = asset.type() == AssetType.THUMBNAIL
                    ? "jpg" : Resrc.getFileExtension(asset.type().resrcClass);
                String entryPath = "override/" + setDefaults.path() + asset.name() + "." + fileExtension;
                
                zip.putNextEntry(new ZipEntry(entryPath));
                zip.write(asset.content());
                zip.closeEntry();
            }
            
            // goo balls
            for (String gooBallId : customGooballIds) {
                Path gooBallDir = Paths.get(customWog2, "game/res/balls", gooBallId);
                logger.trace("Saving custom goo ball at directory {}", gooBallDir);
                
                Iterable<Path> allSubFiles = () -> {
                    try {
                        return Files.walk(gooBallDir).iterator();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };
                
                try {
                    for (Path filePath : allSubFiles) {
                        // ball.wog2 is handled as a regular compiled resource instead
                        if (!Files.isRegularFile(filePath) || filePath.endsWith("ball.wog2"))
                            continue;
                        
                        byte[] content = Files.readAllBytes(filePath);
                        
                        String pathPrefix = filePath.toString().endsWith(".wog2") || filePath.toString().endsWith(".xml")
                            ? "compile/" : "override/";
                        Path relativePath = gooBallDir.relativize(filePath);
                        
                        String entryPath = pathPrefix + "res/balls/" + gooBallId + "/" + relativePath.toString();
                        
                        zip.putNextEntry(new ZipEntry(entryPath));
                        zip.write(content);
                        zip.closeEntry();
                    }
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            }
        }
    }
    
}