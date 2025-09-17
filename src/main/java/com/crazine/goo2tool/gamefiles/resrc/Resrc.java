package com.crazine.goo2tool.gamefiles.resrc;

public interface Resrc {
    
    public static record SetDefaults(String path, String idprefix) implements Resrc {
        
        @Override
        public String id() {
            return null;
        }
        
    }
    
    public static record Image(String id, String path) implements Resrc {}
    public static record Sound(String id, String path, boolean streaming, String bus) implements Resrc {}
    public static record FlashAnim(String id, String path) implements Resrc {}
    public static record Atlas(String id, String path) implements Resrc {}
    // TODO: font
    
    public String id();
    public String path();
    
}
