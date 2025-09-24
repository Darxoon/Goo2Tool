// https://github.com/The-Java-Druid/yamlrecords
// GPL-3.0
package org.yaml.snakeyaml.yamlrecords;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.yamlrecords.RecordConstructor.RecordSubstitute;

public class RecordUtils {

    public static <T extends Record> T instantiateRecord(
            Map<Class<? extends Object>, TypeDescription> typeDefinitions, Class<T> recordClass,
            Map<String, Object> values) {
        if (!isRecord(recordClass)) {
            throw new IllegalArgumentException(recordClass + " is not a record");
        }

        try {
            final RecordComponent[] components = recordClass.getRecordComponents();

            final Class<?>[] paramTypes = Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class[]::new);

            final Object[] args = Arrays.stream(components)
                .map(rc -> convertValue(typeDefinitions, rc.getType(), values.get(rc.getName()), rc.getGenericType()))
                .toArray(Object[]::new );

            return recordClass.getDeclaredConstructor(paramTypes).newInstance(args);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate record " + recordClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(
            Map<Class<? extends Object>, TypeDescription> typeDefinitions, Class<?> type,
            Object value, Type genericType) {
        if (value == null && type.isPrimitive()) {
            throw new IllegalArgumentException("Missing value for primitive field of type " + type.getName());
        }

        if (isOptional(type)) {
            Type wrappedType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            Class<?> wrappedClass = (Class<?>) wrappedType;
            Object inner = (value == null) ? null : convertValue(typeDefinitions, wrappedClass, value, wrappedType);
            return Optional.ofNullable(inner);
        }

        if (isRecord(type) && value instanceof Map<?, ?> mapValue) {
            TypeDescription memberDescription = typeDefinitions.get(type);
            
            Map<String, Object> transformedValues = mapValue.entrySet().stream()
                .collect(Collectors.toMap(entry -> {
                    return getComponentName(memberDescription, (String) entry.getKey());
                }, entry -> entry.getValue()));
            
            return instantiateRecord(typeDefinitions, (Class<? extends Record>)type, transformedValues);
        }

        if ((List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) && value instanceof Collection<?> collection) {
            Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            Class<?> elementClass = (Class<?>) elementType;

            final Collection<Object> result = type == List.class ? new ArrayList<>() : new HashSet<>();
            collection.stream()
                .map(item -> convertValue(typeDefinitions, elementClass, item, elementType))
                .forEach(result::add);
            return result;
        }

        if (Map.class.isAssignableFrom(type) && value instanceof Map<?, ?> mapValue) {
            final Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
            final Class<?> keyType = (Class<?>) typeArgs[0];
            final Class<?> valType = (Class<?>) typeArgs[1];

            return mapValue.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> convertSimple(keyType, entry.getKey()),
                    entry -> convertValue(typeDefinitions, valType, entry.getValue(), valType)));
        }

        return convertSimple(type, value);
    }
    
    private static String getComponentName(TypeDescription memberDescription, String key) {
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

    private static boolean isOptional(Class<?> clazz) {
        return clazz == Optional.class;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object convertSimple(Class<?> targetType, Object value) {
        if (value == null) return null;

        if (targetType.isInstance(value)) return value;

        if (targetType == String.class) return value.toString();

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number n) return n.intValue();
            if (value instanceof String s) return Integer.valueOf(s);
        }

        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number n) return n.longValue();
            if (value instanceof String s) return Long.valueOf(s);
        }

        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number n) return n.doubleValue();
            if (value instanceof String s) return Double.valueOf(s);
        }

        if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number n) return n.floatValue();
            if (value instanceof String s) return Float.valueOf(s);
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean b) return b;
            if (value instanceof String s) return Boolean.valueOf(s);
        }

        if (targetType.isEnum()) {
            if (value instanceof String s) {
                return Enum.valueOf((Class<Enum>) targetType, s);
            }
        }
        throw new IllegalArgumentException("Cannot convert value: " + value + " to type " + targetType.getName());
    }

    public static boolean isRecord(final Class<?> targetType) {
        // Ideally we should use Class.isRecord() here. However, Android desugaring always return false.
        return targetType != null && Record.class.isAssignableFrom(targetType);
    }
}
