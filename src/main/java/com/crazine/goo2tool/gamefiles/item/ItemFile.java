package com.crazine.goo2tool.gamefiles.item;

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
    
    public List<JsonNode> getItems() {
        return items;
    }
    public void setItems(List<JsonNode> items) {
        this.items = items;
    }
    
}
