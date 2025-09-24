package com.crazine.goo2tool.gamefiles.level;

import com.crazine.goo2tool.gamefiles.Vector2;

public class LevelBallInstance {
    
    private int typeEnum;
    private int uid;
    private Vector2 pos = Vector2.ZERO;
    private float angle;
    private int terrainGroup;
    private boolean discovered;
    private boolean floatingWhileAsleep;
    private boolean interactive;
    private boolean wakeWithLiquid;
    private boolean exitPipeAlert;
    private boolean affectsAutoBounds;
    private float launcherLifespanMin;
    private float launcherLifespanMax;
    private float launcherForceFactor;
    private boolean launcherCanUseBalls;
    private float launcherKnockbackFactor;
    private int launcherMaxActive;
    private int launcherBallTypeToGenerate;
    private float thrustForce;
    private float maxVelocity;
    private float stiffness;
    private boolean filled;
    private int detonationRadius;
    private int detonationForce;
    
    public int getTypeEnum() {
        return typeEnum;
    }
    public void setTypeEnum(int typeEnum) {
        this.typeEnum = typeEnum;
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
    public float getAngle() {
        return angle;
    }
    public void setAngle(float angle) {
        this.angle = angle;
    }
    public int getTerrainGroup() {
        return terrainGroup;
    }
    public void setTerrainGroup(int terrainGroup) {
        this.terrainGroup = terrainGroup;
    }
    public boolean isDiscovered() {
        return discovered;
    }
    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }
    public boolean isFloatingWhileAsleep() {
        return floatingWhileAsleep;
    }
    public void setFloatingWhileAsleep(boolean floatingWhileAsleep) {
        this.floatingWhileAsleep = floatingWhileAsleep;
    }
    public boolean isInteractive() {
        return interactive;
    }
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
    public boolean isWakeWithLiquid() {
        return wakeWithLiquid;
    }
    public void setWakeWithLiquid(boolean wakeWithLiquid) {
        this.wakeWithLiquid = wakeWithLiquid;
    }
    public boolean isExitPipeAlert() {
        return exitPipeAlert;
    }
    public void setExitPipeAlert(boolean exitPipeAlert) {
        this.exitPipeAlert = exitPipeAlert;
    }
    public boolean isAffectsAutoBounds() {
        return affectsAutoBounds;
    }
    public void setAffectsAutoBounds(boolean affectsAutoBounds) {
        this.affectsAutoBounds = affectsAutoBounds;
    }
    public float getLauncherLifespanMin() {
        return launcherLifespanMin;
    }
    public void setLauncherLifespanMin(float launcherLifespanMin) {
        this.launcherLifespanMin = launcherLifespanMin;
    }
    public float getLauncherLifespanMax() {
        return launcherLifespanMax;
    }
    public void setLauncherLifespanMax(float launcherLifespanMax) {
        this.launcherLifespanMax = launcherLifespanMax;
    }
    public float getLauncherForceFactor() {
        return launcherForceFactor;
    }
    public void setLauncherForceFactor(float launcherForceFactor) {
        this.launcherForceFactor = launcherForceFactor;
    }
    public boolean isLauncherCanUseBalls() {
        return launcherCanUseBalls;
    }
    public void setLauncherCanUseBalls(boolean launcherCanUseBalls) {
        this.launcherCanUseBalls = launcherCanUseBalls;
    }
    public float getLauncherKnockbackFactor() {
        return launcherKnockbackFactor;
    }
    public void setLauncherKnockbackFactor(float launcherKnockbackFactor) {
        this.launcherKnockbackFactor = launcherKnockbackFactor;
    }
    public int getLauncherMaxActive() {
        return launcherMaxActive;
    }
    public void setLauncherMaxActive(int launcherMaxActive) {
        this.launcherMaxActive = launcherMaxActive;
    }
    public int getLauncherBallTypeToGenerate() {
        return launcherBallTypeToGenerate;
    }
    public void setLauncherBallTypeToGenerate(int launcherBallTypeToGenerate) {
        this.launcherBallTypeToGenerate = launcherBallTypeToGenerate;
    }
    public float getThrustForce() {
        return thrustForce;
    }
    public void setThrustForce(float thrustForce) {
        this.thrustForce = thrustForce;
    }
    public float getMaxVelocity() {
        return maxVelocity;
    }
    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }
    public float getStiffness() {
        return stiffness;
    }
    public void setStiffness(float stiffness) {
        this.stiffness = stiffness;
    }
    public boolean isFilled() {
        return filled;
    }
    public void setFilled(boolean filled) {
        this.filled = filled;
    }
    public int getDetonationRadius() {
        return detonationRadius;
    }
    public void setDetonationRadius(int detonationRadius) {
        this.detonationRadius = detonationRadius;
    }
    public int getDetonationForce() {
        return detonationForce;
    }
    public void setDetonationForce(int detonationForce) {
        this.detonationForce = detonationForce;
    }
    
}
