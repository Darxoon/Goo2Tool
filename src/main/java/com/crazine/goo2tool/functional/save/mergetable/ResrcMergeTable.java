package com.crazine.goo2tool.functional.save.mergetable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResrcMergeTable extends MergeTable {

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
