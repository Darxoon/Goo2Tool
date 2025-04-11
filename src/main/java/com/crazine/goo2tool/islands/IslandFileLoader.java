package com.crazine.goo2tool.islands;

import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.res.ResArchive;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class IslandFileLoader {

    public static Islands loadIslands(Stage stage) throws IOException {

        File islandFile = new File(PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory()
                + "/game/res/islands/islands.wog2");
        
        String islandFileContent;
        if (islandFile.exists() && islandFile.isFile()) {
            islandFileContent = Files.readString(islandFile.toPath());
        } else {
            // TODO: add AppImage support
            try (ResArchive res = ResArchive.loadVanilla(stage)) {
                byte[] fileBytes = res.getFileContent("res/islands/islands.wog2").get();
                islandFileContent = new String(fileBytes, StandardCharsets.UTF_8);
            }
        }
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(islandFileContent, Islands.class);

    }
    
    public static Islands loadIslands() throws IOException {
        return loadIslands(null);
    }

}
