package com.crazine.goo2tool.functional.export.addininfocache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crazine.goo2tool.gui.export.FX_ExportDialog.AddinInfo;

public class AddinInfoCache {
    
    public static record Entry(String uuid, String title, AddinInfo info) {}
    
    private List<Entry> entries = new ArrayList<>();
    
    public void addEntry(String uuid, String title, AddinInfo info)  {
        entries.add(new Entry(uuid, title, info));
    }
    
    public Optional<AddinInfo> getEntry(String uuid, String title) {
        for (Entry entry : entries) {
            if (entry.uuid().equals(uuid))
                return Optional.of(entry.info());
        }
        
        for (Entry entry : entries) {
            if (entry.title().equals(uuid))
                return Optional.of(entry.info());
        }
        
        return Optional.empty();
    }
    
    public List<Entry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
    
}
