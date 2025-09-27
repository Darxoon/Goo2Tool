package com.crazine.goo2tool.gamefiles.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class ItemLoader {

    // ItemFile
    public static ItemFile loadItemFile(String itemContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.readValue(itemContent, ItemFile.class);

    }
    
    public static String saveItemFile(ItemFile itemFile) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return jsonMapper.writeValueAsString(itemFile);
        
    }
    
    // Item
    public static Item loadItemFileAsItem(String itemContent) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        ItemFile itemFile = jsonMapper.readValue(itemContent, ItemFile.class);
        
        if (itemFile.getItems().size() != 1)
            throw new IOException("Expected item file to contain 1 item, got " + itemFile.getItems().size());
        
        JsonNode itemJson = itemFile.getItems().get(0);
        return jsonMapper.treeToValue(itemJson, Item.class);

    }
    
    public static Item loadItem(JsonNode itemJson) throws IOException {
        
        JsonMapper jsonMapper = new JsonMapper();
        return jsonMapper.treeToValue(itemJson, Item.class);

    }
    
    public static JsonNode saveItem(JsonNode itemJson, Item item) throws IOException {
        
        if (!(itemJson instanceof ObjectNode itemObject))
            throw new IOException("Item " + item.getUuid() + ".wog2 file is not a JSON object");
        
        JsonMapper jsonMapper = new JsonMapper();
        
        itemObject.put("uuid", item.getUuid());
        
        JsonNode serializedObjects = jsonMapper.valueToTree(item.getObjects());
        itemObject.set("objects", serializedObjects);

        return itemJson;
        
    }

}
