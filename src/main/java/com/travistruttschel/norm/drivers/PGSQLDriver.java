package com.travistruttschel.norm.drivers;

import com.travistruttschel.norm.DataClient;
import com.travistruttschel.norm.Driver;
import com.travistruttschel.norm.Transaction;
import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.FieldDescriptor;
import com.travistruttschel.norm.entities.FieldValue;
import com.travistruttschel.norm.query.QueryPredicate;
import com.travistruttschel.norm.tracking.ChangeTracker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class PGSQLDriver extends Driver {
    private PGSQLTransaction transaction = null;

    @Override
    public int query(String sql) throws SQLException {
        return 0;
    }

    @Override
    public void refresh() throws SQLException {

    }

    @Override
    public boolean isValid() throws SQLException {
        return false;
    }

    @Override
    public Transaction startTransaction() throws SQLException {
        if (transaction != null) {
            throw new IllegalStateException("Unable to start transaction. Another transaction is already in progress.");
        }

        transaction = new PGSQLTransaction(this);

        transaction.start();

        return transaction;
    }

    @Override
    protected int apply(ChangeTracker<?> changeTracker) throws SQLException {
        return 0;
    }

    @Override
    protected Set<FieldValue> find(EntityDescriptor entity, Object id, Set<FieldDescriptor> fields) throws SQLException {
        return null;
    }

    @Override
    protected Set<Set<FieldValue>> query(EntityDescriptor entity, Set<FieldDescriptor> fields, QueryPredicate<?> predicate) throws SQLException {
        return null;
    }

    @Override
    protected Set<FieldValue> query(String sql, EntityDescriptor entity) throws SQLException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
