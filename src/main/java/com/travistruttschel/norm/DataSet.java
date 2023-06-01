package com.travistruttschel.norm;

import com.travistruttschel.norm.entities.*;
import com.travistruttschel.norm.query.QueryPredicate;
import com.travistruttschel.norm.tracking.ChangeTracker;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class DataSet<T> {
    private final Driver driver;
    private final EntityDescriptor entity;
    private final Map<Object, ChangeTracker<T>> changeTrackers = new HashMap<>();

    public DataSet(Driver driver, EntityDescriptor entity) {
        this.driver = driver;
        this.entity = entity;
    }

    public Optional<T> find(Object id) throws SQLException {
        return find(id, entity.getFields());
    }

    public Optional<T> find(Object id, Consumer<T> fieldSelector) throws SQLException {
        FieldSelector<T> fieldIdentificationProxy = new FieldSelector<>(entity);

        fieldSelector.accept(fieldIdentificationProxy.getProxy());

        return find(id, fieldIdentificationProxy.getFields());
    }

    @SuppressWarnings("unchecked")
    public T query(String sql) throws SQLException {
        Set<FieldValue> values = driver.query(sql, entity);
        T instance = (T) entity.getInstance();

        for (FieldValue value :
                values) {
            value.getField().setValue(value.getValue(), instance);
        }

        return instance;
    }


    @SuppressWarnings("unchecked")
    public Set<T> query(Consumer<T> selectorConsumer, Consumer<QueryPredicate<T>> queryConsumer) throws SQLException {
        FieldSelector<T> fieldSelector = new FieldSelector<>(entity);
        Set<FieldDescriptor> selectedFields = fieldSelector.getFields();
        QueryPredicate<T> queryPredicate = new QueryPredicate<>(entity);

        selectorConsumer.accept(fieldSelector.getProxy());
        queryConsumer.accept(queryPredicate);

        // Always select the primary key, otherwise we have no change tracking capabilities.
        selectedFields.add(entity.getPrimaryKeyField());

        Set<Set<FieldValue>> values = driver.query(entity, selectedFields, queryPredicate);

        Set<T> instances = new HashSet<>();

        for (Set<FieldValue> values0 :
                values) {
            T instance = (T) entity.getInstance();

            for (FieldValue value :
                    values0) {
                value.getField().setValue(instance, value.getValue());
            }

            instances.add(instance);

            startTracking(instance);
        }

        return instances;
    }

    public T add(T instance) {
        return getChangeTracker(instance, EntityState.CREATED).getChangeProxy();
    }

    public T update(T instance) {
        return getChangeTracker(instance, EntityState.UPDATED).getChangeProxy();
    }

    public void delete(T instance) {
        getChangeTracker(instance, EntityState.DELETED);
    }

    public T startTracking(T instance) {
        return getChangeTracker(instance, EntityState.UNMODIFIED).getChangeProxy();
    }

    public void stopTracking(T instance) {
        changeTrackers.remove(instance);
    }

    public int saveChanges() throws SQLException {
        int recordsModified = 0;

        for (Map.Entry<Object, ChangeTracker<T>> entry :
                changeTrackers.entrySet()) {
            recordsModified += driver.apply(entry.getValue());

            entry.getValue().clear();
        }

        return recordsModified;
    }

    public Driver getDriver() {
        return driver;
    }

    @SuppressWarnings("unchecked")
    private Optional<T> find(Object id, Set<FieldDescriptor> fields) throws SQLException {
        FieldDescriptor pKeyField = entity.getPrimaryKeyField();
        Set<FieldDescriptor> selectedFields = new HashSet<>(fields);

        // Always select the primary key, otherwise we have no change tracking capabilities.
        selectedFields.add(entity.getPrimaryKeyField());

        if (!pKeyField.getType().isAssignableFrom(id.getClass())) {
            throw new IllegalArgumentException("The primary key value provided is not compatible with the primary key for the entity.");
        }

        Set<FieldValue> values = driver.find(entity, id, selectedFields);

        if (values.isEmpty()) {
            return Optional.empty();
        }

        T instance = (T) entity.getInstance();

        for (FieldValue value :
                values) {
            value.getField().setValue(id, value.getValue());
        }

        return Optional.of(startTracking(instance));
    }

    private ChangeTracker<T> getChangeTracker(T instance, EntityState state) {
        ChangeTracker<T> changeTracker = changeTrackers.computeIfAbsent(instance, i ->  new ChangeTracker<>(state, entity, instance));

        if (changeTracker.getState() != state) {
            changeTracker.setState(state);
        }

        return changeTracker;
    }
}
