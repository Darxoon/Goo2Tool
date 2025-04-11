package com.crazine.goo2tool.functional;

import java.io.Closeable;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.crazine.goo2tool.addinFile.Goo2mod;

public class AddinReader implements Closeable {
    public static enum ResourceType {
        METADATA,
        COMPILE,
        MERGE,
        OVERRIDE
    }
    
    public static record Resource(String path, ResourceType type, byte[] content) {}
    
    private final ZipFile zipFile;
    
    public AddinReader(Goo2mod mod) throws IOException {
        this.zipFile = new ZipFile(mod.getFile());
    }
    
    public Iterable<Resource> getAllFiles() {
        return new Iterable<>() {

            @Override
            public Iterator<Resource> iterator() {
                Enumeration<? extends ZipEntry> entries = AddinReader.this.zipFile.entries();
                Spliterator<? extends ZipEntry> spliterator = Spliterators.spliteratorUnknownSize(entries.asIterator(), 0);
                
                return StreamSupport.stream(spliterator, false)
                    .filter(entry ->!entry.isDirectory())
                    .map(entry -> {
                        try {
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
                            
                            byte[] content = AddinReader.this.zipFile.getInputStream(entry).readAllBytes();
                            return new Resource(realPath, type, content);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .iterator();
            }
            
        };
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }
    
}
