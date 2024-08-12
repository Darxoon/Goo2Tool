package com.crazine.goo2tool.islands;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class IslandFileLoader {

    public static Islands loadIslands(File islandsFile) throws IOException {

        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(Files.readString(islandsFile.toPath()), Islands.class);

    }

}
