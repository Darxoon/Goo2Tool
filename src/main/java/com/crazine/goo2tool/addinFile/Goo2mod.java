package com.crazine.goo2tool.addinFile;

import com.crazine.goo2tool.VersionNumber;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    
    public static final class Depends {
        
        @JacksonXmlText
        private String id;
        @JsonProperty("min-version")
        private VersionNumber minVersion;
        @JsonProperty("max-version")
        private VersionNumber maxVersion;
        
        @SuppressWarnings("unused")
        private Depends() {}
        
        public Depends(String id, VersionNumber minVersion, VersionNumber maxVersion) {
            this.id = id;
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
        }

        public String getId() {
            return id;
        }
        public VersionNumber getMinVersion() {
            return minVersion;
        }
        public VersionNumber getMaxVersion() {
            return maxVersion;
        }
        
    }
    
    public static record Level(String filename, @JsonProperty(required = false) String thumbnail) {}
    
    public static final VersionNumber MIN_SPEC_VERSION = new VersionNumber(2, 2);
    public static final VersionNumber MAX_SPEC_VERSION = new VersionNumber(2, 2);
    
    @JacksonXmlProperty(isAttribute = true, localName = "spec-version")
    private VersionNumber specVersion;
    
    private String id;
    private String name;
    private ModType type;
    private String version;
    private String description;
    private String author;
    
    @JacksonXmlElementWrapper(localName = "dependencies")
    @JacksonXmlProperty(localName = "depends")
    private List<Depends> dependencies = new ArrayList<>();
    
    @JacksonXmlElementWrapper(localName = "levels")
    @JacksonXmlProperty(localName = "level")
    private List<Level> levels = new ArrayList<>();
    
    @JsonIgnore
    private File file;

    @JsonCreator
    private Goo2mod(@JsonProperty("spec-version") VersionNumber specVersion) {
        if (MIN_SPEC_VERSION.compareTo(specVersion) > 0) {
            throw new IllegalArgumentException("spec-version " + specVersion
                    + " not supported, minimum supported version is " + MIN_SPEC_VERSION);
        } else if (MAX_SPEC_VERSION.compareTo(specVersion) < 0) {
            throw new IllegalArgumentException("spec-version " + specVersion
                    + " is too new, maximum supported version is " + MAX_SPEC_VERSION
                    + ". You are probably using an outdated version of Goo2Tool"
                    + " and should update to the newest version!");
        }
        
        this.specVersion = specVersion;
    }
    
    public Goo2mod(VersionNumber specVersion, String id, String name, ModType type,
                String version, String description, String author) {
        this.specVersion = specVersion;
        this.id = id;
        this.name = name;
        this.type = type;
        this.version = version;
        this.description = description;
        this.author = author;
        
        this.levels = new ArrayList<>();
    }

    
    public VersionNumber getSpecVersion() {
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

    public List<Depends> getDependencies() {
        return dependencies;
    }
    
    public Optional<Depends> getDependency(String id) {
        for (Depends dependency : dependencies) {
            if (dependency.getId().equals(id))
                return Optional.of(dependency);
        }
        
        return Optional.empty();
    }
    
    public List<Level> getLevels() {
        return levels;
    }
    
    public Optional<Level> getLevel(String filename) {
        for (Level level : levels) {
            if (level.filename.equals(filename))
                return Optional.of(level);
        }
        
        return Optional.empty();
    }
    
    public boolean isThumbnail(String filename) {
        for (Level level : levels) {
            if (level.thumbnail() != null && level.thumbnail().equals(filename))
                return true;
        }
        
        return false;
    }
    
    public File getFile() {
        return file;
    }
    void setFile(File file) {
        this.file = file;
    }

}
