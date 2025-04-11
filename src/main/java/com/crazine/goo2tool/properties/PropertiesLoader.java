package com.crazine.goo2tool.properties;

import com.crazine.goo2tool.Platform;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

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


    public static String getGoo2ToolPath() {
        
        return switch (Platform.getCurrent()) {
            case WINDOWS -> System.getenv("APPDATA").replaceAll("\\\\", "/") + "/Goo2Tool";
            case MAC -> System.getProperty("user.home").replaceAll("\\\\", "/") + "/Library/Goo2Tool";
            case LINUX -> System.getProperty("user.home").replaceAll("\\\\", "/") + "/.config/Goo2Tool";
        };

    }

}
