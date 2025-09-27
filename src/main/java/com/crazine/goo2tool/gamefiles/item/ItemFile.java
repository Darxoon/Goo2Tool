package com.crazine.goo2tool.gamefiles.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class ItemFile {
    
    private List<JsonNode> items;
    
    public ItemFile() {
        this.items = new ArrayList<>();
    }

    public ItemFile(List<JsonNode> items) {
        this.items = items;
    }
    
    public static ItemFile fromItem(JsonNode itemJson, Item item) throws IOException {
        return new ItemFile(List.of(ItemLoader.saveItem(itemJson, item)));
    }
    
    public List<JsonNode> getItems() {
        return items;
    }
    public void setItems(List<JsonNode> items) {
        this.items = items;
    }
    
}
