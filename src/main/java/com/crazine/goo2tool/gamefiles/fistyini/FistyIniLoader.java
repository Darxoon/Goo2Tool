package com.crazine.goo2tool.gamefiles.fistyini;

import java.util.ArrayList;
import java.util.Collections;

public class FistyIniLoader {
    
    public static class IniParseException extends Exception {
        
        private final int lineNumber;

        public IniParseException(String message, int lineNumber) {
            super(message);
            this.lineNumber = lineNumber;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
    }
    
    public static FistyIniFile loadIni(String sourceFile) throws IniParseException {
        
        ArrayList<String> entries = new ArrayList<>(DefaultFistyIni.BALL_TABLE.length);
        
        Iterable<String> lines = () -> sourceFile.lines().iterator();
        
        int lineNumber = 1;
        for (String line : lines) {
            // Trim comment
            int commentStart = line.indexOf(';');
            if (commentStart != -1)
                line = line.substring(0, commentStart);
            
            if (line.isBlank())
                continue;
            
            // Parse into lhs and rhs
            int equalsSignPos = line.indexOf('=');
            if (equalsSignPos == -1)
                throw new IniParseException("Line '" + line + "' does not contain a '='", lineNumber);
            
            String lhsString = line.substring(0, equalsSignPos);
            int lhs = Integer.valueOf(lhsString);
            
            String rhs = line.substring(equalsSignPos + 1).strip();
            if (rhs.contains(" "))
                throw new IniParseException("Right hand side '" + rhs + "' must not contain any whitespace except for padding", lineNumber);
            
            // Resize entries if necessary
            if (lhs >= entries.size()) {
                entries.addAll(Collections.nCopies(lhs + 1 - entries.size(), ""));
            }
            
            entries.set(lhs, rhs);
            
            lineNumber++;
        }
        
        return new FistyIniFile(sourceFile, entries);
        
    }
    
}
