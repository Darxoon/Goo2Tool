package com.crazine.goo2tool.addinFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Goo2mod {
    
    public static enum ModType {
        @JsonProperty("mod")
        MOD("mod"),
        @JsonProperty("level")
        LEVEL("level");
        
        private String name;
        
        ModType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    public static record Level(String filename, String name) {}
    
    @JacksonXmlProperty(isAttribute = true, localName = "spec-version")
    private String specVersion;
    
    private String id;
    private String name;
    private ModType type;
    private String version;
    private String description;
    private String author;
    
    @JacksonXmlElementWrapper(localName = "levels")
    @JacksonXmlProperty(localName = "level")
    private List<Level> levels = new ArrayList<>();
    
    @JsonIgnore
    private File file;

    public String getSpecVersion() {
        return specVersion;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public ModType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public List<Level> getLevels() {
        return levels;
    }
    
    public File getFile() {
        return file;
    }
    void setFile(File file) {
        this.file = file;
    }

}
