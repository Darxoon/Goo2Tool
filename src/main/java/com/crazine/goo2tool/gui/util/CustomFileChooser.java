package com.crazine.goo2tool.gui.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.portal.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crazine.goo2tool.Platform;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class CustomFileChooser {
    
    // why do i have to declare this myself
    private static class CustomTuple2<T1, T2> extends Tuple {
        @Position(0) public T1 field1;
        @Position(1) public T2 field2;
        
        public CustomTuple2(T1 field1, T2 field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(CustomFileChooser.class);
    
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    public static Optional<Path> openFile(Stage stage, String title, ExtensionFilter... filters) throws IOException {
        return openFile(stage, title, null, filters);
    }
    
    public static Optional<Path> openFile(Stage stage, String title, Path initialDir, ExtensionFilter... filters) throws IOException {
        if (Platform.getCurrent() == Platform.LINUX) {
            logger.info("Opening xdg-desktop-portal FileChooser");
            
            try (DBusConnection connection = DBusConnectionBuilder.forSessionBus().build()) {
                Map<String, Variant<?>> options = new HashMap<>();
                
                updateFilters(options, filters);
                updateInitialDir(options, initialDir);
                
                Map<String, Variant<?>> results = makeDBusCall(connection, title, options);
                List<?> uris = (List<?>) results.get("uris").getValue();
                
                if (uris.isEmpty())
                    return Optional.empty();
                
                URI uri = new URI((String) uris.get(0));
                return Optional.of(Path.of(uri));
            } catch (DBusException | URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            logger.info("Opening default FileChooser");
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters().addAll(filters);
            
            if (initialDir != null)
                fileChooser.setInitialDirectory(initialDir.toFile());
            
            File result = fileChooser.showOpenDialog(stage);
            
            if (result != null)
                return Optional.of(result.toPath());
            else
                return Optional.empty();
        }
    }
    
    public static Optional<Path> chooseDirectory(Stage stage, String title) throws IOException {
        return chooseDirectory(stage, title, null);
    }
    
    public static Optional<Path> chooseDirectory(Stage stage, String title, Path initialDir) throws IOException {
        if (Platform.getCurrent() == Platform.LINUX) {
            logger.info("Opening xdg-desktop-portal DirectoryChooser");
            
            try (DBusConnection connection = DBusConnectionBuilder.forSessionBus().build()) {
                Map<String, Variant<?>> options = new HashMap<>();
                options.put("directory", new Variant<>(true));
                
                updateInitialDir(options, initialDir);
                
                Map<String, Variant<?>> results = makeDBusCall(connection, title, options);
                List<?> uris = (List<?>) results.get("uris").getValue();
                
                if (uris.isEmpty())
                    return Optional.empty();
                
                URI uri = new URI((String) uris.get(0));
                return Optional.of(Path.of(uri));
            } catch (DBusException | URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            logger.info("Opening default DirectoryChooser");
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(title);
            
            if (initialDir != null)
                directoryChooser.setInitialDirectory(initialDir.toFile());
            
            File result = directoryChooser.showDialog(stage);
            
            if (result != null)
                return Optional.of(result.toPath());
            else
                return Optional.empty();
        }
    }
    
    private static void updateFilters(Map<String, Variant<?>> options, ExtensionFilter[] filters) {
        if (filters == null || filters.length == 0)
            return;
        
        List<CustomTuple2<String, List<CustomTuple2<Integer, String>>>> dbusFilters = new ArrayList<>(Arrays.stream(filters)
            .map(filter -> {
                List<CustomTuple2<Integer, String>> extensions = new ArrayList<>(filter.getExtensions().stream()
                    .map(extension -> new CustomTuple2<>(0, extension))
                    .toList());
                
                return new CustomTuple2<>(filter.getDescription(), extensions);
            })
            .toList());
        
        options.put("filters", new Variant<>(dbusFilters, "a(sa(us))"));
    }
    
    private static void updateInitialDir(Map<String, Variant<?>> options, Path initialDir) throws IOException {
        if (initialDir == null)
            return;
        
        // imperfect size but good enough in most cases
        ByteArrayOutputStream currentFolder = new ByteArrayOutputStream(initialDir.toString().length() + 1);
        currentFolder.write(initialDir.toString().getBytes());
        currentFolder.write(0);
        options.put("current_folder", new Variant<>(currentFolder.toByteArray()));
    }
    
    private static Map<String, Variant<?>> makeDBusCall(DBusConnection connection, String title, Map<String, Variant<?>> options) throws DBusException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Map<String, Variant<?>>> results = new AtomicReference<Map<String,Variant<?>>>(null);
        
        // Set up response handler
        String handleToken = randomString(8);
        String uniqueName = connection.getUniqueName()
            .replace(":", "")
            .replace(".", "_");
        String responsePath = "/org/freedesktop/portal/desktop/request/"
            + uniqueName + "/" + handleToken;
        
        DBusMatchRule matchRule = new DBusMatchRule(Request.Response.class, null, responsePath);
        connection.addSigHandler(matchRule, (Request.Response signal) -> {
            results.set(signal.getResults());
            latch.countDown();
        });
        
        // Make portal FileChooser call
        org.freedesktop.portal.FileChooser fileChooser = connection.getRemoteObject(
                "org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop",
                org.freedesktop.portal.FileChooser.class);
        
        options.put("handle_token", new Variant<>(handleToken));
        // TODO: figure out parentWindow
        fileChooser.OpenFile("", title, options);
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            FX_Alarm.error(e);
        }
        
        return results.get();
    }
    
    private static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
    
}
