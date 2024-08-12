package com.crazine.goo2tool.addinFile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.ext.DOMDeserializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AddinFileLoader {

    public static Goo2mod loadGoo2mod(File goo2modFile) throws IOException {

        ZipFile zipFile = new ZipFile(goo2modFile);

        for (Iterator<? extends ZipEntry> it = zipFile.stream().iterator(); it.hasNext(); ) {
            ZipEntry zipEntry = it.next();

            if (zipEntry.getName().endsWith("addin.xml")) {

                XmlMapper xmlMapper = new XmlMapper();
                Goo2mod goo2mod = xmlMapper.readValue(new String(zipFile.getInputStream(zipEntry).readAllBytes()), Goo2mod.class);
                goo2mod.setFile(goo2modFile);
                return goo2mod;

            }


        }

        return null;

    }

}
