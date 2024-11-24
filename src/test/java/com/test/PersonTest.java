package com.test;


import jakarta.persistence.SchemaValidationException;
import org.junit.jupiter.api.Test;


class PersonTest {

    @Test
    void test() throws InterruptedException, SchemaValidationException {
        ContainerProvider databaseProvider = ContainerProvider.getInstance();



        System.out.println("xxxx " + new Person("Me"));
    }

}
