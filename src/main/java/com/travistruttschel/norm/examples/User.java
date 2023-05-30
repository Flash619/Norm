package com.travistruttschel.norm.examples;

import java.util.UUID;

public class User {
    private String id = UUID.randomUUID().toString();
    private String firstName;
    private int age;

    public User(String id, String firstName, int age) {
        this.id = id;
        this.firstName = firstName;
        this.age = age;
    }

    public User(String firstName, int age) {
        this.firstName = firstName;
        this.age = age;
    }

    private User() {
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getAge() {
        return age;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
