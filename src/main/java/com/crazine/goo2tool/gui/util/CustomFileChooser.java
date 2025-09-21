package com.crazine.goo2tool.gui.util;

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

    public static Path chooseFile(Stage stage, String title, ExtensionFilter... filters) throws IOException {
        if (Platform.getCurrent() == Platform.LINUX) {
            logger.info("Opening xdg-desktop-portal FileChooser");
            try (DBusConnection connection = DBusConnectionBuilder.forSessionBus().build()) {
                Map<String, Variant<?>> options = new HashMap<>();
                
                if (filters != null && filters.length > 0) {
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
                
                Map<String, Variant<?>> results = makeDBusCall(connection, title, options);
                List<?> uris = (List<?>) results.get("uris").getValue();
                
                URI uri;
                try {
                    uri = new URI((String) uris.get(0));
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
                
                return Path.of(uri);
            } catch (DBusException e) {
                throw new IOException(e);
            }
        } else {
            logger.info("Opening default FileChooser");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters().addAll(filters);
            return fileChooser.showOpenDialog(stage).toPath();
        }
    }
    
    public static Path chooseDirectory() {
        return null;
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
