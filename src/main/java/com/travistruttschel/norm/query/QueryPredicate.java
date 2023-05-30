package com.travistruttschel.norm.query;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.FieldSelector;

import java.util.*;
import java.util.function.Consumer;

public class QueryPredicate<T> {
    private final Set<QueryPredicate<T>> groups = new HashSet<>();
    private final Set<QueryPredicateField> fields = new HashSet<>();
    private final FieldSelector<T> fieldSelector;
    private Mode mode = Mode.AND;

    public QueryPredicate(EntityDescriptor entity) {
        this.fieldSelector = new FieldSelector<>(entity);
    }

    public QueryPredicate(FieldSelector<T> fieldSelector) {
        this.fieldSelector = fieldSelector;
    }

    public QueryPredicate<T> field(Consumer<T> consumer, Operator operator, Object value) {
        consumer.accept(fieldSelector.getProxy());

        fields.add(new QueryPredicateField(fieldSelector.requireOne(), operator, value));

        fieldSelector.reset();

        return this;
    }

    public QueryPredicate<T> group(Consumer<QueryPredicate<T>> consumer) {
        QueryPredicate<T> group = new QueryPredicate<>(fieldSelector);

        consumer.accept(group);

        groups.add(group);

        return this;
    }

    public QueryPredicate<T> setMode(Mode mode) {
        this.mode = mode;

        return this;
    }

    public Set<QueryPredicate<T>> getGroups() {
        return groups;
    }

    public Set<QueryPredicateField> getFields() {
        return fields;
    }

    public Mode getMode() {
        return mode;
    }
}
