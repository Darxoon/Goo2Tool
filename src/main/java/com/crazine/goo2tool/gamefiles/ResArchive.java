package com.crazine.goo2tool.gamefiles;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class ResArchive implements Closeable {
    
    public static record ResFile(String path, byte[] content) {
        public static ResFile fromZipEntry(ZipFile file, ZipEntry entry) throws IOException {
            return new ResFile(entry.getName(), file.getInputStream(entry).readAllBytes());
        }
    }
    
    private ZipFile zipFile;
    
    public static ResArchive loadOrSetupVanilla(Stage stage) throws IOException {
        String baseWOG2 = PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory();

        File resGooFile;
        if (Files.exists(Path.of(baseWOG2 + "/game/res.goo"))) {
            resGooFile = new File(baseWOG2 + "/game/res.goo");
        } else {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setContentText("Could not find res.goo file as it appears to have been renamed or moved.\n\n"
                    + "Please pick the new location of the file instead.");
            alert.showAndWait();
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new ExtensionFilter("res.goo Archive", "*.*"));
            
            File initialDirectory = new File(baseWOG2 + "/game");
            if (initialDirectory.exists())
                fileChooser.setInitialDirectory(initialDirectory);
            
            resGooFile = fileChooser.showOpenDialog(stage);
        }
        
        return new ResArchive(resGooFile);
    }
    
    public ResArchive(File inputFile) throws IOException {
        zipFile = new ZipFile(inputFile);
    }
    
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

    public Iterable<ResFile> getAllFiles() {
        return new Iterable<>() {
            
            @Override
            public Iterator<ResFile> iterator() {
                // potential speed up: https://stackoverflow.com/questions/20717897/multithreaded-unzipping-in-java
                Enumeration<? extends ZipEntry> entries = ResArchive.this.zipFile.entries();
                Spliterator<? extends ZipEntry> spliterator = Spliterators.spliteratorUnknownSize(entries.asIterator(), 0);
                
                return StreamSupport.stream(spliterator, false)
                    .parallel()
                    .filter(entry -> !entry.isDirectory())
                    .map(entry -> {
                        try {
                            return ResArchive.ResFile.fromZipEntry(ResArchive.this.zipFile, entry);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).iterator();
            }
            
        };
    }
    
    public int fileCount() {
        return zipFile.size();
    }
    
    @Override
    public void close() throws IOException {
        zipFile.close();
    }
}
