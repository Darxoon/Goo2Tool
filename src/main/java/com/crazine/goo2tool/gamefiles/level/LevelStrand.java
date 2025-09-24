package com.crazine.goo2tool.gamefiles.level;

public class LevelStrand {
    
    private int ball1UID;
    private int ball2UID;
    private int type;
    private boolean filled;
    
    public int getBall1UID() {
        return ball1UID;
    }
    public void setBall1UID(int ball1uid) {
        ball1UID = ball1uid;
    }
    public int getBall2UID() {
        return ball2UID;
    }
    public void setBall2UID(int ball2uid) {
        ball2UID = ball2uid;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public boolean isFilled() {
        return filled;
    }
    public void setFilled(boolean filled) {
        this.filled = filled;
    }
    
}
