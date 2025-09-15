package com.crazine.goo2tool.gamefiles;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipResArchive implements ResArchive {
    
    private ZipFile zipFile;
    
    public ZipResArchive(File inputFile) throws IOException {
        zipFile = new ZipFile(inputFile);
    }
    
    @Override
    public Optional<byte[]> getFileContent(String path) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory())
                continue;
            
            if (entry.getName() != null && entry.getName().equals(path)) {
                return Optional.of(zipFile.getInputStream(entry).readAllBytes());
            }
        }
        
        return Optional.empty();
    }

    @Override
    public Iterable<ResFile> getAllFiles() {
        return new Iterable<>() {
            
            @Override
            public Iterator<ResFile> iterator() {
                // potential speed up: https://stackoverflow.com/questions/20717897/multithreaded-unzipping-in-java
                return ZipResArchive.this.zipFile.stream()
                    .parallel()
                    .filter(entry -> !entry.isDirectory())
                    .map(entry -> {
                        try {
                            return fromZipEntry(ZipResArchive.this.zipFile, entry);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).iterator();
            }
            
        };
    }
    
    private static ResFile fromZipEntry(ZipFile file, ZipEntry entry) throws IOException {
        return new ResFile(entry.getName(), file.getInputStream(entry).readAllBytes());
    }
    
    @Override
    public int fileCount() {
        return zipFile.size();
    }
    
    @Override
    public void close() throws IOException {
        zipFile.close();
    }
    
}
