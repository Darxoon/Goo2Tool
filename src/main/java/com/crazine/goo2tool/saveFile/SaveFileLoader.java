package com.crazine.goo2tool.saveFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;

public class SaveFileLoader {

    /** Fully parses the "wog2_1.dat" file that encodes World of Goo 2 save data. */
    public static WOG2SaveData[] readSaveFile(File saveFile) throws IOException {

        String saveFileContent = Files.readString(saveFile.toPath());

        // Parse the save file for the initial comma-separated values
        ArrayList<String> rootLevelValues = new ArrayList<>();
        int charIndex = 0;
        while (charIndex < saveFileContent.length()) {

            int nextCommaIndex = saveFileContent.indexOf(",", charIndex + 1);
            if (nextCommaIndex == -1) break;

            // Read the next value (everything from now until the next ',')
            String nextValue = saveFileContent.substring(charIndex, nextCommaIndex);

            if (nextValue.startsWith("{")) {
                // If the value begins with '{', then we need to skip through the following object.

                int bracketsLevel = 1;

                while (bracketsLevel != 0) {
                    int nextOpeningBracketIndex = saveFileContent.indexOf("{", nextCommaIndex + 1);
                    int nextClosingBracketIndex = saveFileContent.indexOf("}", nextCommaIndex + 1);
                    if (nextOpeningBracketIndex != -1 && nextOpeningBracketIndex < nextClosingBracketIndex) {
                        nextCommaIndex = nextOpeningBracketIndex;
                        bracketsLevel++;
                    } else {
                        nextCommaIndex = nextClosingBracketIndex;
                        bracketsLevel--;
                    }
                }

                String entireSkippedObject = saveFileContent.substring(charIndex, nextCommaIndex + 1);
                rootLevelValues.add(entireSkippedObject);

            } else {

                // Otherwise, it's some piece of data we can extract.
                rootLevelValues.add(nextValue);

            }

            charIndex = nextCommaIndex + 1;

        }

        ArrayList<WOG2SaveData> saveData = new ArrayList<>();
        ObjectMapper saveDataMapper = new ObjectMapper();

        ArrayList<String> currentSaveFileValues = new ArrayList<>();
        for (String value : rootLevelValues) {

            if (value.startsWith("{")) {
                WOG2SaveData.WOG2SaveFile saveFile1 = saveDataMapper.readValue(value, WOG2SaveData.WOG2SaveFile.class);
                WOG2SaveData saveData1 = new WOG2SaveData();
                saveData1.setValues(new ArrayList<>(currentSaveFileValues.stream().toList()));
                saveData1.setSaveFile(saveFile1);
                saveData.add(saveData1);
                currentSaveFileValues.clear();
            } else {
                currentSaveFileValues.add(value);
            }

        }

        return saveData.toArray(new WOG2SaveData[0]);

    }


    public static void writeSaveFile(File saveFile, WOG2SaveData[] saveData) throws IOException {

        StringBuilder export = new StringBuilder();

        for (WOG2SaveData wog2SaveData : saveData) {

            for (String value : wog2SaveData.getValues()) {
                export.append(value).append(",");
            }

            JsonMapper jsonMapper = new JsonMapper();

            Writer writer = new StringWriter();
            jsonMapper.writeValue(writer, wog2SaveData.getSaveFile());
            export.append(writer);

        }

        export.append(",0.");

        Files.writeString(saveFile.toPath(), export.toString());

    }

}
