package com.crazine.goo2tool.addinFile;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AddinFileLoader {

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
