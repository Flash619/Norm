package com.travistruttschel.norm.examples;

import com.travistruttschel.norm.DataClient;
import com.travistruttschel.norm.DataSet;
import com.travistruttschel.norm.DataClientBuilder;
import com.travistruttschel.norm.Transaction;
import com.travistruttschel.norm.drivers.PGSQLDriver;
import com.travistruttschel.norm.query.Operator;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class Examples {
    public void getAllOlderThan25() throws SQLException {
        DataClient<PGSQLDriver> dataClient = new DataClientBuilder<>(new PGSQLDriver()).build();

        DataSet<User> userDataSet = dataClient.getDataset(User.class);

        // Select only the ID and first name of all users with an age over 25.
        Set<User> users = userDataSet.query(u -> {
            u.getId();
            u.getFirstName();
            }, q -> q.field(User::getAge, Operator.GREATER_THAN, 25));
    }

    public void changeFirstName() throws SQLException {
        DataClient<PGSQLDriver> dataClient = new DataClientBuilder<>(new PGSQLDriver()).build();

        DataSet<User> userDataSet = dataClient.getDataset(User.class);

        // Find a user with the ID "12345".
        Optional<User> optUser = userDataSet.find("12345");

        if (optUser.isPresent()) {
            User user = optUser.get();

            user.setFirstName("Travis");
        }

        // Execute any pending updates on the database.
        userDataSet.getClient().saveChanges();
    }

    public void createTravis() throws SQLException {
        DataClient<PGSQLDriver> dataClient = new DataClientBuilder<>(new PGSQLDriver()).build();

        DataSet<User> userDataSet = dataClient.getDataset(User.class);

        User travis = new User("Travis", 31);

        userDataSet.add(travis);

        userDataSet.getClient().saveChanges();
    }

    public void deleteTravis() throws SQLException {
        DataClient<PGSQLDriver> dataClient = new DataClientBuilder<>(new PGSQLDriver()).build();

        DataSet<User> userDataSet = dataClient.getDataset(User.class);

        Set<User> users = userDataSet.query(User::getFirstName, q -> q.field(User::getFirstName, Operator.EQUALS, "Travis"));

        User travis = users.iterator().next();

        if (travis != null) {
            userDataSet.remove(travis);
        }

        userDataSet.getClient().saveChanges();
    }

    public void updateWithoutQuery() throws SQLException {
        DataClient<PGSQLDriver> dataClient = new DataClientBuilder<>(new PGSQLDriver()).build();

        DataSet<User> userDataSet = dataClient.getDataset(User.class);

        // We only need the ID (primary key) to enable change tracking without a query.
        User user = new User("12345", "", 0);

        // Change tracking is based on ID, which we already have.
        user = userDataSet.update(user);

        // Any updates made after change tracking is enabled are still tracked regardless of entity source.
        user.setFirstName("Joseph");

        userDataSet.getClient().saveChanges();
    }

    public void transactions() throws SQLException {
        DataClient<PGSQLDriver> dataClient = new DataClientBuilder<>(new PGSQLDriver()).build();

        DataSet<User> userDataSet = dataClient.getDataset(User.class);

        try (Transaction transaction = userDataSet.getClient().getDriver().startTransaction()) {
            userDataSet.add(new User("Travis", 31));
            userDataSet.add(new User("Joe", 25));

            // Changes saved within a transaction are not persisted until the transaction has been committed.
            userDataSet.getClient().saveChanges();

            Set<User> users = userDataSet.query(u -> {
                u.getId();
                u.getFirstName();
            }, q -> q.field(User::getAge, Operator.GREATER_THAN_EQUALS, 100));

            for (User user :
                    users) {
                user.setFirstName(String.format("(100+) %s", user.getFirstName()));
            }

            // Changes saved within a transaction are not persisted until the transaction has been committed.
            userDataSet.getClient().saveChanges();

            // Transactions must be committed. If try-with-resource exits without commit being called, the transaction
            // will automatically be aborted.
            transaction.commit();
        } catch (Exception e) {
            // An error occurred during the transaction. All changes have been automatically aborted.
        }
    }
}
