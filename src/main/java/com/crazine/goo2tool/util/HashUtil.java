package com.crazine.goo2tool.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtil {
    
    private static MessageDigest md5 = null;
    
    public static void init() throws NoSuchAlgorithmException {
        md5 = MessageDigest.getInstance("MD5");
    }
    
    public static String getMD5Hash(byte[] fileContent) {
        if (md5 == null)
            throw new RuntimeException("MD5 hasher has not been initialized or was not initialized properly");
        
        byte[] hashBytes = md5.digest(fileContent);
        return HexFormat.of().formatHex(hashBytes);
    }
    
}
