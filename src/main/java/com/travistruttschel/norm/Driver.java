package com.travistruttschel.norm;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.FieldDescriptor;
import com.travistruttschel.norm.entities.FieldValue;
import com.travistruttschel.norm.query.QueryPredicate;
import com.travistruttschel.norm.tracking.ChangeTracker;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Driver implements Closeable {
    Consumer<String> logger = null;

    protected void log(String message) {
        if (logger != null) {
            logger.accept(message);
        }
    }

    public abstract int query(String sql) throws SQLException;
    public abstract void refresh() throws SQLException;
    public abstract boolean isValid() throws SQLException;
    public abstract Transaction startTransaction() throws SQLException;

    protected abstract int apply(ChangeTracker<?> changeTracker) throws SQLException;
    protected abstract Set<FieldValue> find(EntityDescriptor entity, Object id, Set<FieldDescriptor> fields) throws SQLException;
    protected abstract Set<Set<FieldValue>> query(EntityDescriptor entity, Set<FieldDescriptor> fields, QueryPredicate<?> predicate) throws SQLException;
    protected abstract Set<FieldValue> query(String sql, EntityDescriptor entity) throws SQLException;
}
