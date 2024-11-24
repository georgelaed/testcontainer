package com.test;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDateTime;

import static jakarta.persistence.PersistenceConfiguration.*;

@Slf4j
public class ContainerProvider implements AutoCloseable {

    private static final JdbcDatabaseContainer<?> MYSQL = new MySQLContainer<>("mysql:lts");
    private static final JdbcDatabaseContainer<?> JDBC_DATABASE_CONTAINER = MYSQL;

    private static volatile ContainerProvider containerProvider;

    private final JdbcDatabaseContainer<?> dbContainer;
    @Getter private final EntityManagerFactory entityManagerFactory;
    private final PersistenceConfiguration persistenceConfiguration;

    private ContainerProvider(JdbcDatabaseContainer<?> dbContainer) throws InterruptedException {
        this.dbContainer = dbContainer;

        dbContainer.start();
        LocalDateTime start= LocalDateTime.now();
        while(!dbContainer.isRunning() && LocalDateTime.now().isBefore(start.plusSeconds(10))){
            Thread.sleep(2000);
        }

        this.persistenceConfiguration = new PersistenceConfiguration("test");

        persistenceConfiguration.property(JDBC_DRIVER, this.dbContainer.getDriverClassName());
        persistenceConfiguration.property(JDBC_URL, this.dbContainer.getJdbcUrl());
        persistenceConfiguration.property(JDBC_USER, this.dbContainer.getUsername());
        persistenceConfiguration.property(JDBC_PASSWORD, this.dbContainer.getPassword());


        System.out.println("XXX " + this.dbContainer.getDriverClassName());
        System.out.println("XXX " + this.dbContainer.getJdbcUrl());
        System.out.println("XXX " + this.dbContainer.getUsername());
        System.out.println("XXX " + this.dbContainer.getPassword());

        //persistenceConfiguration.managedClass(test.class);

        this.entityManagerFactory = persistenceConfiguration.createEntityManagerFactory();

        this.entityManagerFactory.getSchemaManager().create(true);
    }

    public static ContainerProvider getInstance() throws InterruptedException {
        return getInstance(JDBC_DATABASE_CONTAINER);
    }

    public static ContainerProvider getInstance(JdbcDatabaseContainer<?> dbContainer) throws InterruptedException {
        if (containerProvider == null) {
            synchronized (ContainerProvider.class) {
                if (containerProvider == null) {
                    containerProvider = new ContainerProvider(dbContainer);
                }
            }
        }

        log.debug("Got db helper instance {}!", containerProvider);
        return containerProvider;
    }

    public EntityManagerFactory createNewEntityManagerFactory() {
        return persistenceConfiguration.createEntityManagerFactory();
    }

    @Override
    public void close() throws Exception {
        entityManagerFactory.getSchemaManager().truncate();

        ((AutoCloseable) entityManagerFactory).close();
        ((AutoCloseable) dbContainer).close();

        containerProvider= null;
    }
}
