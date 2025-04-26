package com.crazine.goo2tool.gamefiles.translation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("localized_text_db")
public class TextDB {
    
    @JacksonXmlElementWrapper(localName = "strings")
    @JacksonXmlProperty(localName = "string")
    private List<GameString> strings;
    
    public TextDB() {
        this.strings = new ArrayList<>();
    }

    public TextDB(List<GameString> strings) {
        this.strings = new ArrayList<>(strings);
    }
    
    public void putString(GameString string) {
        for (int i = 0; i < strings.size(); i++) {
            GameString current = strings.get(i);
            
            if (current.getId().equals(string.getId())) {
                strings.set(i, string);
                return;
            }
        }
        
        strings.add(string);
    }
    
    public void removeString(String id) {
        for (int i = 0; i < strings.size(); i++) {
            GameString current = strings.get(i);
            
            if (current.getId().equals(id)) {
                strings.remove(i);
                return;
            }
        }
    }

    public List<GameString> getStrings() {
        return strings;
    }
    
    public void setStrings(List<GameString> strings) {
        this.strings = strings;
    }
    
}
