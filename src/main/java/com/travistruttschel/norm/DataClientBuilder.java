package com.travistruttschel.norm;

import com.travistruttschel.norm.translation.NameTranslator;
import com.travistruttschel.norm.translation.SnakeCaseNameTranslator;

import java.util.function.Consumer;

public class DataClientBuilder<D extends Driver> {
    final D database;
    NameTranslator nameTranslator = new SnakeCaseNameTranslator();
    Consumer<String> logger = null;

    public DataClientBuilder(D driver) {
        this.database = driver;
    }

    public DataClientBuilder<D> setNameTranslator(NameTranslator nameTranslator) {
        this.nameTranslator = nameTranslator;

        return this;
    }

    public DataClientBuilder<D> setLogger(Consumer<String> consumer) {
        this.logger = consumer;

         return this;
    }

    public DataClient<D> build() {
        return new DataClient<>(this);
    }
}
