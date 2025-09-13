package com.crazine.goo2tool.functional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.crazine.goo2tool.Platform;

public class LocateGooDir {
    
    public static record GooDir(Path path, Optional<Path> steamDir) {}
    
    private LocateGooDir() {}
    
    public static Optional<GooDir> locateWog2() {
        switch (Platform.getCurrent()) {
            case WINDOWS:
                // TODO: Detect Steam
                // Detect DRM-free version in Program Files
                Path exeFile = Paths.get(System.getenv("PROGRAMFILES"), "World of Goo 2/World of Goo 2.exe");
                
                if (Files.isRegularFile(exeFile))
                    return Optional.of(new GooDir(exeFile.getParent(), Optional.empty()));
                else
                    return Optional.empty();
            case MAC:
                // TODO: Detect Steam
                // Detect World of Goo 2.app
                Path appDir = Paths.get("/Applications/World of Goo 2.app");
                
                if (Files.isDirectory(appDir))
                    return Optional.of(new GooDir(appDir.getParent(), Optional.empty()));
                else
                    return Optional.empty();
            case LINUX: {
                // Detect Steam
                Path steamDir = locateSteamLinux();
                
                if (steamDir != null) {
                    Path wog2Dir = steamDir.resolve("steamapps/common/World of Goo 2");
                    
                    if (Files.exists(wog2Dir))
                        return Optional.of(new GooDir(wog2Dir, Optional.of(steamDir)));
                }
                
                // Linux version is an AppImage, so it could be installed anywhere
                return Optional.empty();
            }
            default:
                return Optional.empty();
        }
    }
    
    private static String[] steamLinuxCandidates = new String[] {
        ".var/app/com.valvesoftware.Steam/.local/share/Steam",
        ".var/app/com.valvesoftware.Steam/.steam/steam",
        ".var/app/com.valvesoftware.Steam/.steam/root",
        ".local/share/Steam",
        ".steam/steam",
        ".steam/root",
        ".steam/debian-installation",
    };
    
    private static Path locateSteamLinux() {
        String home = System.getProperty("user.home");
        
        for (String pathStr : steamLinuxCandidates) {
            Path path = Paths.get(home, pathStr);
            
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        return null;
    }
    
}
