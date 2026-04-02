package com.crazine.goo2tool.gamefiles.translation;

import java.util.*;

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
        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            LocaleText that = (LocaleText) o;
            return Objects.equals(language, that.language) && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(language);
            result = 31 * result + Objects.hashCode(text);
            return result;
        }
    }
    
    private String id;
    
    @JacksonXmlElementWrapper(localName = "texts")
    @JacksonXmlProperty(localName = "text")
    private List<LocaleText> texts;

    public GameString(String id, List<LocaleText> texts) {
        this.id = id;
        this.texts = new ArrayList<>(texts);
    }
    
    public GameString(String id, LocaleText... texts) {
        this.id = id;
        this.texts = new ArrayList<>(List.of(texts));
    }
    
    // used by Jackson
    @SuppressWarnings("unused")
    private GameString() {
        this.texts = new ArrayList<>();
    }

    public String getId() {
        return id;
    }
    public List<LocaleText> getTexts() {
        return Collections.unmodifiableList(texts);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((texts == null) ? 0 : texts.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GameString other = (GameString) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (texts == null) {
            if (other.texts != null)
                return false;
        } else if (!texts.equals(other.texts))
            return false;
        return true;
    }
    
}
