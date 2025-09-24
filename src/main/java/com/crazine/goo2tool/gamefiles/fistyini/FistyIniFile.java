package com.crazine.goo2tool.gamefiles.fistyini;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FistyIniFile {
    
    private String sourceFile;
    private List<String> entries;
    private Map<String, Integer> typeEnumMap = new HashMap<>();
    
    public FistyIniFile(String sourceFile, List<String> entries) {
        if (!sourceFile.endsWith("\n"))
            sourceFile += "\n";
        
        this.sourceFile = sourceFile;
        this.entries = new ArrayList<>(entries);
        
        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i);
            
            if (entry != null && !entry.isEmpty())
                typeEnumMap.put(entries.get(i), i);
            else
                this.entries.set(i, "");
        }
    }
    
    public Optional<Integer> getBallId(String ballName) {
        return Optional.ofNullable(typeEnumMap.get(ballName));
    }
    
    public Optional<String> getBallName(int typeEnum) {
        if (typeEnum < 0 || typeEnum >= entries.size())
            return Optional.empty();
        
        String entry = entries.get(typeEnum);
        
        if (entry.isEmpty())
            return Optional.empty();
        else
            return Optional.of(entry);
    }
    
    public int addBallId(String ballName, int intendedId) {
        // Check if it already exists
        if (typeEnumMap.containsKey(ballName))
            return typeEnumMap.get(ballName);
        
        if (intendedId > entries.size()) {
            // Resize entries to length intendedId
            entries.addAll(Collections.nCopies(intendedId - entries.size(), ""));
            
            // Append ballName at index intendedId
            entries.add(ballName);
            addBallToSource(intendedId, ballName);
            return intendedId;
        }
        
        // Check if it can be inserted into empty slot
        for (int i = intendedId; i < entries.size(); i++) {
            if (entries.get(i).isEmpty()) {
                entries.set(i, ballName);
                addBallToSource(i, ballName);
                return i;
            }
        }
        
        // Append to the end
        int index = entries.size();
        entries.add(ballName);
        addBallToSource(index, ballName);
        return index;
    }
    
    private void addBallToSource(int typeEnum, String ballName) {
        sourceFile += typeEnum + "=" + ballName + "\n";
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    
    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }
    
}
