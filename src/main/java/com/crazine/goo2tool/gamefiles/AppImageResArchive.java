package com.crazine.goo2tool.gamefiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;

public class AppImageResArchive implements ResArchive {

    private static class AppImageResFile implements ResFile {

        private final String path;
        private final Path fsPath;
        
        public AppImageResFile(String path, Path fsPath) {
            this.path = path;
            this.fsPath = fsPath;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public byte[] readContent() throws IOException {
            return Files.readAllBytes(fsPath);
        }
        
    }
    
    private Process appImageProcess;
    private Path gameDir;
    
    private int fileCount = -1;
    
    private AppImageResArchive(Process appImageProcess, Path gameDir) {
        this.appImageProcess = appImageProcess;
        this.gameDir = gameDir;
    }

    public static AppImageResArchive open(Path appImage) throws IOException {
        if (!appImage.toString().endsWith(".AppImage") || !Files.isRegularFile(appImage))
            throw new IllegalArgumentException("File " + appImage + " is not an AppImage");
        
        // dangerous but necessary :/
        ProcessBuilder processBuilder = new ProcessBuilder(appImage.toAbsolutePath().toString(), "--appimage-mount");
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        Path mountDir = Path.of(reader.readLine());
        if (!Files.isDirectory(mountDir))
            throw new IOException("Failed to mount AppImage " + appImage);
        
        return new AppImageResArchive(process, mountDir.resolve("game"));
    }
    
    public Path getExecutable() {
        return gameDir.resolve("../WorldOfGoo2").normalize();
    }
    
    @Override
    public Optional<byte[]> getFileContent(String path) throws IOException {
        Path filePath = gameDir.resolve(path);
        
        try {
            return Optional.of(Files.readAllBytes(filePath));
        } catch (NoSuchFileException e) {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<ResFile> getAllFiles() {
        return new Iterable<ResArchive.ResFile>() {

            @Override
            public Iterator<ResFile> iterator() {
                try {
                    return Files.walk(gameDir)
                        .filter(path -> Files.isRegularFile(path))
                        .map(path -> getFile(path))
                        .iterator();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            
            private ResFile getFile(Path path) {
                Path relativePath = gameDir.relativize(path);
                return new AppImageResFile(relativePath.toString(), path);
            }
            
        };
    }

    @Override
    public int fileCount() throws IOException {
        if (fileCount < 0)
            calculateFileCount();
        
        return fileCount;
    }
    
    private void calculateFileCount() throws IOException {
        fileCount = (int) Files.walk(gameDir)
            .filter(path -> Files.isRegularFile(path))
            .count();
    }
    
    @Override
    public void close() throws IOException {
        assert appImageProcess.supportsNormalTermination();
        appImageProcess.destroy();
    }
    
}
