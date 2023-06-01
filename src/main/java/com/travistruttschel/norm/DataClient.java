package com.travistruttschel.norm;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.EntityState;
import com.travistruttschel.norm.tracking.ChangeTracker;
import com.travistruttschel.norm.translation.NameTranslator;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DataClient<D extends Driver> implements Closeable {
    private final D driver;
    private final NameTranslator nameTranslator;
    private final HashMap<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<>();
    private final HashMap<Class<?>, DataSet<?>> datasets = new HashMap<>();
    private final Map<Object, ChangeTracker<?>> changeTrackers = new HashMap<>();

    DataClient(DataClientBuilder<D> builder) {
        driver = builder.database;
        nameTranslator = builder.nameTranslator;
        driver.logger = builder.logger;
    }

    @SuppressWarnings("unchecked")
    public <T> DataSet<T> getDataset(Class<T> type) {
        return (DataSet<T>) datasets.computeIfAbsent(type, t -> new DataSet<>(this, getEntityDescriptor(t)));
    }

    public D getDriver() {
        return driver;
    }

    public NameTranslator getNameTranslator() {
        return nameTranslator;
    }

    public int saveChanges() throws SQLException {
        int recordsModified = 0;

        for (Map.Entry<Object, ChangeTracker<?>> entry :
                changeTrackers.entrySet()) {
            recordsModified += driver.apply(entry.getValue());

            entry.getValue().clear();
        }

        return recordsModified;
    }

    @SuppressWarnings("unchecked")
    <T> ChangeTracker<T> getChangeTracker(T instance, EntityState state) {
        ChangeTracker<T> changeTracker = (ChangeTracker<T>) changeTrackers.computeIfAbsent(instance, i ->  new ChangeTracker<>(state, getEntityDescriptor(instance.getClass()), instance));

        if (changeTracker.getState() != state) {
            changeTracker.setState(state);
        }

        return changeTracker;
    }

    public <T> T startTracking(T instance) {
        return startTracking(instance, EntityState.UNMODIFIED);
    }

    public <T> T startTracking(T instance, EntityState entityState) {
        return getChangeTracker(instance, entityState).getChangeProxy();
    }

    public void stopTracking(Object instance) {
        changeTrackers.remove(instance);
    }

    EntityDescriptor getEntityDescriptor(Class<?> type) {
        return entityDescriptors.computeIfAbsent(type, t -> new EntityDescriptor(this, t));
    }

    @Override
    public void close() throws IOException {
        entityDescriptors.clear();
        datasets.clear();
        changeTrackers.clear();
        driver.close();
    }
}
