package com.crazine.goo2tool.functional.save.filetable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class ResFileTableLoader {
    
    public static ResFileTable loadOrInit(Path resFile) throws IOException {
        try {
            
            byte[] content = Files.readAllBytes(resFile);
            
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(content, ResFileTable.class);
            
        } catch (FileNotFoundException | NoSuchFileException e) {
            return new ResFileTable();
        }
    }
    
    public static void save(ResFileTable table, Path outFile) throws IOException {
        
        if (!Files.exists(outFile))
            Files.createFile(outFile);
        
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.writeValue(outFile.toFile(), table);
        
    }
    
}
