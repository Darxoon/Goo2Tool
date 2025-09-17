package com.crazine.goo2tool.gamefiles.resrc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("ResourceManifest")
public class ResrcManifest {
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Resources")
    private List<ResrcGroup> groups = new ArrayList<>();
    
    public ResrcManifest() {}
    
    public ResrcManifest(List<ResrcGroup> groups) {
        this.groups.addAll(groups);
    }
    
    public ResrcManifest(ResrcGroup... groups) {
        this(List.of(groups));
    }
    
    public Optional<ResrcGroup> getGroup(String id) {
        for (ResrcGroup group : groups) {
            if (id.equals(group.getId()))
                return Optional.of(group);
        }
        
        return Optional.empty();
    }
    
    public List<ResrcGroup> getGroups() {
        return groups;
    }
    public void setGroups(List<ResrcGroup> groups) {
        this.groups = groups;
    }
    
}
