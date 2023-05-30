package com.travistruttschel.norm;

import com.travistruttschel.norm.databases.PGSQLDatabase;
import com.travistruttschel.norm.query.Operator;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class Test {
    public void getAllOlderThan25() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        Set<User> users = userDataset.query(u -> {
            u.getId();
            u.getFirstName();
            }, q -> q.field(User::getAge, Operator.GREATER_THAN, 25));
    }

    public void changeFirstName() throws SQLException {
        Norm<PGSQLDatabase> norm = new NormBuilder<>(new PGSQLDatabase()).build();

        Dataset<User> userDataset = norm.getDataset(User.class);

        Optional<User> optUser = userDataset.find(12345);

        if (optUser.isPresent()) {
            User user = optUser.get();

            user.setFirstName("Travis");
        }

        userDataset.saveChanges();
    }
}
