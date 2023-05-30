package com.travistruttschel.norm.entities;

import com.travistruttschel.norm.Norm;
import com.travistruttschel.norm.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EntityDescriptor {
    private final Class<?> type;
    private final FieldDescriptor primaryKeyField;
    private final Set<FieldDescriptor> fields;
    private final Map<Method, FieldDescriptor> fieldsByMethod = new HashMap<>();
    private final String schema;
    private final String table;

    public EntityDescriptor(Norm<?> norm, Class<?> type) {
        this.type = type;
        this.fields = FieldDescriptor.findAll(norm, this);

        FieldDescriptor pKeyField = null;

        for (FieldDescriptor field :
                fields) {
            fieldsByMethod.put(field.getGetter(), field);

            if (field.isPrimaryKey() && pKeyField != null) {
                throw new IllegalArgumentException("Unable to prepare entity. Entity can only contain one primary key.");
            } else if (field.isPrimaryKey()) {
                pKeyField = field;
            }
        }

        if (pKeyField == null) {
            throw new IllegalArgumentException("Unable to prepare entity. Entity must contain a primary key.");
        }

        this.primaryKeyField = pKeyField;

        Table tableAttr = type.getAnnotation(Table.class);

        if (tableAttr != null){
            schema = tableAttr.schema();
            table = tableAttr.value();
        } else {
            schema = "";
            table = norm.getNameTranslator().to(type.getTypeName());
        }
    }

    public FieldDescriptor getPrimaryKeyField() {
        return primaryKeyField;
    }

    public Set<FieldDescriptor> getFields() {
        return fields;
    }

    public Optional<FieldDescriptor> getField(Method method) {
        return Optional.ofNullable(fieldsByMethod.get(method));
    }

    public Class<?> getType() {
        return type;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public Object getInstance() throws SQLException {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();

            constructor.setAccessible(true);

            return constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new SQLException("Unable to create entity instance. A reflection error occurred.", e);
        }
    }
}
