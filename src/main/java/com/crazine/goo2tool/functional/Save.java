package com.crazine.goo2tool.functional;

import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.properties.Addin;
import com.crazine.goo2tool.properties.PropertiesLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.zip.ZipFile;

public class Save {

    public static void save() {

        // Export properties
        try {
            PropertiesLoader.saveProperties(PropertiesLoader.getPropertiesFile(), PropertiesLoader.getProperties());
        } catch (IOException ignored) {

        }


        // Merge original res folder
        String baseWOG2 = PropertiesLoader.getProperties().getBaseWorldOfGoo2Directory();
        baseWOG2 = baseWOG2.substring(0, baseWOG2.lastIndexOf("\\"));
        String customWOG2 = PropertiesLoader.getProperties().getCustomWorldOfGoo2Directory();
        customWOG2 = customWOG2.substring(0, customWOG2.lastIndexOf("\\"));
        File baseFile = new File(baseWOG2);
        Stack<File> baseFiles = new Stack<>();
        baseFiles.add(baseFile);
        while (!baseFiles.isEmpty()) {

            File file = baseFiles.pop();
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                assert fileArray != null;
                baseFiles.addAll(Arrays.asList(fileArray));
            }

            String uniquePath = file.getPath().substring(baseWOG2.length());
            Path customPath = Path.of(customWOG2 + uniquePath);

            // If the file doesn't exist in the new res folder, copy it over
            if (!Files.exists(customPath)) {
                try {
                    Files.copy(file.toPath(), customPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        ArrayList<Goo2mod> goo2mods = new ArrayList<>();
        File goo2modsDirectory = new File(System.getenv("APPDATA") + "\\Goo2Tool\\addins");
        for (File file : goo2modsDirectory.listFiles()) {
            try {
                goo2mods.add(AddinFileLoader.loadGoo2mod(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        ArrayList<Goo2mod> goo2modsSorted = new ArrayList<>();
        for (Addin addin : PropertiesLoader.getProperties().getAddins()) {
            for (Goo2mod goo2mod : goo2mods) {
                if (goo2mod.getId().equals(addin.getName())) {
                    goo2modsSorted.add(goo2mod);
                    break;
                }
            }
        }

        for (Goo2mod goo2mod : goo2modsSorted) {

            try {

                ZipFile addinFile = new ZipFile(goo2mod.getFile().getPath());

                String finalCustomWOG = customWOG2;
                addinFile.entries().asIterator().forEachRemaining(zipEntry -> {

                    if (!zipEntry.getName().contains("/") || zipEntry.getName().indexOf("/",
                            zipEntry.getName().indexOf("/") + 1) == -1) return;

                    if (zipEntry.isDirectory()) return;

                    Path customPath = Path.of(finalCustomWOG + "\\game" + zipEntry.getName().substring(zipEntry.getName().indexOf("/", zipEntry.getName().indexOf("/") + 1)));
                    System.out.println(customPath);

                    // If the file doesn't exist in the new res folder, create it
                    if (!Files.exists(customPath)) {
                        try {
                            Files.createFile(customPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        Files.copy(addinFile.getInputStream(zipEntry), customPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
