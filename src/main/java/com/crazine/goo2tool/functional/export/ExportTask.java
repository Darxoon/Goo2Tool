package com.crazine.goo2tool.functional.export;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.addinFile.Goo2mod.ModType;
import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.gamefiles.environment.Environment;
import com.crazine.goo2tool.gamefiles.environment.EnvironmentLoader;
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.ResrcGroup;
import com.crazine.goo2tool.gamefiles.resrc.ResrcLoader;
import com.crazine.goo2tool.gamefiles.resrc.ResrcManifest;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javafx.concurrent.Task;
import javafx.stage.Stage;

class ExportTask extends Task<Void> {
    
    private static record PathResrc<T extends Resrc>(T resrc, String fullPath) {}
    
    private static enum CompileType {
        LEVEL,
        ENVIRONMENT,
        // ITEM,
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
        // ENVIRONMENT_LUT,
        // ITEM,
        // ITEM_PREVIEW,
        // PARTICLES,
        // SOUND,
        // TERRAIN,
        // TERRAIN_DECORATION,
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
    private final Path levelPath;
    private Path outputPath;
    
    private Map<AssetType, ResrcGroup> customResrcs = new HashMap<>();
    private Map<AssetType, ResrcGroup> originalResrcs = new HashMap<>();
    
    private List<CompiledResource> compiledResources = new ArrayList<>();
    private List<AssetResource> assetResources = new ArrayList<>();
    
    private boolean success = true;

    ExportTask(Stage stage, Path levelPath, Path outputPath) {
        this.stage = stage;
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
        
        String levelContent = Files.readString(levelPath);
        Level level = LevelLoader.loadLevel(levelContent);
        
        compiledResources.add(new CompiledResource(CompileType.LEVEL, level.getUuid(), levelContent));
        
        // Load resrc files
        for (AssetType type : AssetType.values()) {
            customResrcs.put(type, loadResrcManifest(type));
            originalResrcs.put(type, loadOriginalResrcManifest(res, type));
        }
        
        // Background
        String backgroundText = Files.readString(Paths.get(customWog2, "game/res/environments", level.getBackgroundId() + ".wog2"));
        Optional<String> originalBackground = res.getFileText("res/environments/" + level.getBackgroundId() + ".wog2");
        
        JsonMapper jsonMapper = new JsonMapper();
        JsonNode backgroundValue = jsonMapper.readTree(backgroundText);
        Environment background = EnvironmentLoader.loadBackground(backgroundValue);
        
        for (Environment.Layer layer : background.getLayers()) {
            analyzeAsset(layer.getImageName(), AssetType.ENVIRONMENT);
        }
        
        if (originalBackground.isEmpty()) {
            compiledResources.add(new CompiledResource(CompileType.ENVIRONMENT, level.getBackgroundId(), backgroundText));
        }
        
        // Music
        analyzeAsset(level.getMusicId(), AssetType.MUSIC);
        
        // Create addin.xml
        // TODO: ask the user for actual values
        String modId = "sampleid." + level.getTitle().replace(" ", "");
        Goo2mod mod = new Goo2mod("2.2", modId, level.getTitle(), ModType.LEVEL,
                "1.0", "", "Sample Author");
        
        mod.getLevels().add(new Goo2mod.Level(level.getUuid(), null));
        
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        String addinXml = mapper.writer().withRootName("addin").writeValueAsString(mod);
        
        // Write zip file
        writeZipFile(outputPath, addinXml);
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
    
    private void analyzeAsset(String id, AssetType type) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        String customWog2 = properties.getTargetWog2Directory();
        
        ResrcGroup group = customResrcs.get(type);
        ResrcGroup originalGroup = originalResrcs.get(type);
        
        PathResrc<? extends Resrc> resrc = getResrc(id, group, type.resrcClass);
        Optional<Resrc> originalResrc = originalGroup.getResource(id);
        
        if (originalResrc.isEmpty()) {
            Path contentPath = Paths.get(customWog2, "game", resrc.fullPath() + "." + resrc.resrc.fileExtension());
            byte[] content = Files.readAllBytes(contentPath);
            
            // TODO: id() and path() might be inaccurate if user used a different SetDefaults
            assetResources.add(new AssetResource(type, resrc.resrc().id(), resrc.resrc().path(), content));
        }
    }
    
    private void writeZipFile(Path outputPath, String addinXml) throws IOException {
        FileOutputStream fileOutputStream  = new FileOutputStream(outputPath.toFile());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        
        try (ZipOutputStream zip = new ZipOutputStream(bufferedOutputStream)) {
            // addin.xml
            zip.putNextEntry(new ZipEntry("addin.xml"));
            zip.write(addinXml.getBytes());
            zip.closeEntry();
            
            // TODO: translation.xml
            
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
                    default:
                        throw new RuntimeException();
                }
                
                zip.putNextEntry(new ZipEntry(entryPath));
                zip.write(resource.content().getBytes());
                zip.closeEntry();
            }
            
            // resrc.xml files
            for (AssetType type : AssetType.values()) {
                List<Resrc> resources = new ArrayList<>();
                resources.add(type.setDefaults);
                
                for (AssetResource asset : assetResources) {
                    if (asset.type() == type) {
                        switch (type) {
                            case ENVIRONMENT:
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
                
                ResrcGroup group = new ResrcGroup(type.groupId, resources);
                ResrcManifest manifest = new ResrcManifest(group);
                
                zip.putNextEntry(new ZipEntry("merge/" + type.resrcFile));
                zip.write(ResrcLoader.saveManifest(manifest));
                zip.closeEntry();
            }
            
            // override directory
            for (AssetResource asset : assetResources) {
                Resrc.SetDefaults setDefaults = asset.type().setDefaults;
                String fileExtension = Resrc.getFileExtension(asset.type.resrcClass);
                String entryPath = "override/" + setDefaults.path() + asset.name() + "." + fileExtension;
                
                zip.putNextEntry(new ZipEntry(entryPath));
                zip.write(asset.content());
                zip.closeEntry();
            }
        }
    }
    
}