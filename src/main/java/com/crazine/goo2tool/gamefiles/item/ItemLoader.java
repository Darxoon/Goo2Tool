package com.crazine.goo2tool.gamefiles.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;

public class ItemLoader {

    // ItemFile
    public static ItemFile loadItemFile(String levelContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(levelContent, ItemFile.class);

    }
    
    public static String saveItemFile(ItemFile itemFile) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return jsonMapper.writeValueAsString(itemFile);
        
    }
    
    // Item
    public static Item loadItem(JsonNode levelContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.treeToValue(levelContent, Item.class);

    }

}
