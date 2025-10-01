package com.crazine.goo2tool.gui.export.addininfocache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crazine.goo2tool.gui.export.FX_ExportDialog.AddinInfo;

public class AddinInfoCache {
    
    public static record Entry(String uuid, String title, AddinInfo info) {}
    
    private List<Entry> entries = new ArrayList<>();
    
    public void addEntry(String uuid, String title, AddinInfo info)  {
        // Check if entry already exists and replace it in that case
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            
            if (entry.uuid().equals(uuid) && entry.title().equals(title)) {
                entries.set(i, new Entry(uuid, title, info));
                return;
            }
        }
        
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
