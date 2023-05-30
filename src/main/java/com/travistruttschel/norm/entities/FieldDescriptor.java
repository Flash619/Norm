package com.travistruttschel.norm.entities;

import com.travistruttschel.norm.Column;
import com.travistruttschel.norm.Norm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class FieldDescriptor {
    private final Method setter;
    private final Method getter;
    private final Field field;
    private final String name;
    private final String column;
    private final Class<?> type;
    private final boolean isPrimaryKey;

    FieldDescriptor(EntityDescriptor entity, Method setter, Method getter, Field field, String column) {
        if (getter == null) {
            throw new IllegalArgumentException("Getter cannot be null.");
        }

        if (setter == null && field == null) {
            throw new IllegalArgumentException("Setter or backing field must be present.");
        }

        this.setter = setter;
        this.getter = getter;
        this.field = field;

        if (getter.getName().startsWith("get")) {
            String tmp = getter.getName().replaceFirst("get", "");
            tmp = String.format("%s%s", tmp.substring(0, 1).toLowerCase(), tmp.substring(1));

            this.name = tmp;
        } else {
            this.name = getter.getName();
        }

        this.column = column;
        this.type = field != null ? field.getType() : setter.getParameterTypes()[0];
        this.isPrimaryKey = name.equals("id") || name.equals(String.format("%sId", entity.getType().getTypeName()));
    }

    public Optional<Object> getValue(Object instance) throws SQLException {
        try {
            if (getter != null) {
                getter.setAccessible(true);

                return Optional.ofNullable(getter.invoke(instance));
            }

            field.setAccessible(true);

            return Optional.ofNullable(field.get(instance));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SQLException("Failed to get field value. A reflection error occurred.", e);
        }
    }

    public void setValue(Object value, Object instance) throws SQLException {
        try {
            if (setter != null) {
                setter.setAccessible(true);

                setter.invoke(instance, value);
            } else {
                field.setAccessible(true);

                field.set(instance, value);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SQLException("Failed to assign field value. A reflection error occurred.", e);
        }
    }

    public Method getSetter() {
        return setter;
    }

    public Method getGetter() {
        return getter;
    }

    public String getColumn() {
        return column;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldDescriptor that = (FieldDescriptor) o;
        return setter.equals(that.setter) && getter.equals(that.getter) && Objects.equals(field, that.field) && column.equals(that.column) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(setter, getter, field, column, type);
    }

    public static Set<FieldDescriptor> findAll(Norm<?> norm, EntityDescriptor entity) {
        HashSet<FieldDescriptor> descriptors = new HashSet<>();

        for (Method method :
                entity.getType().getMethods()) {
            Column columnAttr = method.getAnnotation(Column.class);
            Method getter = null;
            Method setter = null;
            String name = method.getName();
            String column = null;
            String setterPostfix = null;

            if (name.startsWith("get") || name.startsWith("is") || columnAttr != null) {
                if (columnAttr != null) {
                    column = columnAttr.value();
                }

                getter = method;
                setterPostfix = name;

                if (setterPostfix.startsWith("get")) {
                    setterPostfix = setterPostfix.replaceFirst("get", "");
                    setterPostfix = String.format("%s%s", setterPostfix.substring(0, 1).toLowerCase(), setterPostfix.substring(1));
                }

                if (column == null) {
                    // We've already trimmed the setter postfix of any weird "get" prefix. Just send it through the translator.
                    column = norm.getNameTranslator().to(setterPostfix);
                }

                for (Method method0 :
                        entity.getType().getDeclaredMethods()) {
                    name = method0.getName();

                    if (name.endsWith(setterPostfix)) {
                        setter = method0;

                        break;
                    }
                }
            }

            if (setter != null) {
                descriptors.add(new FieldDescriptor(entity, setter, getter, null, column));

                continue;
            } else if (getter != null) {
                try {
                    Field field = entity.getType().getField(setterPostfix);

                    descriptors.add(new FieldDescriptor(entity, setter, getter, field, column));
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("Failed to locate matching backing field for getter. A reflection error occurred.", e);
                }
            }
        }

        return descriptors;
    }
}
