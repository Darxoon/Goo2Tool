package com.crazine.goo2tool.properties;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AddinConfigEntry {

    @JacksonXmlProperty(isAttribute = true)
    private String id;
    @JacksonXmlProperty(isAttribute = true)
    private BooleanProperty loaded = new SimpleBooleanProperty(false);
    
    public String getId() {
        return id;
    }
    public void setId(String name) {
        this.id = name;
    }

    public boolean isLoaded() {
        return loaded.get();
    }
    public void setLoaded(boolean loaded) {
        this.loaded.set(loaded);
    }
    public BooleanProperty loadedProperty() {
        return loaded;
    }

}