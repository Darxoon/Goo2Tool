package com.crazine.goo2tool.functional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crazine.goo2tool.gui.util.FX_Alarm;
import com.crazine.goo2tool.util.Platform;

public class LocateGooDir {
    
    public static record GooDir(Path path, Optional<Path> steamDir) {}
    
    private LocateGooDir() {}
    
    public static Optional<GooDir> locateWog2() {
        switch (Platform.getCurrent()) {
            case WINDOWS: {
                // Detect Steam
                Path steamDir = locateSteamWindows();
                
                if (steamDir != null) {
                    Path wog2Dir = steamDir.resolve("steamapps/common/World of Goo 2");
                    
                    if (Files.exists(wog2Dir))
                        return Optional.of(new GooDir(wog2Dir, Optional.of(steamDir)));
                }
                
                // Detect DRM-free version in Program Files
                Path exeFile = Paths.get(System.getenv("PROGRAMFILES"), "World of Goo 2/World of Goo 2.exe");
                
                if (Files.isRegularFile(exeFile))
                    return Optional.of(new GooDir(exeFile.getParent(), Optional.empty()));
                else
                    return Optional.empty();
            }
            case MAC:
                // TODO (priority): Detect Steam
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
    
    // Windows
    private static final String WINDOWS_REG_COMMAND =
        "reg query \"HKCU\\Software\\Valve\\Steam\" /v SteamPath 2>nul || reg query \"HKLM\\Software\\Valve\\Steam\" /v InstallPath 2>nul";
    
    private static final Pattern WINDOWS_REG_REGEX =
        Pattern.compile("(?:SteamPath|InstallPath)\\s+REG_SZ\\s+(.+)");
    
    private static final String[] STEAM_WINDOWS_CANDIDATES = new String[] {
        "C:\\Program Files (x86)\\Steam",
        "C:\\Program Files\\Steam",
        System.getenv("LocalAppData") + "/Steam",
        System.getenv("AppData") + "/Steam",
    };
    
    private static Path locateSteamWindows() {
        try {
            
            // query registry
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/C", WINDOWS_REG_COMMAND);
            Process process = processBuilder.start();
            process.waitFor();
            
            // parse result of command
            String regResult = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = WINDOWS_REG_REGEX.matcher(regResult);
            
            if (matcher.find()) {
                Path steamPath = Paths.get(matcher.group(1));
                return steamPath.toRealPath();
            }
            
        } catch (IOException e) {
            FX_Alarm.error(e);
        } catch (InterruptedException e) {
            FX_Alarm.error(e);
        }
        
        for (String candidate : STEAM_WINDOWS_CANDIDATES) {
            Path path = Paths.get(candidate);
            
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        return null;
    }
    
    // Linux
    private static final String[] STEAM_LINUX_CANDIDATES = new String[] {
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
        
        for (String pathStr : STEAM_LINUX_CANDIDATES) {
            Path path = Paths.get(home, pathStr);
            
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        return null;
    }
    
}
