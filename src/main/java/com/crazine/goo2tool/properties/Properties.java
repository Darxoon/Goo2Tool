package com.crazine.goo2tool.properties;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;

public class Properties {

    private String baseWorldOfGoo2Directory = "";
    public String getBaseWorldOfGoo2Directory() {
        return baseWorldOfGoo2Directory;
    }
    public void setBaseWorldOfGoo2Directory(String baseWoG2Dir) {
        this.baseWorldOfGoo2Directory = baseWoG2Dir;
    }

    private String customWorldOfGoo2Directory = "";
    public String getCustomWorldOfGoo2Directory() {
        return customWorldOfGoo2Directory;
    }
    public void setCustomWorldOfGoo2Directory(String customWoG2Dir) {
        this.customWorldOfGoo2Directory = customWoG2Dir;
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
    private ArrayList<AddinConfigEntry> addins = new ArrayList<>();
    @JacksonXmlProperty() public ArrayList<AddinConfigEntry> getAddins() {
        return addins;
    }
    @JacksonXmlProperty() public void setAddins(ArrayList<AddinConfigEntry> addins) {
        this.addins = addins;
    }

}
