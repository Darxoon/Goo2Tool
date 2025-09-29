package com.crazine.goo2tool.gamefiles.item;

public class ItemUserVariable {
    
    public static final int TYPE_GOO_BALL = 4;
    
    private String name;
    private float defaultValue;
    private float minValue;
    private float maxValue;
    private int orderIndex = 0xffffffff;
    private boolean enabled = true;
    private int type;
    private String stringValue;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public float getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(float defaultValue) {
        this.defaultValue = defaultValue;
    }
    public float getMinValue() {
        return minValue;
    }
    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }
    public float getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }
    public int getOrderIndex() {
        return orderIndex;
    }
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getStringValue() {
        return stringValue;
    }
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    
}
