package com.crazine.goo2tool.properties;

import com.crazine.goo2tool.addinfile.AddinFileLoader;
import com.crazine.goo2tool.addinfile.Goo2mod;
import com.crazine.goo2tool.gui.FX_Mods;
import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.util.Platform;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;

public class PropertiesLoader {

    private static Properties properties;
    public static Properties getProperties() {
        return properties;
    }
    
    public static boolean allImportantInitialized() {
        return isValidBaseWog2(properties.getBaseWorldOfGoo2Directory())
            && (properties.isSteam() || isValidDir(properties.getCustomWorldOfGoo2Directory()));
    }

    public static boolean isValidBaseWog2(String pathStr) {
        if (pathStr == null || pathStr.isEmpty())
            return false;
        
        Path path = Path.of(pathStr);
        return Files.isDirectory(path)
            || (pathStr.endsWith(".AppImage") && Files.isRegularFile(path));
    }
    
    public static boolean isValidDir(String path) {
        return path != null
            && !path.isEmpty()
            && Files.isDirectory(Paths.get(path));
    }

    private static final File propertiesFile = new File(getGoo2ToolPath() + "/properties.xml");
    public static File getPropertiesFile() {
        return propertiesFile;
    }


    public static Properties loadProperties(File propertiesFile) throws IOException {

        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(propertiesFile, Properties.class);

    }


    public static void saveProperties() throws IOException {

        if (!Files.exists(propertiesFile.toPath())) Files.createFile(propertiesFile.toPath());
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.writeValue(new FileWriter(propertiesFile), properties);

    }


    public static void init() {
        if (!Files.exists(propertiesFile.getParentFile().toPath())) {
            try {
                Files.createDirectory(propertiesFile.getParentFile().toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (Files.exists(propertiesFile.toPath())) {
            try {
                properties = loadProperties(propertiesFile);
            } catch (IOException e) {
                e.printStackTrace();
                properties = new Properties();
            }
        } else {
            properties = new Properties();
        }

    }
    
    public static void loadGoo2mod(File goo2modFile) throws IOException {
        Goo2mod goo2mod = AddinFileLoader.loadGoo2mod(goo2modFile);
        
        // delete all existing mods with the same id
        Path addinsDir = Paths.get(PropertiesLoader.getGoo2ToolPath(), "addins");
        for (File child : addinsDir.toFile().listFiles()) {
            Goo2mod currentMod = AddinFileLoader.loadGoo2mod(child);
            
            if (currentMod.getId().equals(goo2mod.getId()))
                child.delete();
        }
        
        // install new mod
        if (!properties.hasAddin(goo2mod.getId())) {
            AddinConfigEntry addin = new AddinConfigEntry();
            addin.setId(goo2mod.getId());
            addin.setLoaded(false);
            properties.getAddins().add(addin);
        }
        
        FX_Mods.getModTableView().getItems().removeAll(FX_Mods.getModTableView().getItems()
                .stream()
                .filter(item -> item.getId().equals(goo2mod.getId()))
                .toList());
        
        FX_Mods.getModTableView().getItems().add(goo2mod);
        FX_Mods.getModTableView().getSelectionModel().select(goo2mod);

        Path newPath = addinsDir.resolve(goo2modFile.getName());
        Files.copy(goo2modFile.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
        
        saveProperties();
    }

    public static void uninstallGoo2mod(Goo2mod mod) {
        FX_Mods.getModTableView().getItems().remove(mod);
        
        Optional<AddinConfigEntry> addin = getProperties().getAddin(mod);
        if (addin.isPresent()) {
            getProperties().getAddins().remove(addin.get());
        }
        
        Path addinsDir = Paths.get(PropertiesLoader.getGoo2ToolPath(), "addins");
        
        try (Stream<Path> stream = Files.walk(addinsDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                try {
                    Goo2mod currentMod = AddinFileLoader.loadGoo2mod(path.toFile());
                    
                    if (currentMod.getId().equals(mod.getId())) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    FX_Alarm.error(e);
                }
            });
        } catch (IOException e) {
            FX_Alarm.error(e);
        }
        
        try {
            saveProperties();
        } catch (IOException e) {
            FX_Alarm.error(e);
        }
    }

    public static String getGoo2ToolPath() {
        
        return switch (Platform.getCurrent()) {
            case WINDOWS -> System.getenv("APPDATA").replaceAll("\\\\", "/") + "/Goo2Tool";
            case MAC -> System.getProperty("user.home") + "/Library/Application Support/Goo2Tool";
            case LINUX -> System.getProperty("user.home") + "/.local/share/Goo2Tool";
        };

    }

}
