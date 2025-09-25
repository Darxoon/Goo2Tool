package com.crazine.goo2tool.functional.export;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.crazine.goo2tool.VersionNumber;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.addinFile.Goo2mod.ModType;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.environment.Environment;
import com.crazine.goo2tool.gamefiles.environment.EnvironmentLoader;
import com.crazine.goo2tool.gamefiles.item.Item;
import com.crazine.goo2tool.gamefiles.item.ItemFile;
import com.crazine.goo2tool.gamefiles.item.ItemLoader;
import com.crazine.goo2tool.gamefiles.item.ItemObject;
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelItem;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.ResrcGroup;
import com.crazine.goo2tool.gamefiles.resrc.ResrcLoader;
import com.crazine.goo2tool.gamefiles.resrc.ResrcManifest;
import com.crazine.goo2tool.gamefiles.translation.GameString;
import com.crazine.goo2tool.gamefiles.translation.TextDB;
import com.crazine.goo2tool.gamefiles.translation.TextLoader;
import com.crazine.goo2tool.gui.export.FX_ExportDialog.AddinInfo;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
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
        // BALL,
        // PARTICLE_EFFECT,
        // PARTICLE_SYSTEM,
        // RES_OPTIONS, // ?
    }
    
    private static record CompiledResource(CompileType type, String name, String content) {}
    
    private static enum AssetType {
        // AMBIENCE,
        MUSIC("music", "res/music/_resources.xml", Resrc.Sound.class,
            new Resrc.SetDefaults("res/music/", "BGM_")),
        ENVIRONMENT("environments", "res/environments/images/_resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("res/environments/images/", "ENV_BG_")),
        ENVIRONMENT_LUT("environments_lut", "res/environments/luts/_resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("res/environments/luts/", "ENV_LUT_")),
        ITEM("items", "res/items/images/_resources.xml", Resrc.Image.class,
            new Resrc.SetDefaults("res/items/images/", "IMAGE_ITEM_")),
        // ITEM_PREVIEW,
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
    
    private final Stage stage;
    private final AddinInfo addinInfo;
    private final Path levelPath;
    private final Path outputPath;
    
    private String resrcPathPrefix = "";
    private String resrcIdPrefix = "";
    
    private Map<AssetType, ResrcGroup> customResrcs = new HashMap<>();
    private Map<AssetType, ResrcGroup> originalResrcs = new HashMap<>();
    
    private List<CompiledResource> compiledResources = new ArrayList<>();
    private List<AssetResource> assetResources = new ArrayList<>();
    
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
        String customWog2 = properties.getTargetWog2Directory();
        String profileDir = properties.getProfileDirectory();
        
        JsonMapper jsonMapper = new JsonMapper();
        
        String levelContent = Files.readString(levelPath);
        ObjectNode levelJson = (ObjectNode) jsonMapper.readTree(levelContent);
        Level level = LevelLoader.loadLevel(levelJson);
        
        resrcPathPrefix = addinInfo.modId().replace(".", "_") + "_";
        resrcPathPrefix = resrcPathPrefix.substring(0, 1).toUpperCase() + resrcPathPrefix.substring(1);
        
        resrcIdPrefix = resrcPathPrefix.toUpperCase();
        
        // translation-local.xml
        Optional<byte[]> translationLocalBytes = res.getFileContent("res/properties/translation-local.xml");
        TextDB translationLocal = TextLoader.loadText(translationLocalBytes.get());
        TextDB customTranslationLocal = TextLoader.loadText(Paths.get(customWog2, "game/res/properties/translation-local.xml"));
        
        TextDB textPatch = new TextDB();
        
        for (LevelItem item : level.getItems()) {
            String localizedStringId = item.getLocalizedStringId();
            
            if (localizedStringId.isEmpty())
                continue;
            
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
        String addinThumbnailPath;
        
        if (addinInfo.embedThumbnail()) {
            Path realThumbnailPath = Paths.get(profileDir, "tmp/thumbs-cache", level.getUuid() + ".jpg");
            byte[] thumbnailContent = Files.readAllBytes(realThumbnailPath);
            
            assetResources.add(new AssetResource(AssetType.THUMBNAIL, null, level.getUuid(), thumbnailContent));
            addinThumbnailPath = "res/thumbnails/" + level.getUuid() + ".jpg";
        } else {
            addinThumbnailPath = null;
        }
        
        // Load resrc files
        for (AssetType type : AssetType.values()) {
            if (type.groupId == null || type.resrcFile == null)
                continue;
            
            customResrcs.put(type, loadResrcManifest(type));
            originalResrcs.put(type, loadOriginalResrcManifest(res, type));
        }
        
        // Background
        String backgroundText = Files.readString(Paths.get(customWog2, "game/res/environments", level.getBackgroundId() + ".wog2"));
        Optional<String> originalBackground = res.getFileText("res/environments/" + level.getBackgroundId() + ".wog2");
        
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
                levelJson.put("backgroundId", newBackgroundId);
                
                JsonNode serializedLayers = jsonMapper.valueToTree(background.getLayers());
                backgroundValue.put("id", newBackgroundId);
                backgroundValue.set("layers", serializedLayers);
                
                String newBackgroundText = EnvironmentLoader.saveBackground(backgroundValue);
                compiledResources.add(new CompiledResource(CompileType.ENVIRONMENT, newBackgroundId, newBackgroundText));
            }
        } else {
            JsonNode serializedLayers = jsonMapper.valueToTree(background.getLayers());
            backgroundValue.set("layers", serializedLayers);
            
            String newBackgroundText = EnvironmentLoader.saveBackground(backgroundValue);
            compiledResources.add(new CompiledResource(CompileType.ENVIRONMENT, level.getBackgroundId(), newBackgroundText));
        }
        
        // Items
        Set<String> itemIds = level.getItems().stream()
                .map(item -> item.getType())
                .collect(Collectors.toSet());
        
        Map<String, String> modifiedItemIds = new HashMap<>();
        
        for (String itemId : itemIds) {
            String itemFileText = Files.readString(Paths.get(customWog2, "game/res/items", itemId + ".wog2"));
            Optional<String> originalItemFileText = res.getFileText("res/items/" + itemId + ".wog2");
            
            ItemFile itemFile = ItemLoader.loadItemFile(itemFileText);
            
            if (itemFile.getItems().size() != 1)
                throw new IOException("Expected " + itemId + ".wog2 file to contain 1 item, not " + itemFile.getItems().size());
            
            ObjectNode itemJson = (ObjectNode) itemFile.getItems().get(0);
            Item item = ItemLoader.loadItem(itemJson);
            
            boolean itemModified = false;
            
            for (ItemObject object : item.getObjects()) {
                String newName = analyzeAsset(res, object.getName(), AssetType.ITEM);
                
                if (!newName.equals(object.getName())) {
                    itemModified = true;
                    object.setName(newName);
                }
            }
            
            if (originalItemFileText.isPresent()) {
                if (itemModified || !itemFileText.equals(originalItemFileText.get())) {
                    String newItemId = UUID.randomUUID().toString();
                    modifiedItemIds.put(itemId, newItemId);
                    
                    JsonNode serializedObjects = jsonMapper.valueToTree(item.getObjects());
                    itemJson.put("uuid", newItemId);
                    itemJson.set("objects", serializedObjects);
                    
                    ItemFile newItemFile = new ItemFile(List.of(itemJson));
                    
                    String newItemFileText = ItemLoader.saveItemFile(newItemFile);
                    compiledResources.add(new CompiledResource(CompileType.ITEM, newItemId, newItemFileText));
                }
            } else {
                JsonNode serializedObjects = jsonMapper.valueToTree(item.getObjects());
                itemJson.set("objects", serializedObjects);
                
                ItemFile newItemFile = new ItemFile(List.of(itemJson));
                String newItemFileText = ItemLoader.saveItemFile(newItemFile);
                
                compiledResources.add(new CompiledResource(CompileType.ITEM, itemId, newItemFileText));
            }
        }
        
        for (LevelItem item : level.getItems()) {
            item.setType(modifiedItemIds.getOrDefault(item.getType(), item.getType()));
        }
        
        levelJson.set("items", jsonMapper.valueToTree(level.getItems()));
        
        // Music
        String newMusicId = analyzeAsset(res, level.getMusicId(), AssetType.MUSIC);
        levelJson.put("musicId", newMusicId);
        
        // Serialize level
        String newLevelContent = LevelLoader.saveLevel(levelJson);
        compiledResources.add(new CompiledResource(CompileType.LEVEL, level.getUuid(), newLevelContent));
        
        // Create addin.xml
        Goo2mod mod = new Goo2mod(new VersionNumber(2, 2), addinInfo.modId(), addinInfo.name(), ModType.LEVEL,
                addinInfo.version(), addinInfo.description(), addinInfo.author());
        
        mod.getLevels().add(new Goo2mod.Level(level.getUuid(), addinThumbnailPath));
        
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        String addinXml = mapper.writer().withRootName("addin").writeValueAsString(mod);
        
        // Write zip file
        writeZipFile(outputPath, addinXml, textPatch);
    }
    
    private ResrcGroup loadResrcManifest(AssetType type) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        String customWog2 = properties.getTargetWog2Directory();
        
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
        
        Properties properties = PropertiesLoader.getProperties();
        String customWog2 = properties.getTargetWog2Directory();
        
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
    
    private void writeZipFile(Path outputPath, String addinXml, TextDB textPatch) throws IOException {
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
                    default:
                        throw new RuntimeException();
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
                    if (asset.type() == type) {
                        switch (type) {
                            case ENVIRONMENT:
                            case ENVIRONMENT_LUT:
                            case ITEM:
                                resources.add(new Resrc.Image(asset.id(), asset.name()));
                                break;
                            case MUSIC:
                                resources.add(new Resrc.Sound(asset.id(), asset.name(), true, "Music"));
                                break;
                            default:
                                break;
                        }
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
        }
    }
    
}