package com.crazine.goo2tool.addinFile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;

public class Goo2mod {

    @JacksonXmlElementWrapper(localName = "spec-version")
    private double specVersion;
    public double getSpecVersion() {
        return specVersion;
    }
    public void setSpecVersion(double specVersion) {
        this.specVersion = specVersion;
    }

    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private String type;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    private double version;
    public double getVersion() {
        return version;
    }
    public void setVersion(double version) {
        this.version = version;
    }

    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    private String author;
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    @JacksonXmlProperty(isAttribute = false)
    private File file;
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }

}
