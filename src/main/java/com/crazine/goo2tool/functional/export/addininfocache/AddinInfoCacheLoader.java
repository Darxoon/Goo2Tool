package com.crazine.goo2tool.functional.export.addininfocache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.crazine.goo2tool.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class AddinInfoCacheLoader {
    
    private static AddinInfoCache cache;
    
    public static AddinInfoCache getOrInit() throws IOException {
        if (cache != null)
            return cache;
        
        try {
            
            byte[] content = Files.readAllBytes(getFilePath());
            
            if (content.length == 0)
                return new AddinInfoCache();
            
            JsonMapper mapper = new JsonMapper();
            cache = mapper.readValue(content, AddinInfoCache.class);
            return cache;
            
        } catch (NoSuchFileException e) {
            return new AddinInfoCache();
        }
    }
    
    public static void save() throws IOException {
        
        // if (!Files.exists(outFile.toPath()))
        //     Files.createFile(outFile.toPath());
        
        AddinInfoCache value = cache != null ? cache : new AddinInfoCache();
        
        JsonMapper mapper = new JsonMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writeValue(getFilePath().toFile(), value);
        
    }
    
    private static Path getFilePath() {
        return Path.of(PropertiesLoader.getGoo2ToolPath(), "addinInfoCache.wog2");
    }
    
}
