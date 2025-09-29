package com.crazine.goo2tool.gamefiles.item;

import java.util.ArrayList;
import java.util.List;

import com.crazine.goo2tool.gamefiles.Vector2;
import com.fasterxml.jackson.databind.JsonNode;

public class ItemObject {
    
    private String name;
    private int randomizationGroup;
    private Vector2 position = Vector2.ZERO;
    private float rotation;
    private Vector2 scale = Vector2.ONE;
    private long color = 0xffffffff;
    private int depthOffset;
    private int sortOffset;
    private int imageBlendingType = 2;
    private Vector2 pivot = new Vector2(0.5f, 0.5f);
    private float rotationSpeed;
    private boolean invisible;
    private boolean clickable;
    private int stableFluidsDensityFactor;
    private float dynamicLightingFactor;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private boolean ignoreScale;
    private float shaderFactor = 1.0f;
    private boolean enableWind;
    private Vector2 windFactor = Vector2.ZERO;
    private int stencilMode;
    private int stencilMask;
    private int alphaTestValue;
    
    // ?
    // contains material though, for materials.wog2 merge
    private JsonNode body;
    
    private JsonNode colorFx;
    private List<JsonNode> particleEffects = new ArrayList<>();
    private List<JsonNode> points = new ArrayList<>();
    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getRandomizationGroup() {
        return randomizationGroup;
    }
    public void setRandomizationGroup(int randomizationGroup) {
        this.randomizationGroup = randomizationGroup;
    }
    public Vector2 getPosition() {
        return position;
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }
    public float getRotation() {
        return rotation;
    }
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    public Vector2 getScale() {
        return scale;
    }
    public void setScale(Vector2 scale) {
        this.scale = scale;
    }
    public long getColor() {
        return color;
    }
    public void setColor(long color) {
        this.color = color;
    }
    public int getDepthOffset() {
        return depthOffset;
    }
    public void setDepthOffset(int depthOffset) {
        this.depthOffset = depthOffset;
    }
    public int getSortOffset() {
        return sortOffset;
    }
    public void setSortOffset(int sortOffset) {
        this.sortOffset = sortOffset;
    }
    public int getImageBlendingType() {
        return imageBlendingType;
    }
    public void setImageBlendingType(int imageBlendingType) {
        this.imageBlendingType = imageBlendingType;
    }
    public Vector2 getPivot() {
        return pivot;
    }
    public void setPivot(Vector2 pivot) {
        this.pivot = pivot;
    }
    public float getRotationSpeed() {
        return rotationSpeed;
    }
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }
    public boolean isInvisible() {
        return invisible;
    }
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
    public boolean isClickable() {
        return clickable;
    }
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    public int getStableFluidsDensityFactor() {
        return stableFluidsDensityFactor;
    }
    public void setStableFluidsDensityFactor(int stableFluidsDensityFactor) {
        this.stableFluidsDensityFactor = stableFluidsDensityFactor;
    }
    public float getDynamicLightingFactor() {
        return dynamicLightingFactor;
    }
    public void setDynamicLightingFactor(float dynamicLightingFactor) {
        this.dynamicLightingFactor = dynamicLightingFactor;
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
    public boolean isIgnoreScale() {
        return ignoreScale;
    }
    public void setIgnoreScale(boolean ignoreScale) {
        this.ignoreScale = ignoreScale;
    }
    public float getShaderFactor() {
        return shaderFactor;
    }
    public void setShaderFactor(float shaderFactor) {
        this.shaderFactor = shaderFactor;
    }
    public boolean isEnableWind() {
        return enableWind;
    }
    public void setEnableWind(boolean enableWind) {
        this.enableWind = enableWind;
    }
    public Vector2 getWindFactor() {
        return windFactor;
    }
    public void setWindFactor(Vector2 windFactor) {
        this.windFactor = windFactor;
    }
    public int getStencilMode() {
        return stencilMode;
    }
    public void setStencilMode(int stencilMode) {
        this.stencilMode = stencilMode;
    }
    public int getStencilMask() {
        return stencilMask;
    }
    public void setStencilMask(int stencilMask) {
        this.stencilMask = stencilMask;
    }
    public int getAlphaTestValue() {
        return alphaTestValue;
    }
    public void setAlphaTestValue(int alphaTestValue) {
        this.alphaTestValue = alphaTestValue;
    }
    public JsonNode getBody() {
        return body;
    }
    public void setBody(JsonNode body) {
        this.body = body;
    }
    public JsonNode getColorFx() {
        return colorFx;
    }
    public void setColorFx(JsonNode colorFx) {
        this.colorFx = colorFx;
    }
    public List<JsonNode> getParticleEffects() {
        return particleEffects;
    }
    public void setParticleEffects(List<JsonNode> particleEffects) {
        this.particleEffects = particleEffects;
    }
    public List<JsonNode> getPoints() {
        return points;
    }
    public void setPoints(List<JsonNode> points) {
        this.points = points;
    }
    
}
