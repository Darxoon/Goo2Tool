package com.crazine.goo2tool.gamefiles;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class ResArchive implements Closeable {
    
    public static record ResFile(String path, byte[] content) {
        
        private static ResFile fromZipEntry(ZipFile file, ZipEntry entry) throws IOException {
            return new ResFile(entry.getName(), file.getInputStream(entry).readAllBytes());
        }
        
    }
    
    private ZipFile zipFile;
    
    public static ResArchive loadOrSetupVanilla(Stage stage) throws IOException {
        Properties properties = PropertiesLoader.getProperties();
        String baseWOG2 = properties.getBaseWorldOfGoo2Directory();
        String resGooPath = properties.getResGooPath();

        File resGooFile;
        if (!resGooPath.isEmpty() && Files.exists(Path.of(resGooPath))) {
            resGooFile = new File(resGooPath);
        } else if (Files.exists(Path.of(baseWOG2, "game/res.goo"))) {
            Path baseFile = Path.of(baseWOG2, "game/res.goo");
            
            if (properties.isSteam()) {
                Path newFile = Path.of(baseWOG2, "game/res.goo_backup");
                Files.move(baseFile, newFile, StandardCopyOption.REPLACE_EXISTING);
                properties.setResGooPath(newFile.toString());
                resGooFile = newFile.toFile();
            } else {
                resGooFile = baseFile.toFile();
            }
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
            
            File chosenFile = fileChooser.showOpenDialog(stage);
            PropertiesLoader.getProperties().setResGooPath(chosenFile.toString());
            resGooFile = chosenFile;
        }
        
        PropertiesLoader.saveProperties();
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
    
    public Optional<String> getFileText(String path) throws IOException {
        return getFileContent(path).map(content -> new String(content, StandardCharsets.UTF_8));
    }

    public Iterable<ResFile> getAllFiles() {
        return new Iterable<>() {
            
            @Override
            public Iterator<ResFile> iterator() {
                // potential speed up: https://stackoverflow.com/questions/20717897/multithreaded-unzipping-in-java
                return ResArchive.this.zipFile.stream()
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
