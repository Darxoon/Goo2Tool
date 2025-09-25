package com.crazine.goo2tool.gamefiles;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipResArchive implements ResArchive {
    
    private static class ZipResFile implements ResFile {

        private final String path;
        private final ZipFile zipFile;
        private final ZipEntry zipEntry;
        
        public ZipResFile(String path, ZipFile zipFile, ZipEntry zipEntry) {
            this.path = path;
            this.zipFile = zipFile;
            this.zipEntry = zipEntry;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public byte[] readContent() throws IOException {
            return zipFile.getInputStream(zipEntry).readAllBytes();
        }
        
    }
    
    private ZipFile zipFile;
    
    public ZipResArchive(File inputFile) throws IOException {
        zipFile = new ZipFile(inputFile);
    }
    
    @Override
    public Optional<byte[]> getFileContent(String path) throws IOException {
        ZipEntry entry = zipFile.getEntry(path);
        
        if (entry == null || entry.isDirectory())
            return Optional.empty();
        
        return Optional.of(zipFile.getInputStream(entry).readAllBytes());
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
        return new ZipResFile(entry.getName(), file, entry);
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
