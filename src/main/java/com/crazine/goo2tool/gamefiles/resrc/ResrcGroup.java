package com.crazine.goo2tool.gamefiles.resrc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crazine.goo2tool.gamefiles.resrc.Resrc.SetDefaults;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = ResrcLoader.ResrcGroupDeserializer.class)
@JsonSerialize(using = ResrcLoader.ResrcGroupSerializer.class)
public class ResrcGroup {
    
    private static Logger logger = LoggerFactory.getLogger(ResrcGroup.class);
    
    private String id;
    private List<Resrc> resources = new ArrayList<>();
    
    // TODO: potentially put this into one Map?
    private Map<String, Resrc> resourceMap = new HashMap<>();
    private Map<String, Resrc.SetDefaults> resourceSetDefaults = new HashMap<>();
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
            if (resrc instanceof SetDefaults) {
                setDefaults = (SetDefaults) resrc;
                continue;
            }
            
            if (setDefaults == null) {
                logger.warn("Adding empty SetDefaults to ResrcGroup {}", id);
                setDefaults = new SetDefaults("", "");
            }
            
            String realId = setDefaults.idprefix() + resrc.id();
            this.resourceMap.put(realId, resrc);
            this.resourceSetDefaults.put(realId, setDefaults);
            this.resourcePaths.put(realId, setDefaults.path() + resrc.path());
        }
    }
    
    public void removeResource(String id) {
        List<Resrc> toRemove = new ArrayList<>();
        
        Resrc.SetDefaults setDefaults = null;
        int setDefaultResources = 0;
        
        for (Resrc resrc : resources) {
            if (resrc instanceof SetDefaults) {
                // Remove old setDefaults if it didn't affect anything
                if (setDefaults != null && setDefaultResources == 0) {
                    logger.debug("Unnecessary SetDefault with path={} and idprefix={} removed",
                            setDefaults.path(), setDefaults.idprefix());
                    toRemove.add(setDefaults);
                }
                
                setDefaults = (SetDefaults) resrc;
                setDefaultResources = 0;
                continue;
            }
            
            if (setDefaults == null) {
                logger.warn("Adding empty SetDefaults to ResrcGroup {} (while removing {})", this.id, id);
                setDefaults = new SetDefaults("", "");
            }
            
            String realId = setDefaults.idprefix() + resrc.id();
            
            if (realId.equals(id)) {
                toRemove.add(resrc);
            } else {
                setDefaultResources++;
            }
        }
        
        if (setDefaults != null && setDefaultResources == 0) {
            logger.debug("Unnecessary SetDefault with path={} and idprefix={} removed",
                    setDefaults.path(), setDefaults.idprefix());
            toRemove.add(setDefaults);
        }
        
        resources.removeIf(resrc -> toRemove.stream().anyMatch(resrc2 -> resrc == resrc2));
    }
    
    public Optional<Resrc> getResource(String id) {
        return Optional.ofNullable(resourceMap.get(id));
    }
    public Optional<Resrc.SetDefaults> getResourceSetDefaults(String id) {
        return Optional.ofNullable(resourceSetDefaults.get(id));
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
