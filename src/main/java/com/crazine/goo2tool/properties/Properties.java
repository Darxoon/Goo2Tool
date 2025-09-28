package com.crazine.goo2tool.properties;

import com.crazine.goo2tool.addinfile.Goo2mod;
import com.crazine.goo2tool.util.VersionNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Optional;

public class Properties {

    private StringProperty baseWorldOfGoo2Directory = new SimpleStringProperty("");
    private StringProperty customWorldOfGoo2Directory = new SimpleStringProperty("");
    private StringProperty profileDirectory = new SimpleStringProperty("");
    private StringProperty saveFilePath = new SimpleStringProperty("");
    private StringProperty resGooPath = new SimpleStringProperty("");
    private BooleanProperty steam = new SimpleBooleanProperty();
    private boolean isProton;
    private boolean steamWarningShown;
    private ObjectProperty<VersionNumber> fistyVersion = new SimpleObjectProperty<>();
    private StringProperty launchCommand = new SimpleStringProperty("");
    
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
    
    @JsonIgnore
    public StringProperty baseWorldOfGoo2DirectoryProperty() {
        return baseWorldOfGoo2Directory;
    }
    public String getBaseWorldOfGoo2Directory() {
        return baseWorldOfGoo2Directory.get();
    }
    public void setBaseWorldOfGoo2Directory(String baseWorldOfGoo2Directory) {
        this.baseWorldOfGoo2Directory.set(baseWorldOfGoo2Directory);
    }
    
    @JsonIgnore
    public StringProperty customWorldOfGoo2DirectoryProperty() {
        return customWorldOfGoo2Directory;
    }
    public String getCustomWorldOfGoo2Directory() {
        return customWorldOfGoo2Directory.get();
    }
    public void setCustomWorldOfGoo2Directory(String customWorldOfGoo2Directory) {
        this.customWorldOfGoo2Directory.set(customWorldOfGoo2Directory);
    }
    
    @JsonIgnore
    public String getTargetWog2Directory() {
        return steam.get() ? baseWorldOfGoo2Directory.get() : customWorldOfGoo2Directory.get();
    }
    
    @JsonIgnore
    public StringProperty profileDirectoryProperty() {
        return profileDirectory;
    }
    public String getProfileDirectory() {
        return profileDirectory.get();
    }
    public void setProfileDirectory(String profileDirectory) {
        this.profileDirectory.set(profileDirectory);
    }
    
    @JsonIgnore
    public StringProperty resGooPathProperty() {
        return resGooPath;
    }
    public String getResGooPath() {
        return resGooPath.get();
    }
    public void setResGooPath(String resGooPath) {
        this.resGooPath.set(resGooPath);
    }
    
    @JsonIgnore
    public StringProperty saveFilePathProperty() {
        return saveFilePath;
    }
    public String getSaveFilePath() {
        return saveFilePath.get();
    }
    public void setSaveFilePath(String saveFile) {
        this.saveFilePath.set(saveFile);
    }
    
    @JsonIgnore
    public BooleanProperty steamProperty() {
        return steam;
    }
    public boolean isSteam() {
        return steam.get();
    }
    public void setSteam(boolean isSteam) {
        System.out.println("setSteam " + isSteam);
        steam.set(isSteam);
    }
    
    public boolean isProton() {
        return isProton;
    }
    
    public void setProton(boolean isProton) {
        this.isProton = isProton;
    }
    
    public boolean isSteamWarningShown() {
        return steamWarningShown;
    }
    
    public void setSteamWarningShown(boolean steamWarningShown) {
        this.steamWarningShown = steamWarningShown;
    }
    
    public ObjectProperty<VersionNumber> firstyVersionProperty() {
        return fistyVersion;
    }
    
    public VersionNumber getFistyVersion() {
        return fistyVersion.get();
    }
    
    public void setFistyVersion(VersionNumber fistyVersion) {
        this.fistyVersion.set(fistyVersion);
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
