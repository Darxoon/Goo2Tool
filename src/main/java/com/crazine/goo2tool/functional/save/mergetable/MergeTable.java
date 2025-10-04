package com.crazine.goo2tool.functional.save.mergetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public abstract class MergeTable<T> {
    
    public static enum MergeType {
        @JsonProperty("resource_xml")
        RESOURCE_XML,
        @JsonProperty("translation_xml")
        TRANSLATION_XML,
    }
    
    @JsonInclude(Include.NON_NULL)
    public static class MergeEntry<T> {
        
        @JacksonXmlProperty(isAttribute = true) private String group;
        @JacksonXmlProperty(isAttribute = true) private String id;
        @JacksonXmlProperty(isAttribute = true) private String modId;
        
        @JacksonXmlProperty(localName = "OriginalValue")
        private T originalValue;
        
        @JacksonXmlProperty(localName = "Value")
        private T modValue;
        
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
        
        public T getOriginalValue() {
            return originalValue;
        }
        public void setOriginalValue(T originalValue) {
            this.originalValue = originalValue;
        }
        
        public T getModValue() {
            return modValue;
        }
        public void setModValue(T modValue) {
            this.modValue = modValue;
        }
        
    }
    
    public static class MergeFile<T> {
        
        @JacksonXmlProperty(isAttribute = true)
        private String path;
        
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "ModOverride")
        private List<MergeEntry<T>> entries = new ArrayList<>();
        
        public MergeFile(@JsonProperty("path") String path) {
            this.path = path;
        }
        
        public MergeEntry<T> getOrAddEntry(String group, String id, String modId) {
            MergeEntry<T> entry = null;
            
            for (MergeEntry<T> currentEntry : entries) {
                if (Objects.equals(currentEntry.getGroup(), group) && currentEntry.getId().equals(id)) {
                    entry = currentEntry;
                    break;
                }
            }
            
            if (entry == null) {
                entry = new MergeEntry<T>(group, id, modId);
                entries.add(entry);
            }
            
            return entry;
        }

        public String getPath() {
            return path;
        }
        
        public List<MergeEntry<T>> getEntries() {
            return entries;
        }
        public void setEntries(List<MergeEntry<T>> resrcs) {
            this.entries = resrcs;
        }
        
    }
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "File")
    private List<MergeFile<T>> files = new ArrayList<>();
    
    public Optional<MergeFile<T>> getFile(String path) {
        for (MergeFile<T> file : files) {
            if (file.getPath().equals(path))
                return Optional.of(file);
        }
        
        return Optional.empty();
    }
    
    public MergeFile<T> getOrAddFile(String path, Supplier<MergeFile<T>> factory) {
        for (MergeFile<T> file : files) {
            if (file.getPath().equals(path))
                return file;
        }
        
        MergeFile<T> newFile = factory.get();
        files.add(newFile);
        return newFile;
    }
    
    public MergeFile<T> addFile(MergeFile<T> file) {
        files.add(file);
        return file;
    }
    
    @JacksonXmlProperty(isAttribute = true)
    public abstract MergeType getType();
    
    public List<MergeFile<T>> getFiles() {
        return files;
    }

    public void setFiles(List<MergeFile<T>> files) {
        this.files = files;
    }
    
}
