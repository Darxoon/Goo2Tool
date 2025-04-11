package com.crazine.goo2tool.res;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.crazine.goo2tool.properties.PropertiesLoader;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class ResArchive implements Closeable {
    private ZipFile zipFile;
    
    public static ResArchive loadVanilla(Stage stage) throws IOException {
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

    @Override
    public void close() throws IOException {
        zipFile.close();
    }
}
