package com.crazine.goo2tool.gamefiles.islands;

import com.crazine.goo2tool.gamefiles.ResArchive;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class IslandFileLoader {

    public static Islands loadIslands(ResArchive res) throws IOException {

        File islandFile = new File(PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory()
                + "/game/res/islands/islands.wog2");
        
        String islandFileContent;
        if (islandFile.exists() && islandFile.isFile()) {
            islandFileContent = Files.readString(islandFile.toPath());
        } else {
            Optional<String> fileContent = res.getFileText("res/islands/islands.wog2");

            if (fileContent.isEmpty())
                throw new IOException("Missing file in game res: res/properties/translation-local.xml");

            islandFileContent = fileContent.get();
        }
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(islandFileContent, Islands.class);

    }

}
