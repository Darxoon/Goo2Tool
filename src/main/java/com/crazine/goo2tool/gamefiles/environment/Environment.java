package com.crazine.goo2tool.gamefiles.environment;

import java.util.ArrayList;
import java.util.List;

import com.crazine.goo2tool.gamefiles.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Environment {
    
    public static class Layer {
        
        private String imageName;
        private int scale;
        private int depth;
        private boolean fill;
        private boolean foreground;
        private boolean repeatX;
        private boolean repeatY;
        private Vector2 anchors;
        private float bloom;
        private long color;
        private int blendingType;
        private String flashAnimationName;
        private boolean isFlashAnimation;
        private Vector2 animScroll;
        
        public String getImageName() {
            return imageName;
        }
        public void setImageName(String imageName) {
            this.imageName = imageName;
        }
        public int getScale() {
            return scale;
        }
        public void setScale(int scale) {
            this.scale = scale;
        }
        public int getDepth() {
            return depth;
        }
        public void setDepth(int depth) {
            this.depth = depth;
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
        public Vector2 getAnchors() {
            return anchors;
        }
        public void setAnchors(Vector2 anchors) {
            this.anchors = anchors;
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
        
    }
    
    private String name;
    private String id;
    private List<Layer> layers = new ArrayList<>();
    // ...
    
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
    
}
