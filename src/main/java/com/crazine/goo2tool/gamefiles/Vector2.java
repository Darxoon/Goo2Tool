package com.crazine.goo2tool.gamefiles;

public record Vector2(float x, float y) {
    
    public static final Vector2 ZERO = new Vector2(0.0f, 0.0f);
    
    public Vector2 withX(float x) {
        return new Vector2(x, this.y);
    }
    
    public Vector2 withY(float y) {
        return new Vector2(this.x, y);
    }
    
}
