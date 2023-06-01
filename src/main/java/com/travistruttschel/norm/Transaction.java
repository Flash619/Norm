package com.travistruttschel.norm;

import java.sql.SQLException;

public abstract class Transaction implements AutoCloseable {
    private boolean committed = false;
    private boolean aborted = false;

    public void commit() throws SQLException {
        if (committed) {
            return;
        }

        if (aborted) {
            throw new IllegalStateException("Unable to commit transaction. The transaction has been aborted.");
        }

        executeCommit();

        committed = true;
    }

    public void abort() throws SQLException {
        if (aborted) {
            return;
        }

        if (committed) {
            throw new IllegalStateException("Unable to abort transaction. The transaction has been committed.");
        }

        executeAbort();

        aborted = true;
    }

    protected abstract void executeCommit() throws SQLException;
    protected abstract void executeAbort() throws SQLException;

    @Override
    public void close() throws Exception {
        if (!aborted && !committed) {
            abort();
        }
    }
}
