package com.travistruttschel.norm;

import com.travistruttschel.norm.databases.Database;
import com.travistruttschel.norm.entities.*;
import com.travistruttschel.norm.query.QueryPredicate;
import com.travistruttschel.norm.tracking.ChangeTracker;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class Dataset<T> {
    private final Database database;
    private final EntityDescriptor entity;
    private final Map<Object, ChangeTracker<T>> changeTrackers = new HashMap<>();

    public Dataset(Database database, EntityDescriptor entity) {
        this.database = database;
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
    public Set<T> query(Consumer<T> selectorConsumer, Consumer<QueryPredicate<T>> queryConsumer) throws SQLException {
        FieldSelector<T> fieldSelector = new FieldSelector<>(entity);
        QueryPredicate<T> queryPredicate = new QueryPredicate<>(entity);

        selectorConsumer.accept(fieldSelector.getProxy());
        queryConsumer.accept(queryPredicate);

        Set<Set<FieldValue>> values = database.query(entity, fieldSelector.getFields(), queryPredicate);
        Set<T> instances = new HashSet<>();

        for (Set<FieldValue> values0 :
                values) {
            T instance = (T) entity.getInstance();

            for (FieldValue value :
                    values0) {
                value.getField().setValue(instance, value.getValue());
            }

            instances.add(instance);
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
            recordsModified += database.apply(entry.getValue());

            entry.getValue().clear();
        }

        return recordsModified;
    }

    public Database getDatabase() {
        return database;
    }

    @SuppressWarnings("unchecked")
    private Optional<T> find(Object id, Set<FieldDescriptor> fields) throws SQLException {
        FieldDescriptor pKeyField = entity.getPrimaryKeyField();

        if (!pKeyField.getType().isAssignableFrom(id.getClass())) {
            throw new IllegalArgumentException("The primary key value provided is not compatible with the primary key for the entity.");
        }

        Set<FieldValue> values = database.find(entity, id, fields);

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
