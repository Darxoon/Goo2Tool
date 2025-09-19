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
    
    private String backgroundId;
    // ...
    
    private String musicId;
    private String ambienceId;
    
    @JsonCreator
    public Level(
        @JsonProperty("version") int version,
        @JsonProperty("uuid") String uuid,
        @JsonProperty("title") String title,
        @JsonProperty("backgroundId") String backgroundId,
        @JsonProperty("musicId") String musicId,
        @JsonProperty("ambienceId") String ambienceId
    ) {
        if (version != 2)
            throw new IllegalArgumentException("Unknown level version " + version);
        
        this.version = version;
        this.uuid = uuid;
        this.title = title;
        this.backgroundId = backgroundId;
        this.musicId = musicId;
        this.ambienceId = ambienceId;
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
    
    public String getBackgroundId() {
        return backgroundId;
    }
    public void setBackgroundId(String backgroundId) {
        this.backgroundId = backgroundId;
    }
    
    public String getMusicId() {
        return musicId;
    }
    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }
    
    public String getAmbienceId() {
        return ambienceId;
    }
    public void setAmbienceId(String ambienceId) {
        this.ambienceId = ambienceId;
    }
    
}
