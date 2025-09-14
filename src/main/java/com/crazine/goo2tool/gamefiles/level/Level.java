package com.crazine.goo2tool.gamefiles.level;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Level {

    private int version;
    private String uuid = "";
    private String title = "";
    // ...
    
    @JsonCreator
    public Level(
        @JsonProperty("version") int version,
        @JsonProperty("uuid") String uuid,
        @JsonProperty("title") String title
    ) {
        if (version != 2)
            throw new IllegalArgumentException("Unknown level version " + version);
        
        this.version = version;
        this.uuid = uuid;
        this.title = title;
    }
    
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
}
