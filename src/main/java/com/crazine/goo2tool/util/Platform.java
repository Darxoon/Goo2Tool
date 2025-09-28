package com.crazine.goo2tool.util;

public enum Platform {
    WINDOWS,
    MAC,
    LINUX;
    
    public static Platform getCurrent() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return Platform.WINDOWS;
        } else if (os.contains("mac")) {
            return Platform.MAC;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return Platform.LINUX;
        } else {
            throw new RuntimeException("Unsupported OS: " + os);
        }
    }
}
