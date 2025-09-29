package com.crazine.goo2tool.gamefiles.environment;

import java.util.ArrayList;
import java.util.List;

import com.crazine.goo2tool.gamefiles.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Environment {
    
    @JsonInclude(Include.NON_NULL)
    public static class Layer {
        
        private String imageName;
        private float scale = 1.0f;
        private float depth;
        private float depthYOverride;
        private boolean fill;
        private boolean foreground;
        private boolean repeatX;
        private boolean repeatY;
        private boolean mirrorX;
        private boolean mirrorY;
        private boolean flipX;
        private boolean flipY;
        private Vector2 anchors;
        private boolean anchorsTakeDepthIntoAccount;
        private float bloom;
        private long color = 0xffffffff;
        private int blendingType = 2;
        private String flashAnimationName;
        private boolean isFlashAnimation;
        private Vector2 animScroll;
        
        // ?
        private JsonNode gradient;
        
        public String getImageName() {
            return imageName;
        }
        public void setImageName(String imageName) {
            this.imageName = imageName;
        }
        public float getScale() {
            return scale;
        }
        public void setScale(float scale) {
            this.scale = scale;
        }
        public float getDepth() {
            return depth;
        }
        public void setDepth(float depth) {
            this.depth = depth;
        }
        public float getDepthYOverride() {
            return depthYOverride;
        }
        public void setDepthYOverride(float depthYOverride) {
            this.depthYOverride = depthYOverride;
        }
        public boolean isFill() {
            return fill;
        }
        public void setFill(boolean fill) {
            this.fill = fill;
        }
        public boolean isForeground() {
            return foreground;
        }
        public void setForeground(boolean foreground) {
            this.foreground = foreground;
        }
        public boolean isRepeatX() {
            return repeatX;
        }
        public void setRepeatX(boolean repeatX) {
            this.repeatX = repeatX;
        }
        public boolean isRepeatY() {
            return repeatY;
        }
        public void setRepeatY(boolean repeatY) {
            this.repeatY = repeatY;
        }
        public boolean isMirrorX() {
            return mirrorX;
        }
        public void setMirrorX(boolean mirrorX) {
            this.mirrorX = mirrorX;
        }
        public boolean isMirrorY() {
            return mirrorY;
        }
        public void setMirrorY(boolean mirrorY) {
            this.mirrorY = mirrorY;
        }
        public boolean isFlipX() {
            return flipX;
        }
        public void setFlipX(boolean flipX) {
            this.flipX = flipX;
        }
        public boolean isFlipY() {
            return flipY;
        }
        public void setFlipY(boolean flipY) {
            this.flipY = flipY;
        }
        public Vector2 getAnchors() {
            return anchors;
        }
        public void setAnchors(Vector2 anchors) {
            this.anchors = anchors;
        }
        public boolean isAnchorsTakeDepthIntoAccount() {
            return anchorsTakeDepthIntoAccount;
        }
        public void setAnchorsTakeDepthIntoAccount(boolean anchorsTakeDepthIntoAccount) {
            this.anchorsTakeDepthIntoAccount = anchorsTakeDepthIntoAccount;
        }
        public float getBloom() {
            return bloom;
        }
        public void setBloom(float bloom) {
            this.bloom = bloom;
        }
        public long getColor() {
            return color;
        }
        public void setColor(long color) {
            this.color = color;
        }
        public int getBlendingType() {
            return blendingType;
        }
        public void setBlendingType(int blendingType) {
            this.blendingType = blendingType;
        }
        public String getFlashAnimationName() {
            return flashAnimationName;
        }
        public void setFlashAnimationName(String flashAnimationName) {
            this.flashAnimationName = flashAnimationName;
        }
        @JsonProperty("isFlashAnimation") public boolean isFlashAnimation() {
            return isFlashAnimation;
        }
        @JsonProperty("isFlashAnimation") public void setFlashAnimation(boolean isFlashAnimation) {
            this.isFlashAnimation = isFlashAnimation;
        }
        public Vector2 getAnimScroll() {
            return animScroll;
        }
        public void setAnimScroll(Vector2 animScroll) {
            this.animScroll = animScroll;
        }
        public JsonNode getGradient() {
            return gradient;
        }
        public void setGradient(JsonNode gradient) {
            this.gradient = gradient;
        }
        
    }
    
    private String name;
    private String id;
    private List<Layer> layers = new ArrayList<>();
    // ...
    
    private String fireLut;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public List<Layer> getLayers() {
        return layers;
    }
    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }
    
    public String getFireLut() {
        return fireLut;
    }
    public void setFireLut(String fireLut) {
        this.fireLut = fireLut;
    }
    
}
