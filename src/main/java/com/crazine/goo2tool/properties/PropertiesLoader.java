package com.crazine.goo2tool.properties;

import com.crazine.goo2tool.Platform;
import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.gui.FX_Alarm;
import com.crazine.goo2tool.gui.FX_Mods;
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
    
    public static boolean isAllInitialized() {
        return !getProperties().getBaseWorldOfGoo2Directory().isEmpty()
            && !getProperties().getCustomWorldOfGoo2Directory().isEmpty()
            && !getProperties().getProfileDirectory().isEmpty();
    }


    private static final File propertiesFile = new File(getGoo2ToolPath() + "/properties.xml");
    public static File getPropertiesFile() {
        return propertiesFile;
    }


    public static Properties loadProperties(File propertiesFile) throws IOException {

        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(propertiesFile, Properties.class);

    }


    public static void saveProperties(File propertiesFile, Properties properties) throws IOException {

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
        
        AddinConfigEntry addin = new AddinConfigEntry();
        addin.setName(goo2mod.getId());
        addin.setLoaded(false);
        if (!PropertiesLoader.getProperties().hasAddin(addin.getName())) {
            PropertiesLoader.getProperties().getAddins().add(addin);
        }
        FX_Mods.getModTableView().getItems().add(goo2mod);
        FX_Mods.getModTableView().getSelectionModel().select(goo2mod);

        Path newPath = Paths.get(PropertiesLoader.getGoo2ToolPath(), "addins", goo2modFile.getName());
        Files.copy(goo2modFile.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
        
        try {
            saveProperties(getPropertiesFile(), getProperties());
        } catch (IOException e) {
            FX_Alarm.error(e);
        }
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
            saveProperties(getPropertiesFile(), getProperties());
        } catch (IOException e) {
            FX_Alarm.error(e);
        }
    }

    public static String getGoo2ToolPath() {
        
        return switch (Platform.getCurrent()) {
            case WINDOWS -> System.getenv("APPDATA").replaceAll("\\\\", "/") + "/Goo2Tool";
            case MAC -> System.getProperty("user.home").replaceAll("\\\\", "/") + "/Library/Goo2Tool";
            case LINUX -> System.getProperty("user.home").replaceAll("\\\\", "/") + "/.config/Goo2Tool";
        };

    }

}
