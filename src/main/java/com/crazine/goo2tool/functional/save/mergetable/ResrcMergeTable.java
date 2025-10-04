package com.crazine.goo2tool.functional.save.mergetable;

import com.crazine.goo2tool.gamefiles.resrc.Resrc;
import com.crazine.goo2tool.gamefiles.resrc.Resrc.SetDefaults;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ResrcMergeTable extends MergeTable<ResrcMergeTable.ResrcValue> {

    @JsonDeserialize(using = MergeTableLoader.ResrcValueDeserializer.class)
    @JsonSerialize(using = MergeTableLoader.ResrcValueSerializer.class)
    public static class ResrcValue {
        
        private Resrc.SetDefaults setDefaults;
        private Resrc value;
        
        public ResrcValue(SetDefaults setDefaults, Resrc value) {
            this.setDefaults = setDefaults;
            this.value = value;
        }
        
        public Resrc.SetDefaults getSetDefaults() {
            return setDefaults;
        }
        public Resrc getValue() {
            return value;
        }
        
    }
    
    @JsonCreator
    private ResrcMergeTable(@JsonProperty("type") MergeType type) {
        if (type != MergeType.RESOURCE_XML)
            throw new IllegalArgumentException("ResrcMergeTable has to be of type resource_xml, not " + type);
    }
    
    public ResrcMergeTable() {}
    
    @Override
    public MergeType getType() {
        return MergeType.RESOURCE_XML;
    }
    
}
