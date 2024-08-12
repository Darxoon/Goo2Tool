package com.crazine.goo2tool.properties;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;

public class Properties {

    private String baseWorldOfGoo2Directory = "";
    public String getBaseWorldOfGoo2Directory() {
        return baseWorldOfGoo2Directory;
    }
    public void setBaseWorldOfGoo2Directory(String baseWorldOfGoo2Directory) {
        this.baseWorldOfGoo2Directory = baseWorldOfGoo2Directory;
    }

    private String customWorldOfGoo2Directory = "";
    public String getCustomWorldOfGoo2Directory() {
        return customWorldOfGoo2Directory;
    }
    public void setCustomWorldOfGoo2Directory(String customWorldOfGoo2Directory) {
        this.customWorldOfGoo2Directory = customWorldOfGoo2Directory;
    }

    private String profileDirectory = "";
    public String getProfileDirectory() {
        return profileDirectory;
    }
    public void setProfileDirectory(String profileDirectory) {
        this.profileDirectory = profileDirectory;
    }

    @JacksonXmlElementWrapper(localName = "Addins")
    @JacksonXmlProperty(localName = "Addin")
    private Addin[] addins = new Addin[0];
    public Addin[] getAddins() {
        return addins;
    }
    public void setAddins(Addin[] addins) {
        this.addins = addins;
    }

}
