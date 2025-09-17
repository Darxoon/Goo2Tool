package com.crazine.goo2tool.functional.save.filetable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class ResFileTable {
    
    // this used to be a record but Jackson didn't like that >:(
    public static class OverriddenFileEntry {
        
        @JacksonXmlProperty(isAttribute = true)
        private String modId;
        
        @JacksonXmlText
        private String path;
        
        public OverriddenFileEntry() {}
        
        public OverriddenFileEntry(String modId, String path) {
            this.modId = modId;
            this.path = path;
        }

        public String getModId() {
            return modId;
        }
        
        public void setModId(String modId) {
            this.modId = modId;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
    }
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "File")
    private Map<String, OverriddenFileEntry> entries = new HashMap<>();
    
    @JacksonXmlProperty()
    public List<OverriddenFileEntry> getEntries() {
        return new ArrayList<>(entries.values());
    }
    
    @JacksonXmlProperty()
    public void setEntries(List<OverriddenFileEntry> entries) {
        this.entries.clear();
        
        for (OverriddenFileEntry entry : entries) {
            this.entries.put(entry.path, entry);
        }
    }
    
    public void addEntry(String owningModId, String path) {
        entries.put(path, new OverriddenFileEntry(owningModId, path));
    }
    
    public void addEntry(OverriddenFileEntry entry) {
        entries.put(entry.path, entry);
    }
    
    public Optional<OverriddenFileEntry> getEntry(String path) {
        return Optional.ofNullable(entries.get(path));
    }
    
    public boolean hasEntry(String path) {
        return entries.containsKey(path);
    }
    
    public void removeEntry(String path) {
        entries.remove(path);
    }
    
    public void removeEntry(OverriddenFileEntry entry) {
        entries.remove(entry.getPath());
    }
    
}
