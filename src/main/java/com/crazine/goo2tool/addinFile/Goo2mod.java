package com.crazine.goo2tool.addinFile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;

public class Goo2mod {

    @JacksonXmlElementWrapper(localName = "spec-version")
    private double specVersion;
    @JacksonXmlProperty(localName = "spec-version")
    public double getSpecVersion() {
        return specVersion;
    }
    @JacksonXmlProperty(localName = "spec-version")
    public void setSpecVersion(double specVersion) {
        this.specVersion = specVersion;
    }

    @JacksonXmlElementWrapper(localName = "id")
    private String id;
    @JacksonXmlProperty(localName = "id")
    public String getId() {
        return id;
    }
    @JacksonXmlProperty(localName = "id")
    public void setId(String id) {
        this.id = id;
    }

    @JacksonXmlElementWrapper(localName = "name")
    private String name;
    @JacksonXmlProperty(localName = "name")
    public String getName() {
        return name;
    }
    @JacksonXmlProperty(localName = "name")
    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlElementWrapper(localName = "type")
    private String type;
    @JacksonXmlProperty(localName = "type")
    public String getType() {
        return type;
    }
    @JacksonXmlProperty(localName = "type")
    public void setType(String type) {
        this.type = type;
    }


    @JacksonXmlElementWrapper(localName = "version")
    private double version;
    @JacksonXmlProperty(localName = "version")
    public double getVersion() {
        return version;
    }
    @JacksonXmlProperty(localName = "version")
    public void setVersion(double version) {
        this.version = version;
    }

    @JacksonXmlElementWrapper(localName = "description")
    private String description;
    @JacksonXmlProperty(localName = "description")
    public String getDescription() {
        return description;
    }
    @JacksonXmlProperty(localName = "description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JacksonXmlElementWrapper(localName = "author")
    private String author;
    @JacksonXmlProperty(localName = "author")
    public String getAuthor() {
        return author;
    }
    @JacksonXmlProperty(localName = "author")
    public void setAuthor(String author) {
        this.author = author;
    }

    @JacksonXmlProperty()
    private File file;
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }

}
