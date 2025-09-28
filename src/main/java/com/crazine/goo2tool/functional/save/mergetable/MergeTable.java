package com.crazine.goo2tool.functional.save.mergetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.Resrc.SetDefaults;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class MergeTable {
    
    public static enum MergeType {
        @JsonProperty("resource_xml")
        RESOURCE_XML,
    }
    
    @JsonDeserialize(using = MergeTableLoader.MergeValueDeserializer.class)
    @JsonSerialize(using = MergeTableLoader.MergeValueSerializer.class)
    public static class MergeValue {
        
        private Resrc.SetDefaults setDefaults;
        private Resrc value;
        
        public MergeValue(SetDefaults setDefaults, Resrc value) {
            this.setDefaults = setDefaults;
            this.value = value;
        }
        
        public Resrc.SetDefaults getSetDefaults() {
            return setDefaults;
        }
        public Resrc getValue() {
            return value;
        }
        
    }
    
    @JsonInclude(Include.NON_NULL)
    public static class MergeEntry {
        
        @JacksonXmlProperty(isAttribute = true) private String group;
        @JacksonXmlProperty(isAttribute = true) private String id;
        @JacksonXmlProperty(isAttribute = true) private String modId;
        
        @JacksonXmlProperty(localName = "OriginalValue")
        private MergeValue originalValue;
        
        @JacksonXmlProperty(localName = "Value")
        private MergeValue modValue;
        
        public MergeEntry(@JsonProperty("group") String group,
                @JsonProperty("id") String id, @JsonProperty("modId") String modId) {
            this.group = group;
            this.id = id;
            
            if (modId != null)
                this.modId = modId;
            else
                this.modId = "";
        }
        
        public String getGroup() {
            return group;
        }
        public String getId() {
            return id;
        }
        public String getModId() {
            return modId;
        }
        
        public MergeValue getOriginalValue() {
            return originalValue;
        }
        public void setOriginalValue(MergeValue originalValue) {
            this.originalValue = originalValue;
        }
        
        public MergeValue getModValue() {
            return modValue;
        }
        public void setModValue(MergeValue modValue) {
            this.modValue = modValue;
        }
        
    }
    
    public static class MergeFile {
        
        @JacksonXmlProperty(isAttribute = true)
        private String path;
        @JacksonXmlProperty(isAttribute = true)
        private MergeType type;
        
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "ModOverride")
        private List<MergeEntry> entries = new ArrayList<>();
        
        public MergeFile(@JsonProperty("path") String path, @JsonProperty("type") MergeType type) {
            this.path = path;
            this.type = type;
        }
        
        public MergeEntry getOrAddEntry(String group, String id, String modId) {
            MergeEntry entry = null;
            
            for (MergeEntry currentEntry : entries) {
                if (currentEntry.getGroup().equals(group) && currentEntry.getId().equals(id)) {
                    entry = currentEntry;
                    break;
                }
            }
            
            if (entry == null) {
                entry = new MergeEntry(group, id, modId);
                entries.add(entry);
            }
            
            return entry;
        }

        public String getPath() {
            return path;
        }
        public MergeType getType() {
            return type;
        }
        
        public List<MergeEntry> getEntries() {
            return entries;
        }
        public void setEntries(List<MergeEntry> resrcs) {
            this.entries = resrcs;
        }
        
    }
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "File")
    private List<MergeFile> files = new ArrayList<>();
    
    public Optional<MergeFile> getFile(String path) {
        for (MergeFile file : files) {
            if (file.getPath().equals(path))
                return Optional.of(file);
        }
        
        return Optional.empty();
    }
    
    public MergeFile getOrAddFile(String path, Supplier<MergeFile> factory) {
        for (MergeFile file : files) {
            if (file.getPath().equals(path))
                return file;
        }
        
        MergeFile newFile = factory.get();
        files.add(newFile);
        return newFile;
    }
    
    public MergeFile addFile(MergeFile file) {
        files.add(file);
        return file;
    }

    public List<MergeFile> getFiles() {
        return files;
    }

    public void setFiles(List<MergeFile> files) {
        this.files = files;
    }
    
}
