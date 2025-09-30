package com.crazine.goo2tool.addinfile;

import com.crazine.goo2tool.properties.AddinConfigEntry;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AddinFileLoader {
    
    public static List<Goo2mod> loadEnabledAddins() throws IOException {
        // TODO: Cache goo2mods
        Path goo2modDirectory = Path.of(PropertiesLoader.getGoo2ToolPath(), "addins");
        
        Map<String, Goo2mod> allGoo2mods;
        try {
            allGoo2mods = Files.list(goo2modDirectory)
                .parallel()
                .map(filePath -> loadGoo2modUnchecked(filePath.toFile()))
                .collect(Collectors.toMap(mod -> mod.getId(), mod -> mod));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        
        // In reverse order, so that the result can be iterated normally and get expected
        // file override rules
        ArrayList<Goo2mod> result = new ArrayList<>();
        for (AddinConfigEntry addin : PropertiesLoader.getProperties().getAddins().reversed()) {
            if (!addin.isLoaded())
                continue;
            
            Goo2mod goo2mod = allGoo2mods.get(addin.getId());
            if (goo2mod != null) {
                result.add(goo2mod);
            }
        }
        
        return result;
    }

    private static Goo2mod loadGoo2modUnchecked(File goo2modFile) throws UncheckedIOException {
        try {
            return loadGoo2mod(goo2modFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static Goo2mod loadGoo2mod(File goo2modFile) throws IOException {

        try (AddinReader reader = new AddinReader(goo2modFile)) {
            
            Optional<String> addinXmlFile = reader.getFileText("addin.xml");
            
            XmlMapper xmlMapper = new XmlMapper();
            Goo2mod goo2mod;
            try {
                goo2mod = xmlMapper.readValue(addinXmlFile.get(), Goo2mod.class);
            } catch (JacksonException | IllegalArgumentException e) {
                throw new IOException("Failed to parse addin.xml: " + e.getMessage());
            }
            goo2mod.setFile(goo2modFile);
            
            // "*" is reserved because it's being used as 'more than 1 mod' in the file table
            if (goo2mod.getName().equals("*")) {
                throw new IOException("Invalid mod id '*'");
            }
            
            // validate levels property
            switch (goo2mod.getType()) {
                case LEVEL:
                    if (goo2mod.getLevels().size() != 1) {
                        throw new IOException("Goo2mods of type 'level' are required to have exactly 1 <level> entry.");
                    }
                    break;
                case MOD:
                    if (goo2mod.getLevels().size() != 0) {
                        throw new IOException("Goo2mods of type 'mod' are not allowed to have any <level> entries.");
                    }
                    break;
            }
            
            return goo2mod;

        }

    }

}
