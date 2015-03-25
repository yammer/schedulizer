package com.yammer.schedulizer.entities;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonAnnotatedEntity {

    private Map<String, Object> annotations = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotationProperty(String name, Object value) {
        annotations.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAnnotationProperty(String name) {
        return (T) annotations.get(name);
    }
}
