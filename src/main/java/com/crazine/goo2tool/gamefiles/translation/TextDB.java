package com.crazine.goo2tool.gamefiles.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("localized_text_db")
public class TextDB {
    
    @JacksonXmlElementWrapper(localName = "strings")
    @JacksonXmlProperty(localName = "string")
    private List<GameString> strings = new ArrayList<>();
    
    @JsonIgnore
    private Map<String, Integer> indexMap = new HashMap<>();
    
    public TextDB() {}

    public TextDB(List<GameString> strings) {
        setStrings(strings);
    }
    
    public void putString(GameString string) {
        if (indexMap.containsKey(string.getId())) {
            int index = indexMap.get(string.getId());
            strings.set(index, string);
        } else {
            indexMap.put(string.getId(), this.strings.size());
            strings.add(string);
        }
    }
    
    public Optional<GameString> getString(String id) {
        if (indexMap.containsKey(id)) {
            int index = indexMap.get(id);
            return Optional.ofNullable(strings.get(index));
        } else {
            return Optional.empty();
        }
    }
    
    public void removeString(String id) {
        if (indexMap.containsKey(id)) {
            int index = indexMap.get(id);
            strings.remove(index);
            indexMap.remove(id);
        }
    }

    public List<GameString> getStrings() {
        return Collections.unmodifiableList(strings);
    }
    
    public void setStrings(List<GameString> strings) {
        this.strings.clear();
        this.indexMap.clear();
        
        for (GameString string : strings) {
            indexMap.put(string.getId(), this.strings.size());
            this.strings.add(string);
        }
    }
    
}
