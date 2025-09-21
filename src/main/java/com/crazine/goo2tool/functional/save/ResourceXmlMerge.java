package com.crazine.goo2tool.functional.save;

import java.util.ArrayList;
import java.util.List;

import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.ResrcGroup;
import com.crazine.goo2tool.gamefiles.resrc.ResrcManifest;

public class ResourceXmlMerge {
    
    public static ResrcManifest transformResources(ResrcManifest original, ResrcManifest patch) {
        List<ResrcGroup> groups = new ArrayList<>();
        groups.addAll(original.getGroups());
        
        for (ResrcGroup patchGroup : patch.getGroups()) {
            // try find original group belonging to patch
            ResrcGroup originalGroup = null;
            int originalGroupIndex = -1;
            
            for (int i = 0; i < original.getGroups().size(); i++) {
                ResrcGroup group = original.getGroups().get(i);
                
                if (group.getId().equals(patchGroup.getId())) {
                    originalGroup = group;
                    originalGroupIndex = i;
                    break;
                }
            }
            
            if (originalGroup == null) {
                originalGroup = new ResrcGroup(patchGroup.getId(), List.of());
            }
            
            ResrcGroup merged = transformGroup(originalGroup, patchGroup);
            
            if (originalGroupIndex >= 0) {
                groups.set(originalGroupIndex, merged);
            } else {
                groups.add(merged);
            }
        }
        
        return new ResrcManifest(groups);
    }
    
    private static ResrcGroup transformGroup(ResrcGroup original, ResrcGroup patch) {
        if (patch == null || patch.getResources().isEmpty())
            return original;
        
        ResrcGroup out = original.copy();
        
        if (!(patch.getResources().get(0) instanceof Resrc.SetDefaults)) {
            throw new IllegalArgumentException("Resources \""
                + original.getId() + "\" has to start with a SetDefaults element (to avoid compatibility issues)!");
        }
        
        out.addResources(patch.getResources());
        return out;
    }
    
}
