package com.travistruttschel.norm.databases;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.FieldDescriptor;
import com.travistruttschel.norm.entities.FieldValue;
import com.travistruttschel.norm.query.QueryPredicate;
import com.travistruttschel.norm.tracking.ChangeTracker;

import java.sql.SQLException;
import java.util.Set;

public class PGSQLDatabase implements Database {
    @Override
    public int apply(ChangeTracker<?> changeTracker) throws SQLException {
        return 0;
    }

    @Override
    public Set<FieldValue> find(EntityDescriptor entity, Object id, Set<FieldDescriptor> fields) {
        return null;
    }

    @Override
    public Set<Set<FieldValue>> query(EntityDescriptor entity, Set<FieldDescriptor> fields, QueryPredicate<?> predicate) {
        return null;
    }

    @Override
    public int execute(String sql) {
        return 0;
    }
}
