package com.travistruttschel.norm.query;

import com.travistruttschel.norm.entities.FieldDescriptor;

public class QueryPredicateField {
    private final FieldDescriptor field;
    private final Operator operator;
    private final Object value;

    public QueryPredicateField(FieldDescriptor field, Operator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public FieldDescriptor getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
