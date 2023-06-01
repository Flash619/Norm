package com.travistruttschel.norm;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.translation.NameTranslator;

import java.util.HashMap;

public class DataClient<D extends Driver> {
    private final D driver;
    private final NameTranslator nameTranslator;
    private final HashMap<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<>();
    private final HashMap<Class<?>, DataSet<?>> datasets = new HashMap<>();

    DataClient(DataClientBuilder<D> builder) {
        driver = builder.database;
        nameTranslator = builder.nameTranslator;
        driver.logger = builder.logger;
    }

    @SuppressWarnings("unchecked")
    public <T> DataSet<T> getDataset(Class<T> type) {
        return (DataSet<T>) datasets.computeIfAbsent(type, t -> new DataSet<>(driver, getEntityDescriptor(t)));
    }

    public D getDriver() {
        return driver;
    }

    public NameTranslator getNameTranslator() {
        return nameTranslator;
    }

    EntityDescriptor getEntityDescriptor(Class<?> type) {
        return entityDescriptors.computeIfAbsent(type, t -> new EntityDescriptor(this, t));
    }
}
