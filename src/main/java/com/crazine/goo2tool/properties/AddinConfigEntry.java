package com.crazine.goo2tool.properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AddinConfigEntry {

    private String name;
    private BooleanProperty loaded = new SimpleBooleanProperty(false);
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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