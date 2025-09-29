package com.crazine.goo2tool.gamefiles.level;

import java.util.ArrayList;
import java.util.List;

import com.crazine.goo2tool.gamefiles.Vector2;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LevelItem {
    
    public static class UserVariable {
        
        private float value;

        @JsonCreator
        public UserVariable(@JsonProperty("value") float value) {
            this.value = value;
        }
        
        public float getValue() {
            return value;
        }
        public void setValue(float value) {
            this.value = value;
        }
        
    }
    
    private String id;
    private String type;
    private String localizedStringId;
    private int uid;
    private Vector2 pos = Vector2.ZERO;
    private Vector2 scale = Vector2.ONE;
    private float rotation;
    private float depth;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private float rotSpeed = 1.0f;
    private int seed;
    private int liquidType;
    private int adapterBallId;
    private boolean invisible;
    private int forcedRandomizationIndex = 0xffffffff;
    private String particleEffectName;
    private int uid1;
    private int uid2;
    
    private List<UserVariable> userVariables = new ArrayList<>();
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLocalizedStringId() {
        return localizedStringId;
    }
    public void setLocalizedStringId(String localizedStringId) {
        this.localizedStringId = localizedStringId;
    }
    
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public Vector2 getPos() {
        return pos;
    }
    public void setPos(Vector2 pos) {
        this.pos = pos;
    }
    
    public Vector2 getScale() {
        return scale;
    }
    public void setScale(Vector2 scale) {
        this.scale = scale;
    }
    
    public float getRotation() {
        return rotation;
    }
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    
    public float getDepth() {
        return depth;
    }
    public void setDepth(float depth) {
        this.depth = depth;
    }
    
    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }
    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }
    
    public boolean isFlipVertical() {
        return flipVertical;
    }
    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }
    
    public float getRotSpeed() {
        return rotSpeed;
    }
    public void setRotSpeed(float rotSpeed) {
        this.rotSpeed = rotSpeed;
    }
    
    public int getSeed() {
        return seed;
    }
    public void setSeed(int seed) {
        this.seed = seed;
    }
    
    public int getLiquidType() {
        return liquidType;
    }
    public void setLiquidType(int liquidType) {
        this.liquidType = liquidType;
    }
    
    public int getAdapterBallId() {
        return adapterBallId;
    }
    public void setAdapterBallId(int adapterBallId) {
        this.adapterBallId = adapterBallId;
    }
    
    public boolean isInvisible() {
        return invisible;
    }
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
    
    public int getForcedRandomizationIndex() {
        return forcedRandomizationIndex;
    }
    public void setForcedRandomizationIndex(int forcedRandomizationIndex) {
        this.forcedRandomizationIndex = forcedRandomizationIndex;
    }
    
    public String getParticleEffectName() {
        return particleEffectName;
    }
    public void setParticleEffectName(String particleEffectName) {
        this.particleEffectName = particleEffectName;
    }
    
    public int getUid1() {
        return uid1;
    }
    public void setUid1(int uid1) {
        this.uid1 = uid1;
    }
    
    public int getUid2() {
        return uid2;
    }
    public void setUid2(int uid2) {
        this.uid2 = uid2;
    }
    
    public List<UserVariable> getUserVariables() {
        return userVariables;
    }
    public void setUserVariables(List<UserVariable> userVariables) {
        this.userVariables = userVariables;
    }
    
}