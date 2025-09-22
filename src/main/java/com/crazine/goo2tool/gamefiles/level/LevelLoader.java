package com.crazine.goo2tool.gamefiles.level;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class LevelLoader {

    public static Level loadLevel(String levelContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(levelContent, Level.class);

    }
    
    public static Level loadLevel(JsonNode levelContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.treeToValue(levelContent, Level.class);

    }
    
    // Takes in a JsonNode because the Level type is incomplete
    public static String saveLevel(JsonNode text) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return jsonMapper.writeValueAsString(text);
        
    }

}
