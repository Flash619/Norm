package com.travistruttschel.norm.entities;

public class FieldValue {
    private final FieldDescriptor field;
    private Object value;

    public FieldValue(FieldDescriptor field, Object value) {
        this.field = field;
        this.value = value;
    }

    public FieldDescriptor getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
