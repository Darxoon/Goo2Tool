package com.crazine.goo2tool.gamefiles.item;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    private String name;
    private String uuid;
    // ...
    
    private String animationName;
    // ...
    
    private List<ItemObject> objects = new ArrayList<>();
    // ...
    
    private List<ItemUserVariable> userVariables = new ArrayList<>();
    // ...
    
    private JsonNode sound;
    // ...

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getAnimationName() {
        return animationName;
    }
    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public List<ItemObject> getObjects() {
        return objects;
    }
    public void setObjects(List<ItemObject> objects) {
        this.objects = objects;
    }
    
    public List<ItemUserVariable> getUserVariables() {
        return userVariables;
    }
    public void setUserVariables(List<ItemUserVariable> userVariables) {
        this.userVariables = userVariables;
    }
    
    public JsonNode getSound() {
        return sound;
    }
    public void setSound(JsonNode sound) {
        this.sound = sound;
    }
    
}
