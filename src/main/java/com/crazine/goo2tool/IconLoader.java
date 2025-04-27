package com.crazine.goo2tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.crazine.goo2tool.gui.Main_Application;

import javafx.scene.image.Image;

public class IconLoader {
    
    private static Image conduit = null;
    private static Image terrain = null;
    
    private IconLoader() {}
    
    public static void init() throws IOException {
        conduit = loadImage("conduit.png");
        terrain = loadImage("terrain.png");
    }
    
    private static Image loadImage(String name) throws IOException {
        String projectLocation = Main_Application.getProjectLocation();
        InputStream iconStream = new FileInputStream(projectLocation + "/" + name);
        return new Image(iconStream);
    }
    
    public static Image getConduit() {
        return conduit;
    }
    
    public static Image getTerrain() {
        return terrain;
    }
    
}
