package com.crazine.goo2tool.properties;

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


    private static final File propertiesFile = new File(System.getenv("APPDATA") + "/Goo2Tool/properties.xml");
    public static File getPropertiesFile() {
        return propertiesFile;
    }


    public static Properties loadProperties(File propertiesFile) throws IOException {

        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(propertiesFile, Properties.class);

    }


    public static void saveProperties(File propertiesFile, Properties properties) throws IOException {

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.writeValue(new FileWriter(propertiesFile), properties);

    }


    public static void init() {

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

}
