package com.travistruttschel.norm;

import com.travistruttschel.norm.databases.Database;
import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.translation.NameTranslator;

import java.util.HashMap;

public class Norm<D extends Database> {
    private final D database;
    private final NameTranslator nameTranslator;
    private final HashMap<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<>();
    private final HashMap<Class<?>, Dataset<?>> datasets = new HashMap<>();

    Norm(NormBuilder<D> builder) {
        database = builder.database;
        nameTranslator = builder.nameTranslator;
    }

    @SuppressWarnings("unchecked")
    public <T> Dataset<T> getDataset(Class<T> type) {
        return (Dataset<T>) datasets.computeIfAbsent(type, t -> new Dataset<>(database, getEntityDescriptor(t)));
    }

    public D getDatabase() {
        return database;
    }

    public NameTranslator getNameTranslator() {
        return nameTranslator;
    }

    EntityDescriptor getEntityDescriptor(Class<?> type) {
        return entityDescriptors.computeIfAbsent(type, t -> new EntityDescriptor(this, t));
    }
}
