package com.travistruttschel.norm.tracking;

import com.travistruttschel.norm.entities.EntityDescriptor;
import com.travistruttschel.norm.entities.EntityState;
import com.travistruttschel.norm.entities.FieldDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChangeTracker<T> implements InvocationHandler {
    private EntityState state;

    private final T instance;
    private final T changeProxy;
    private final EntityDescriptor entity;
    private final HashSet<FieldChange> changes = new HashSet<>();
    private final Map<Method, FieldDescriptor> fieldsByMethod = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ChangeTracker(EntityState state, EntityDescriptor entity, T instance) {
        this.state = state;
        this.instance = instance;
        this.changeProxy =  (T) Proxy.newProxyInstance(instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), this);
        this.entity = entity;

        for (FieldDescriptor field :
                entity.getFields()) {
            fieldsByMethod.put(field.getSetter(), field);
        }
    }

    public T getInstance() {
        return instance;
    }

    public T getChangeProxy() {
        return changeProxy;
    }

    public EntityDescriptor getEntity() {
        return entity;
    }

    public Set<FieldChange> getChanges() {
        return changes;
    }

    public EntityState getState() {
        return state;
    }

    public void setState(EntityState state) {
        this.state = state;
    }

    public void clear() {
        changes.clear();
        state = EntityState.UNMODIFIED;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        FieldDescriptor field = fieldsByMethod.get(method);

        if (field != null) {
            setState(EntityState.UPDATED);

            changes.add(new FieldChange(field, field.getValue(instance), args[0]));
        }

        return method.invoke(instance, args);
    }
}
