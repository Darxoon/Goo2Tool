package com.crazine.goo2tool.gamefiles.resrc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Resrc {
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface ResrcType {
        String fileExtension();
    }
    
    public static record SetDefaults(String path, String idprefix) implements Resrc {
        
        @Override
        public String id() {
            return null;
        }
        
        public boolean isEmpty() {
            return path.isEmpty() && idprefix.isEmpty();
        }
        
    }
    
    @ResrcType(fileExtension = "image")
    public static record Image(String id, String path) implements Resrc {}
    
    @ResrcType(fileExtension = "ogg")
    public static record Sound(String id, String path, boolean streaming, String bus) implements Resrc {}
    
    // file extension: ?
    public static record FlashAnim(String id, String path) implements Resrc {}
    
    @ResrcType(fileExtension = "atlas")
    public static record Atlas(String id, String path) implements Resrc {}
    
    // TODO: font
    
    public String id();
    public String path();
    
    public default String fileExtension() {
        return getFileExtension(getClass());
    }
    
    public static String getFileExtension(Class<? extends Resrc> class1) {
        ResrcType attribute = class1.getAnnotation(ResrcType.class);
        return attribute.fileExtension();
    }
    
}
