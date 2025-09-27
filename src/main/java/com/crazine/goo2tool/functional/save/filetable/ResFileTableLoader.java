package com.crazine.goo2tool.functional.save.filetable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
    
    public static void save(ResFileTable table, File outFile) throws IOException {
        
        if (!Files.exists(outFile.toPath()))
            Files.createFile(outFile.toPath());
        
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.writeValue(outFile, table);
        
    }
    
}
