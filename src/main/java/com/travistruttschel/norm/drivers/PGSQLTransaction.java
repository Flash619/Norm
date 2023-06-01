package com.travistruttschel.norm.drivers;

import com.travistruttschel.norm.Transaction;

import java.sql.SQLException;

public class PGSQLTransaction extends Transaction {
    private final PGSQLDriver driver;

    public PGSQLTransaction(PGSQLDriver driver) {
        this.driver = driver;
    }

    void start() throws SQLException {
        driver.query("start transaction");
    }

    @Override
    protected void executeCommit() throws SQLException {
        driver.query("commit");
    }

    @Override
    protected void executeAbort() throws SQLException {
        driver.query("rollback");
    }
}
