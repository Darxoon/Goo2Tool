package com.crazine.goo2tool.addinFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AddinReader implements Closeable {
    
    public static enum ResourceType {
        METADATA,
        COMPILE,
        MERGE,
        OVERRIDE
    }
    
    public static record Resource(String path, ResourceType type, byte[] content) {
        
        private static Resource fromZipEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
            String realPath = entry.getName();
            ResourceType type;
            if (realPath.startsWith("compile/")) {
                realPath = realPath.substring("compile/".length());
                type = ResourceType.COMPILE;
            } else if (realPath.startsWith("merge/")) {
                realPath = realPath.substring("merge/".length());
                type = ResourceType.MERGE;
            } else if (realPath.startsWith("override/")) {
                realPath = realPath.substring("override/".length());
                type = ResourceType.OVERRIDE;
            } else {
                type = ResourceType.METADATA;
            }
            
            byte[] content = zipFile.getInputStream(entry).readAllBytes();
            return new Resource(realPath, type, content);
        }
        
        public String contentText() {
            return new String(content, StandardCharsets.UTF_8);
        }
        
    }
    
    private final ZipFile zipFile;
    
    public AddinReader(Goo2mod mod) throws IOException {
        this.zipFile = new ZipFile(mod.getFile());
    }
    
    public AddinReader(File goo2modFile) throws IOException {
        this.zipFile = new ZipFile(goo2modFile);
    }
    
    public Optional<Resource> getFileContent(String path) throws IOException {
        for (Iterator<? extends ZipEntry> it = zipFile.stream().iterator(); it.hasNext(); ) {
            ZipEntry zipEntry = it.next();

            if (zipEntry.getName().equals(path)) {
                return Optional.of(Resource.fromZipEntry(zipFile, zipEntry));
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<String> getFileText(String path) throws IOException {
        return getFileContent(path).map(Resource::contentText);
    }
    
    public Iterable<Resource> getAllFiles() {
        return new Iterable<>() {

            @Override
            public Iterator<Resource> iterator() {
                return zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(entry -> {
                        try {
                            return Resource.fromZipEntry(zipFile, entry);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .iterator();
            }
            
        };
    }
    
    public long getFileCount() {
        return zipFile.stream()
            .filter(entry -> !entry.isDirectory())
            .count();
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }
    
}
