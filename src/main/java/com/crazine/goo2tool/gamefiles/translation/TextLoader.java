package com.crazine.goo2tool.gamefiles.translation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class TextLoader {
    
    public static TextDB loadText(Path textFile) throws IOException {
        byte[] content = Files.readAllBytes(textFile);
        
        XmlMapper mapper = new XmlMapper();
        return mapper.readValue(content, TextDB.class);
    }
    
    public static TextDB loadText(byte[] content) throws IOException {
        XmlMapper mapper = new XmlMapper();
        return mapper.readValue(content, TextDB.class);
    }
    
    public static TextDB loadText(String content) throws IOException {
        XmlMapper mapper = new XmlMapper();
        return mapper.readValue(content, TextDB.class);
    }
    
    public static void saveText(TextDB text, File outFile) throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        
        mapper.writeValue(outFile, text);
    }
    
}
