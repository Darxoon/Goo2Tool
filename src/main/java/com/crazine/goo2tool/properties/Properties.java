package com.crazine.goo2tool.properties;

import com.crazine.goo2tool.addinFile.Goo2mod;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Optional;

public class Properties {

    private String baseWorldOfGoo2Directory = "";
    private String customWorldOfGoo2Directory = "";
    private String profileDirectory = "";
    private String resGooPath = "";
    private boolean isSteam;
    private StringProperty launchCommand = new SimpleStringProperty();
    
    @JacksonXmlElementWrapper(localName = "Addins")
    @JacksonXmlProperty(localName = "Addin")
    private ArrayList<AddinConfigEntry> addins = new ArrayList<>();
    
    @JacksonXmlProperty() public ArrayList<AddinConfigEntry> getAddins() {
        return addins;
    }
    @JacksonXmlProperty() public void setAddins(ArrayList<AddinConfigEntry> addins) {
        this.addins = addins;
    }
    
    public boolean hasAddin(String name) {
        for (AddinConfigEntry addin : addins) {
            if (addin.getId().equals(name))
                return true;
        }
        
        return false;
    }
    
    public Optional<AddinConfigEntry> getAddin(Goo2mod goo2mod) {
        return getAddin(goo2mod.getId());
    }
    
    public Optional<AddinConfigEntry> getAddin(String id) {
        for (AddinConfigEntry addin : addins) {
            if (addin.getId().equals(id))
                return Optional.of(addin);
        }
        
        return Optional.empty();
    }
    
    public String getBaseWorldOfGoo2Directory() {
        return baseWorldOfGoo2Directory;
    }
    
    public void setBaseWorldOfGoo2Directory(String baseWorldOfGoo2Directory) {
        this.baseWorldOfGoo2Directory = baseWorldOfGoo2Directory;
    }
    
    public String getCustomWorldOfGoo2Directory() {
        return customWorldOfGoo2Directory;
    }
    
    public void setCustomWorldOfGoo2Directory(String customWorldOfGoo2Directory) {
        this.customWorldOfGoo2Directory = customWorldOfGoo2Directory;
    }
    
    public String getProfileDirectory() {
        return profileDirectory;
    }
    
    public void setProfileDirectory(String profileDirectory) {
        this.profileDirectory = profileDirectory;
    }
    
    public String getResGooPath() {
        return resGooPath;
    }
    
    public void setResGooPath(String resGooPath) {
        this.resGooPath = resGooPath;
    }
    
    public boolean isSteam() {
        return isSteam;
    }
    
    public void setSteam(boolean isSteam) {
        this.isSteam = isSteam;
    }
    
    public String getLaunchCommand() {
        return launchCommand.get();
    }
    
    public void setLaunchCommand(String launchCommand) {
        this.launchCommand.set(launchCommand);
    }
    
    public StringProperty launchCommandProperty() {
        return launchCommand;
    }
    
}
