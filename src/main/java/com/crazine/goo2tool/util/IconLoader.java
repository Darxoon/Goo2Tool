package com.crazine.goo2tool.util;

import java.io.IOException;
import java.io.InputStream;

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
        InputStream iconStream = IconLoader.class.getClassLoader().getResourceAsStream(name);
        return new Image(iconStream);
    }
    
    public static Image getConduit() {
        return conduit;
    }
    
    public static Image getTerrain() {
        return terrain;
    }
    
}
