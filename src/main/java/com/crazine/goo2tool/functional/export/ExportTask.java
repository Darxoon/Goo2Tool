package com.crazine.goo2tool.functional.export;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
        ENVIRONMENT("environments", "res/environments/images/_resources.xml",
            new Resrc.SetDefaults("res/environments/images/", "ENV_BG_"));
        
        public final String groupId;
        public final String resrcFile;
        public final Resrc.SetDefaults setDefaults;
        
        private AssetType(String groupId, String resrcFile, Resrc.SetDefaults setDefaults) {
            this.groupId = groupId;
            this.resrcFile = resrcFile;
            this.setDefaults = setDefaults;
        }
    }
    
    private static record AssetResource(AssetType type, String id, String name, byte[] content) {}
    
    private final Stage stage;
    private final Path levelPath;
    private Path outputPath;
    
    private ResrcGroup environmentResrc;
    private ResrcGroup originalEnvironmentResrc;
    
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
        environmentResrc = loadResrcManifest(AssetType.ENVIRONMENT);
        originalEnvironmentResrc = loadOriginalResrcManifest(res, AssetType.ENVIRONMENT);
        
        // Background
        String background = Files.readString(Paths.get(customWog2, "game/res/environments", level.getBackgroundId() + ".wog2"));
        Optional<String> originalBackground = res.getFileText("res/environments/" + level.getBackgroundId() + ".wog2");
        
        JsonMapper jsonMapper = new JsonMapper();
        JsonNode backgroundValue = jsonMapper.readTree(background);
        
        analyzeBackground(backgroundValue);
        
        if (originalBackground.isEmpty()) {
            compiledResources.add(new CompiledResource(CompileType.ENVIRONMENT, level.getBackgroundId(), background));
        }
        
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
    
    private void analyzeBackground(JsonNode background) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        String customWog2 = properties.getTargetWog2Directory();
        
        Environment environment = EnvironmentLoader.loadBackground(background);
        
        for (Environment.Layer layer : environment.getLayers()) {
            String imageId = layer.getImageName();
            
            Optional<Resrc> imageResrc = environmentResrc.getResource(imageId);
            Optional<String> imagePath = environmentResrc.getResourcePath(imageId);
            
            if (imageResrc.isEmpty() || imagePath.isEmpty())
                throw new IOException("Could not find image with ID '"
                        + imageId + "'");
            
            if (!(imageResrc.get() instanceof Resrc.Image image))
                throw new IOException("Resource with ID '"
                        + imageId + "' should be an image");
            
            Optional<Resrc> originalImageResrc = originalEnvironmentResrc.getResource(imageId);
            
            if (originalImageResrc.isEmpty()) {
                byte[] imageContent = Files.readAllBytes(Paths.get(customWog2, "game", imagePath.get() + ".image"));
                
                // TODO: image.id and image.path might be inaccurate if user used a different SetDefaults
                assetResources.add(new AssetResource(AssetType.ENVIRONMENT, image.id(), image.path(), imageContent));
            }
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
                        resources.add(new Resrc.Image(asset.id(), asset.name()));
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
                String entryPath = "override/" + setDefaults.path() + asset.name() + ".image";
                
                zip.putNextEntry(new ZipEntry(entryPath));
                zip.write(asset.content());
                zip.closeEntry();
            }
        }
    }
    
}