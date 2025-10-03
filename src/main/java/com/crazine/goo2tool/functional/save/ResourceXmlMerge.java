package com.crazine.goo2tool.functional.save;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeEntry;
import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeFile;
import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeValue;
import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.Resrc.SetDefaults;
import com.crazine.goo2tool.gamefiles.resrc.ResrcGroup;
import com.crazine.goo2tool.gamefiles.resrc.ResrcManifest;

public class ResourceXmlMerge {
    
    public static ResrcManifest transformResources(ResrcManifest original, ResrcManifest patch, MergeFile mergeFile, String modId) {
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
            
            ResrcGroup merged = transformGroup(originalGroup, patchGroup, mergeFile, modId);
            
            if (originalGroupIndex >= 0) {
                groups.set(originalGroupIndex, merged);
            } else {
                groups.add(merged);
            }
        }
        
        return new ResrcManifest(groups);
    }
    
    private static ResrcGroup transformGroup(ResrcGroup original, ResrcGroup patch, MergeFile mergeFile, String modId) {
        if (patch == null || patch.getResources().isEmpty())
            return original;
        
        ResrcGroup out = original.copy();
        
        if (!(patch.getResources().get(0) instanceof Resrc.SetDefaults)) {
            throw new IllegalArgumentException("Resources \""
                + original.getId() + "\" has to start with a SetDefaults element (to avoid compatibility issues)!");
        }
        
        Resrc.SetDefaults setDefaults = null;
        
        for (Resrc resrc : patch.getResources()) {
            if (resrc instanceof SetDefaults) {
                setDefaults = (SetDefaults) resrc;
                continue;
            }
            
            if (setDefaults == null)
                throw new IllegalArgumentException("Resrc Group is missing a <SetDefaults>");
            
            String realId = setDefaults.idprefix() + resrc.id();
            MergeEntry entry = mergeFile.getOrAddEntry(patch.getId(), realId, modId);
            
            detectManualModification(entry, realId, out);
            
            out.removeResource(realId);
            entry.setModValue(new MergeValue(setDefaults, resrc));
        }
        
        out.addResources(patch.getResources(), true);
        return out;
    }
    
    private static void detectManualModification(MergeEntry entry, String realId, ResrcGroup outGroup) {
        if (entry.getModValue() == null) {
            // Merging this entry for the first time ever, so save its current value
            Optional<Resrc> originalResrc = outGroup.getResource(realId);
            Optional<Resrc.SetDefaults> originalSetDefaults = outGroup.getResourceSetDefaults(realId);
            
            if (originalResrc.isPresent() && originalSetDefaults.isPresent()) {
                entry.setOriginalValue(new MergeValue(originalSetDefaults.get(), originalResrc.get()));
            }
            return;
        }
        
        // Detect if value in resources.xml is different from modValue
        // (i.e. it was modified by the user), so the user-modified value can be saved
        MergeValue modValue = entry.getModValue();
        
        String originalPath = modValue.getSetDefaults().path() + modValue.getValue().path();
        
        Optional<Resrc> originalResrc = outGroup.getResource(realId);
        Optional<Resrc.SetDefaults> originalSetDefaults = outGroup.getResourceSetDefaults(realId);
        Optional<String> originalResrcPath = outGroup.getResourcePath(realId);
        
        if (originalResrcPath.isPresent() && !originalResrcPath.get().equals(originalPath)) {
            assert originalResrc.isPresent() && originalSetDefaults.isPresent();
            
            entry.setOriginalValue(new MergeValue(originalSetDefaults.get(), originalResrc.get()));
        }
    }
    
}
