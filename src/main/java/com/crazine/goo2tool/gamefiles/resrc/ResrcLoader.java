package com.crazine.goo2tool.gamefiles.resrc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class ResrcLoader {
    
    public static class ResrcGroupDeserializer extends StdDeserializer<ResrcGroup> {

        public ResrcGroupDeserializer() {
            this(null);
        }
        
        public ResrcGroupDeserializer(Class<ResrcGroup> vc) {
            super(vc);
        }

        @Override
        public ResrcGroup deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            FromXmlParser parser = (FromXmlParser) p;
            XMLStreamReader stax = parser.getStaxReader();
            
            String id = stax.getAttributeValue("", "id");
            List<Resrc> resources = new ArrayList<>();
            
            do {
                parser.nextToken();
            } while (!parser.getCurrentToken().isStructStart());
            
            while (parser.getCurrentToken().isStructStart()) {
                String name = stax.getLocalName();
                resources.add(parseResource(stax));
                
                while (!parser.getCurrentToken().isStructEnd()) {
                    if (!stax.getLocalName().equals(name))
                        throw new RuntimeException("Unexpected tag <" + stax.getLocalName() + ">, expected </" + name + ">");
                    
                    parser.nextToken();
                }
                
                parser.nextToken();
                
                if (parser.getCurrentToken() == JsonToken.FIELD_NAME)
                    parser.nextToken();
            }
            
            return new ResrcGroup(id, resources);
        }
        
        private Resrc parseResource(XMLStreamReader stax) {
            switch (stax.getLocalName()) {
                case "SetDefaults": {
                    String path = stax.getAttributeValue("", "path");
                    String idprefix = stax.getAttributeValue("", "idprefix");
                    return new Resrc.SetDefaults(path, idprefix);
                }
                case "Image": {
                    String id = stax.getAttributeValue("", "id");
                    String path = stax.getAttributeValue("", "path");
                    return new Resrc.Image(id, path);
                }
                case "Sound": {
                    String id = stax.getAttributeValue("", "id");
                    String path = stax.getAttributeValue("", "path");
                    boolean streaming = Boolean.valueOf(stax.getAttributeValue("", "streaming"));
                    String bus = stax.getAttributeValue("", "bus");
                    return new Resrc.Sound(id, path, streaming, bus);
                }
                case "FlashAnim": {
                    String id = stax.getAttributeValue("", "id");
                    String path = stax.getAttributeValue("", "path");
                    return new Resrc.FlashAnim(id, path);
                }
                case "Atlas": {
                    String id = stax.getAttributeValue("", "id");
                    String path = stax.getAttributeValue("", "path");
                    return new Resrc.Atlas(id, path);
                }
                case "font":
                    throw new RuntimeException("Resources of type font are not supported yet!");
                default:
                    throw new RuntimeException("Unknown resource type " + stax.getLocalName());
            }
        }
        
    }
    
    public static class ResrcGroupSerializer extends StdSerializer<ResrcGroup> {

        public ResrcGroupSerializer() {
            this(null);
        }
        
        public ResrcGroupSerializer(Class<ResrcGroup> t) {
            super(t);
        }

        @Override
        public void serialize(ResrcGroup value, JsonGenerator g, SerializerProvider provider) throws IOException {
            ToXmlGenerator gen = (ToXmlGenerator) g;
            gen.writeStartObject();
            gen.setNextIsAttribute(true);
            gen.writeStringField("id", value.getId());
            
            for (Resrc resrc : value.getResources()) {
                gen.writeFieldName(resrc.getClass().getSimpleName());
                gen.writeStartObject(resrc);
                
                if (resrc instanceof Record) {
                    for (RecordComponent component : resrc.getClass().getRecordComponents()) {
                        try {
                            Object fieldValue = component.getAccessor().invoke(resrc);
                            gen.setNextIsAttribute(true);
                            gen.writeObjectField(component.getName(), fieldValue);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
                gen.writeEndObject();
            }
            
            gen.writeEndObject();
        }
        
    }
    
    public static ResrcManifest loadManifest(Path filePath) throws IOException {
        byte[] content = Files.readAllBytes(filePath);
        
        XmlMapper mapper = new XmlMapper();
        return mapper.readValue(content, ResrcManifest.class);
    }
    
    public static ResrcManifest loadManifest(byte[] content) throws IOException {
        XmlMapper mapper = new XmlMapper();
        return mapper.readValue(content, ResrcManifest.class);
    }
    
    public static void saveManifest(ResrcManifest manifest, File outFile) throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        
        mapper.writeValue(outFile, manifest);
    }
    
    public static byte[] saveManifest(ResrcManifest manifest) throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        
        return mapper.writeValueAsBytes(manifest);
    }
    
}
