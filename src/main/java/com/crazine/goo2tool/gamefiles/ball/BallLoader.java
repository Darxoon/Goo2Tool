package com.crazine.goo2tool.gamefiles.ball;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class BallLoader {
    
    public static Ball loadBall(String ballText) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(ballText, Ball.class);

    }
    
    public static Ball loadBall(JsonNode ballValue) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.treeToValue(ballValue, Ball.class);

    }
    
    // Takes in a JsonNode because the Ball type is incomplete
    public static String saveBall(JsonNode ballJson) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return jsonMapper.writeValueAsString(ballJson);
        
    }
    
}
