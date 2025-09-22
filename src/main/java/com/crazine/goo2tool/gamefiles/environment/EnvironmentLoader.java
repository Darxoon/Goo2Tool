package com.crazine.goo2tool.gamefiles.environment;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class EnvironmentLoader {
    
    public static Environment loadBackground(String environmentText) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(environmentText, Environment.class);

    }
    
    public static Environment loadBackground(JsonNode environmentValue) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.treeToValue(environmentValue, Environment.class);

    }
    
    // Takes in a JsonNode because the Environment type is incomplete
    public static String saveBackground(JsonNode text) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return jsonMapper.writeValueAsString(text);
        
    }
    
}
