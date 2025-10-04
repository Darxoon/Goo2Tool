package com.crazine.goo2tool.functional.save.mergetable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamReader;

import com.crazine.goo2tool.functional.save.mergetable.MergeTable.MergeValue;
import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.Resrc.SetDefaults;
import com.crazine.goo2tool.gamefiles.resrc.ResrcLoader.ResrcGroupDeserializer;
import com.crazine.goo2tool.gamefiles.resrc.ResrcLoader.ResrcGroupSerializer;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class MergeTableLoader {
    
    public static class MergeValueDeserializer extends StdDeserializer<MergeValue> {
        public MergeValueDeserializer() {
            this(null);
        }
        public MergeValueDeserializer(Class<MergeValue> vc) {
            super(vc);
        }
        @Override
        public MergeValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            FromXmlParser parser = (FromXmlParser) p;
            XMLStreamReader stax = parser.getStaxReader();
            
            do {
                parser.nextToken();
            } while (!parser.getCurrentToken().isStructStart());
            
            Resrc.SetDefaults setDefaults = (SetDefaults) ResrcGroupDeserializer.readResource(parser, stax);
            Resrc value = ResrcGroupDeserializer.readResource(parser, stax);
            
            if (parser.getCurrentToken().isStructStart())
                throw new IOException("Expected end of Entry element");
            
            return new MergeValue(setDefaults, value);
        }
    }
    
    public static class MergeValueSerializer extends StdSerializer<MergeValue> {
        public MergeValueSerializer() {
            this(null);
        }
        public MergeValueSerializer(Class<MergeValue> t) {
            super(t);
        }
        @Override
        public void serialize(MergeValue value, JsonGenerator g, SerializerProvider provider) throws IOException {
            ToXmlGenerator gen = (ToXmlGenerator) g;
            gen.writeStartObject();
            
            ResrcGroupSerializer.writeResource(gen, value.getSetDefaults());
            ResrcGroupSerializer.writeResource(gen, value.getValue());
            
            gen.writeEndObject();
        }
    }
    
    public static ResrcMergeTable loadOrInit(Path resFile) throws IOException {
        try {
            
            byte[] content = Files.readAllBytes(resFile);
            
            if (content.length == 0)
                return new ResrcMergeTable();
            
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(content, ResrcMergeTable.class);
            
        } catch (FileNotFoundException | NoSuchFileException e) {
            return new ResrcMergeTable();
        }
    }
    
    public static void save(MergeTable table, Path outFile) throws IOException {
        
        if (!Files.exists(outFile))
            Files.createFile(outFile);
        
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.writer().withRootName("MergeTable").writeValue(outFile.toFile(), table);
        
    }
    
}
