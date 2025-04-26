package com.crazine.goo2tool.gamefiles.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class GameString {
    
    public static class LocaleText {
        @JacksonXmlProperty(isAttribute = true)
        private String language;
        
        @JacksonXmlText
        private String text;
        
        public LocaleText() {}
        
        public LocaleText(String language, String text) {
            this.language = language;
            this.text = text;
        }
        
        public String getLanguage() {
            return language;
        }
        public void setLanguage(String language) {
            this.language = language;
        }
        
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
    }
    
    private String id;
    
    @JacksonXmlElementWrapper(localName = "texts")
    @JacksonXmlProperty(localName = "text")
    private List<LocaleText> texts;
    
    public GameString() {
        this.texts = new ArrayList<>();
    }

    public GameString(String id, List<LocaleText> texts) {
        this.id = id;
        this.texts = new ArrayList<>(texts);
    }
    
    public GameString(String id, LocaleText... texts) {
        this.id = id;
        this.texts = new ArrayList<>(List.of(texts));
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public List<LocaleText> getTexts() {
        return texts;
    }
    public void setTexts(List<LocaleText> texts) {
        this.texts = texts;
    }
    
    @JsonIgnore
    public Optional<LocaleText> getLocal() {
        for (LocaleText text : texts) {
            if (text.language.equals("en")) {
                return Optional.of(text);
            }
        }
        
        return Optional.empty();
    }
    
    @JsonIgnore
    public boolean hasIntl() {
        for (LocaleText text : texts) {
            if (!text.language.equals("en")) {
                return true;
            }
        }
        
        return false;
    }
    
    @JsonIgnore
    public List<LocaleText> getIntl() {
        List<LocaleText> out = new ArrayList<>();
        
        for (LocaleText text : texts) {
            if (!text.language.equals("en")) {
                out.add(text);
            }
        }
        
        return out;
    }
    
}
