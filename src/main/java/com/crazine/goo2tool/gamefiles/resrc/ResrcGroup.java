package com.crazine.goo2tool.gamefiles.resrc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = ResrcLoader.ResrcGroupDeserializer.class)
@JsonSerialize(using = ResrcLoader.ResrcGroupSerializer.class)
public class ResrcGroup {
    
    private String id;
    private List<Resrc> resources = new ArrayList<>();
    
    public ResrcGroup() {}
    
    public ResrcGroup(String id, List<Resrc> resources) {
        this.id = id;
        this.resources.addAll(resources);
    }
    
    public ResrcGroup copy() {
        return new ResrcGroup(id, new ArrayList<>(resources));
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public List<Resrc> getResources() {
        return resources;
    }
    public void setResources(List<Resrc> content) {
        this.resources = content;
    }
    
}
