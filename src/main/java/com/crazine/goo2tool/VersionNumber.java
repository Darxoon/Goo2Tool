package com.crazine.goo2tool;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@JsonSerialize(using = ToStringSerializer.class)
@JsonDeserialize(using = VersionNumber.VersionNumberDeserializer.class)
public final class VersionNumber implements Comparable<VersionNumber> {
    
    public static class VersionNumberDeserializer extends FromStringDeserializer<VersionNumber> {
        public VersionNumberDeserializer() {
            super(null);
        }
        protected VersionNumberDeserializer(Class<?> vc) {
            super(vc);
        }
        @Override
        protected VersionNumber _deserialize(String value, DeserializationContext ctxt) throws IOException {
            return VersionNumber.fromString(value);
        }
    }
    
    private int[] elements;
    
    public VersionNumber(int... elements) {
        if (elements.length > 4)
            throw new IllegalArgumentException("Version number cannot have more than 4 elements");
        
        this.elements = elements;
    }
    
    public static VersionNumber fromString(String string) {
        String[] segments = string.split("\\.");
        int[] intSegments = Arrays.stream(segments)
            .mapToInt(segment -> Integer.valueOf(segment))
            .toArray();
        
        return new VersionNumber(intSegments);
    }
    
    public int[] getElements() {
        return elements;
    }
    
    @Override
    public String toString() {
        List<String> stringSegments = Arrays.stream(elements).mapToObj(Integer::toString).toList();
        return String.join(".", stringSegments);
    }

    @Override
    public int compareTo(VersionNumber other) {
        int length = Math.min(elements.length, other.elements.length);
        
        for (int i = 0; i < length; i++) {
            int result = elements[i] - other.elements[i];
            if (result != 0)
                return result;
        }
        
        if (elements.length == other.elements.length)
            return 0;
        
        int[] longer = elements.length > other.elements.length ? elements : other.elements;
        for (int i = length; i < longer.length; i++) {
            if (longer[i] > 0)
                return longer == elements ? longer[i] : -longer[i];
        }
        
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elements);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VersionNumber other = (VersionNumber) obj;
        return Arrays.equals(elements, other.elements);
    }
    
}
