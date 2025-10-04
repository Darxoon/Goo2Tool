package com.crazine.goo2tool.functional.save.mergetable;

import com.crazine.goo2tool.gamefiles.translation.GameString;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslationMergeTable extends MergeTable<GameString> {
    
    @JsonCreator
    private TranslationMergeTable(@JsonProperty("type") MergeType type) {
        if (type != MergeType.TRANSLATION_XML)
            throw new IllegalArgumentException("TranslationMergeTable has to be of type resource_xml, not " + type);
    }
    
    public TranslationMergeTable() {}
    
    @Override
    public MergeType getType() {
        return MergeType.TRANSLATION_XML;
    }
    
}
