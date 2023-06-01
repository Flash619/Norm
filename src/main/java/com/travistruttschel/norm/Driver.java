package com.travistruttschel.norm.drivers;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.FieldDescriptor;
import com.travistruttschel.norm.entities.FieldValue;
import com.travistruttschel.norm.query.QueryPredicate;
import com.travistruttschel.norm.tracking.ChangeTracker;

import java.sql.SQLException;
import java.util.Set;

public interface Driver {
    int apply(ChangeTracker<?> changeTracker) throws SQLException;
    Set<FieldValue> find(EntityDescriptor entity, Object id, Set<FieldDescriptor> fields);
    Set<Set<FieldValue>> query(EntityDescriptor entity, Set<FieldDescriptor> fields, QueryPredicate<?> predicate);
    int execute(String sql);
}
