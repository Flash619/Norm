package com.travistruttschel.norm;

import com.travistruttschel.norm.databases.Database;
import com.travistruttschel.norm.translation.NameTranslator;
import com.travistruttschel.norm.translation.SnakeCaseNameTranslator;

public class NormBuilder<D extends Database> {
    final D database;
    NameTranslator nameTranslator = new SnakeCaseNameTranslator();

    public NormBuilder(D database) {
        this.database = database;
    }

    public NormBuilder<D> setNameTranslator(NameTranslator nameTranslator) {
        this.nameTranslator = nameTranslator;

        return this;
    }

    public Norm<D> build() {
        return new Norm<>(this);
    }
}
