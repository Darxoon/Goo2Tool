package com.crazine.goo2tool.functional;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonMerge {
    
    public static JsonNode transformJson(JsonNode original, ObjectNode patch) {
        String propertyType = "merge";
        
        if (patch.has("__propertyType__")) {
            JsonNode propertyTypeObj = patch.get("__propertyType__");
            
            if (!propertyTypeObj.isTextual())
                throw new IllegalArgumentException("Property '__propertyType__' has to be a string, not " + propertyTypeObj.asText());
            
            propertyType = propertyTypeObj.textValue();
        }
        
        switch (propertyType) {
            case "merge": {
                if (original == null || original.isNull())
                    original = patch.objectNode();
                
                if (!original.isObject())
                    throw new IllegalArgumentException(
                            "Trying to merge an object with a non-object");
                
                return transformObject((ObjectNode) original, patch);
            }
            case "array":
                if (original == null || original.isNull())
                    original = patch.arrayNode();
                
                if (!original.isArray())
                    throw new IllegalArgumentException(
                            "Cannot use property type \"array\" on a non-array");
                
                return transformArray((ArrayNode) original, patch);
            default:
                throw new IllegalArgumentException("Unknown __propertyType__ value '"
                        + propertyType + "; possible values are \"array\" and \"merge\" (default)");
        }
    }
    
    private static JsonNode transformObject(ObjectNode original, ObjectNode patch) {
        // Create a new empty json object and shallow copy
        ObjectNode out = patch.objectNode();
        
        if (original != null && !original.isNull()) {
            for (Map.Entry<String, JsonNode> field : original.properties()) {
                out.set(field.getKey(), field.getValue());
            }
        }
        
        // Recursively apply modifications
        for (Map.Entry<String, JsonNode> field : patch.properties()) {
            String fieldName = field.getKey();
            JsonNode value = field.getValue();
            
            if (fieldName.startsWith("__") && fieldName.endsWith("__"))
                continue;
            
            if (!value.isObject()) {
                out.set(fieldName, value);
            } else {
                JsonNode node = transformJson(original.get(fieldName), (ObjectNode) value);
                out.set(fieldName, node);
            }
        }
        
        return out;
    }
    
    private static JsonNode transformArray(ArrayNode original, ObjectNode patch) {
        ArrayNode out = patch.arrayNode();
        out.addAll(original);
        
        if (patch.has("merge")) {
            JsonNode toMerge = patch.get("merge");
            
            if (!toMerge.isObject())
                throw new IllegalArgumentException(
                        "Property \"merge\" of property with type \"array\" has to be an object");
            
            
            for (Map.Entry<String, JsonNode> field : toMerge.properties()) {
                int index = Integer.valueOf(field.getKey());
                JsonNode valuePatch = field.getValue();
                
                if (!valuePatch.isObject())
                    throw new IllegalArgumentException(
                            "Items in \"merge\" property have to be objects");
                
                JsonNode transformed = transformJson(original.get(index), (ObjectNode) valuePatch);
                out.set(index, transformed);
            }
        }
        
        // TODO: insert
        
        if (patch.has("append")) {
            JsonNode toAppend = patch.get("append");
            
            if (!toAppend.isArray())
                throw new IllegalArgumentException(
                        "Property \"append\" of property with type \"array\" has to be an array");
            
            out.addAll((ArrayNode) toAppend);
        }
        
        return out;
    }
    
}
