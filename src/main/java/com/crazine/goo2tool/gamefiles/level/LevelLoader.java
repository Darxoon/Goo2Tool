package com.crazine.goo2tool.gamefiles.level;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class LevelLoader {

    public static Level loadLevel(String levelContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(levelContent, Level.class);

    }

}
