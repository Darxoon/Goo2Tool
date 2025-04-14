package com.crazine.goo2tool.addinFile;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AddinFileLoader {

    public static Goo2mod loadGoo2mod(File goo2modFile) throws IOException {

        try (AddinReader reader = new AddinReader(goo2modFile)) {
            
            Optional<String> addinXmlFile = reader.getFileText("addin.xml");
            
            XmlMapper xmlMapper = new XmlMapper();
            Goo2mod goo2mod = xmlMapper.readValue(addinXmlFile.get(), Goo2mod.class);
            goo2mod.setFile(goo2modFile);
        
            if (goo2mod.getName().equals("*"))
                throw new IOException("Invalid mod id '*'");
            
            return goo2mod;

        }

    }

}
