// https://github.com/The-Java-Druid/yamlrecords
// GPL-3.0
package org.yaml.snakeyaml.yamlrecords;

import java.util.Map;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertySubstitute;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

public class RecordConstructor extends Constructor {
    
    public static class RecordSubstitute extends PropertySubstitute {

        private final String componentName;
        
        public RecordSubstitute(String name, Class<?> type, String componentName, Class<?>... params) {
            super(name, type, params);
            
            this.componentName = componentName;
        }
        
        public String componentName() {
            return componentName;
        }
        
    }
    
    public RecordConstructor(Class<? extends Record> rootType) {
        super(rootType, new LoaderOptions());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object constructObject(Node node) {
        final Class<?> targetType = typeTags.get(node.getTag());
        if (RecordUtils.isRecord(targetType) && node instanceof MappingNode mappingNode) {
            return constructRecord((Class<? extends Record>) targetType, mappingNode);
        }
        return super.constructObject(node);
    }

    private Object constructRecord(Class<?extends Record> recordClass, MappingNode node) {
        TypeDescription memberDescription = typeDefinitions.get(recordClass);
        
        final Map<String, Object> values = node.getValue().stream()
            .collect(Collectors.toMap(tuple -> getKey(memberDescription, tuple), this::getValue));
        return RecordUtils.instantiateRecord(typeDefinitions, recordClass, values);
    }

    private String getKey(TypeDescription memberDescription, NodeTuple tuple) {
        final Object key = constructObject(tuple.getKeyNode());
        if (!(key instanceof String)) {
            throw new YAMLException("Record keys must be strings: " + key);
        }
        
        if (memberDescription != null) {
            Property property = memberDescription.getProperty((String) key);
            
            // Probably from addPropertyParameters and not substituteProperty
            if (property.getType() == null)
                return (String) key;
            
            if (!(property instanceof RecordSubstitute recordSubstitute))
                throw new IllegalArgumentException("PropertySubstitute for field '" + key + "'' is not of type RecordSubstitute");
            
            return recordSubstitute.componentName();
        } else {
            return (String) key;
        }
    }

    private Object getValue(NodeTuple tuple) {
        return constructObject(tuple.getValueNode());
    }

}
