package com.crazine.goo2tool.gamefiles.ball;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    public static String saveBall(JsonNode ballJson, Ball ball) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        // TODO: improve Loader error handling
        if (!(ballJson instanceof ObjectNode ballObject))
            throw new IOException("ball.wog2 file is not a JSON object");
        
        ballObject.set("strandImageId", jsonMapper.valueToTree(ball.getStrandImageId()));
        ballObject.set("strandInactiveImageId", jsonMapper.valueToTree(ball.getStrandInactiveImageId()));
        ballObject.set("strandInactiveOverlayImageId", jsonMapper.valueToTree(ball.getStrandInactiveOverlayImageId()));
        ballObject.set("strandBurntImageId", jsonMapper.valueToTree(ball.getStrandBurntImageId()));
        ballObject.set("strandBackgroundImageId", jsonMapper.valueToTree(ball.getStrandBackgroundImageId()));
        ballObject.set("detachStrandImageId", jsonMapper.valueToTree(ball.getDetachStrandImageId()));
        ballObject.set("dragMarkerImageId", jsonMapper.valueToTree(ball.getDragMarkerImageId()));
        ballObject.set("detachMarkerImageId", jsonMapper.valueToTree(ball.getDetachMarkerImageId()));
        
        return jsonMapper.writeValueAsString(ballJson);
        
    }
    
}
