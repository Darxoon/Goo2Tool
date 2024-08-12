package com.crazine.goo2tool.saveFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
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

            currentSaveFileValues.add(value);

            if (value.startsWith("{")) {
                WOG2SaveData.WOG2SaveFile saveFile1 = saveDataMapper.readValue(value, WOG2SaveData.WOG2SaveFile.class);
                WOG2SaveData saveData1 = new WOG2SaveData();
                saveData1.setValues((ArrayList<String>) currentSaveFileValues.clone());
                saveData1.setSaveFile(saveFile1);
                saveData.add(saveData1);
                currentSaveFileValues.clear();
            }

        }

        return saveData.toArray(new WOG2SaveData[0]);

    }

}
