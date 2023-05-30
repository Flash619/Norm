package com.travistruttschel.norm.tracking;

import com.travistruttschel.norm.entities.FieldDescriptor;

import java.util.Objects;

public class FieldChange {
    private final FieldDescriptor field;
    private final Object oldValue;
    private final Object newValue;

    FieldChange(FieldDescriptor field, Object oldValue, Object newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldChange that = (FieldChange) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }
}
