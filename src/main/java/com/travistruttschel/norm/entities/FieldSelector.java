package com.travistruttschel.norm.entities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

public class FieldSelector<T> implements InvocationHandler {
    private final T proxy;
    private final Set<FieldDescriptor> fields = new HashSet<>();
    private final EntityDescriptor entity;

    @SuppressWarnings("unchecked")
    public FieldSelector(EntityDescriptor entity) {
        this.proxy = (T) Proxy.newProxyInstance(entity.getType().getClassLoader(), entity.getType().getInterfaces(), this);
        this.entity = entity;
    }

    public T getProxy() {
        return proxy;
    }

    public Set<FieldDescriptor> getFields() {
        return fields;
    }

    public FieldDescriptor requireOne() {
        if (fields.size() > 1) {
            throw new IllegalArgumentException("Unable to determine selected field. Only one field should be selected.");
        }

        if (fields.size() < 1) {
            throw new IllegalArgumentException("Unable to determine selected field. One field should be selected");
        }

        return fields.iterator().next();
    }

    public void reset() {
        fields.clear();
    }

    @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        entity.getField(method).ifPresent(fields::add);

        return null;
    }
}
