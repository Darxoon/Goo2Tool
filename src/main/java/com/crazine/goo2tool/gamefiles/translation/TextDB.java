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
    private final Map<String, GameString> idMap = new HashMap<>();

    public TextDB() {}

    public TextDB(List<GameString> strings) {
        setStrings(strings);
    }
    
    public void putString(GameString string) {
        if (idMap.containsKey(string.getId())) {
            int index = strings.indexOf(idMap.get(string.getId()));
            strings.set(index, string);
        } else {
            strings.add(string);
        }

        idMap.put(string.getId(), string);
    }
    
    public Optional<GameString> getString(String id) {
        return Optional.ofNullable(idMap.get(id));
    }
    
    public void removeString(String id) {
        if (idMap.containsKey(id)) {
            GameString string = idMap.get(id);
            strings.remove(string);
            idMap.remove(id);
        }
    }

    public List<GameString> getStrings() {
        return Collections.unmodifiableList(strings);
    }
    
    public void setStrings(List<GameString> strings) {
        this.strings.clear();
        this.idMap.clear();
        
        for (GameString string : strings) {
            idMap.put(string.getId(), string);
            this.strings.add(string);
        }
    }
    
}
