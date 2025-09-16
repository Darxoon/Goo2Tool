package com.crazine.goo2tool.functional.export;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
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
import com.crazine.goo2tool.gamefiles.level.Level;
import com.crazine.goo2tool.gamefiles.level.LevelLoader;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    
    private final Stage stage;
    private final Path levelPath;
    private Path outputPath;
    
    private List<CompiledResource> compiledResources = new ArrayList<>();
    
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
        
        // Background
        String background = Files.readString(Paths.get(customWog2, "game/res/environments", level.getBackgroundId() + ".wog2"));
        Optional<String> originalBackground = res.getFileText("res/environments/" + level.getBackgroundId() + ".wog2");
        
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
        System.out.println(addinXml);
        
        // Write zip file
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
        }
    }
    
}