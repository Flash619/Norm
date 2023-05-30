package com.travistruttschel.norm.examples;

import com.travistruttschel.norm.Dataset;
import com.travistruttschel.norm.Norm;
import com.travistruttschel.norm.NormBuilder;
import com.travistruttschel.norm.databases.PGSQLDatabase;
import com.travistruttschel.norm.query.Operator;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class Examples {
    public void getAllOlderThan25() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        // Select only the ID and first name of all users with an age over 25.
        Set<User> users = userDataset.query(u -> {
            u.getId();
            u.getFirstName();
            }, q -> q.field(User::getAge, Operator.GREATER_THAN, 25));
    }

    public void changeFirstName() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        // Find a user with the ID "12345".
        Optional<User> optUser = userDataset.find("12345");

        if (optUser.isPresent()) {
            User user = optUser.get();

            user.setFirstName("Travis");
        }

        // Execute any pending updates on the database.
        userDataset.saveChanges();
    }

    public void createTravis() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        User travis = new User("Travis", 31);

        userDataset.add(travis);

        userDataset.saveChanges();
    }

    public void deleteTravis() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        Set<User> users = userDataset.query(User::getFirstName, q -> q.field(User::getFirstName, Operator.EQUALS, "Travis"));

        User travis = users.iterator().next();

        if (travis != null) {
            userDataset.delete(travis);
        }

        userDataset.saveChanges();
    }

    public void updateWithoutQuery() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        // We only need the ID (primary key) to enable change tracking without a query.
        User user = new User("12345", "", 0);

        // Change tracking is based on ID, which we already have.
        user = userDataset.update(user);

        // Any updates made after change tracking is enabled are still tracked regardless of entity source.
        user.setFirstName("Joseph");

        userDataset.saveChanges();
    }
}
