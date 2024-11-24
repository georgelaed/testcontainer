package com.test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;


@Entity
public class Person {

    @Id
    @GeneratedValue
    private UUID id;

    @Getter
    String name;


    public Person() {}

    public Person(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
