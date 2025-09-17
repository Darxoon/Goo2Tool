package com.crazine.goo2tool.gamefiles.resrc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.crazine.goo2tool.gamefiles.resrc.Resrc.SetDefaults;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = ResrcLoader.ResrcGroupDeserializer.class)
@JsonSerialize(using = ResrcLoader.ResrcGroupSerializer.class)
public class ResrcGroup {
    
    private String id;
    private List<Resrc> resources = new ArrayList<>();
    private Map<String, Resrc> resourceMap = new HashMap<>();
    private Map<String, String> resourcePaths = new HashMap<>();
    
    public ResrcGroup() {}
    
    public ResrcGroup(String id, List<Resrc> resources) {
        this.id = id;
        addResources(resources);
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
    
    public void addResources(List<Resrc> resources) {
        this.resources.addAll(resources);
        
        Resrc.SetDefaults setDefaults = null;
        
        for (Resrc resrc : resources) {
            if (resrc instanceof SetDefaults setDefaults2) {
                setDefaults = setDefaults2;
                continue;
            }
            
            if (setDefaults == null)
                throw new IllegalArgumentException("Resrc Group is missing a <SetDefaults>");
            
            String realId = setDefaults.idprefix() + resrc.id();
            this.resourceMap.put(realId, resrc);
            this.resourcePaths.put(realId, setDefaults.path() + resrc.path());
        }
    }
    
    public Optional<Resrc> getResource(String id) {
        return Optional.ofNullable(resourceMap.get(id));
    }
    public Optional<String> getResourcePath(String id) {
        return Optional.ofNullable(resourcePaths.get(id));
    }
    
    public List<Resrc> getResources() {
        return Collections.unmodifiableList(resources);
    }
    public void setResources(List<Resrc> content) {
        this.resources = content;
    }
    
}
