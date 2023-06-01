package com.travistruttschel.norm;

import com.travistruttschel.norm.entities.*;
import com.travistruttschel.norm.query.QueryPredicate;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class DataSet<T> {
    private final DataClient<?> client;
    private final Driver driver;
    private final EntityDescriptor entity;

    public DataSet(DataClient<?> client, EntityDescriptor entity) {
        this.client = client;
        this.driver = client.getDriver();
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

            client.startTracking(instance);
        }

        return instances;
    }

    public T add(T instance) {
        return client.getChangeTracker(instance, EntityState.CREATED).getChangeProxy();
    }

    public T update(T instance) {
        return client.getChangeTracker(instance, EntityState.UPDATED).getChangeProxy();
    }

    public void delete(T instance) {
        client.getChangeTracker(instance, EntityState.DELETED);
    }

    public DataClient<?> getClient() {
        return client;
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

        return Optional.of(client.startTracking(instance));
    }
}
