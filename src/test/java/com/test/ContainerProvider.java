package com.test;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;

import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.SchemaManager;
import jakarta.persistence.SchemaValidationException;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;

import lombok.Getter;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static jakarta.persistence.PersistenceConfiguration.JDBC_DRIVER;
import static jakarta.persistence.PersistenceConfiguration.JDBC_PASSWORD;
import static jakarta.persistence.PersistenceConfiguration.JDBC_URL;
import static jakarta.persistence.PersistenceConfiguration.JDBC_USER;

public class ContainerProvider implements AutoCloseable {

    private static final Logger log = Logger.getLogger(ContainerProvider.class.getName());

    private static final JdbcDatabaseContainer<?> MYSQL = new MySQLContainer<>("mysql:lts");
    private static final JdbcDatabaseContainer<?> JDBC_DATABASE_CONTAINER = MYSQL;

    private static volatile ContainerProvider containerProvider;

    private final JdbcDatabaseContainer<?> dbContainer;
    private final PersistenceConfiguration persistenceConfiguration;

    @Getter private EntityManagerFactory   currentEntityManagerFactory;
    private         SchemaManager          currentEMFschemaManager;

    private List<EntityManagerFactory> entityManagerFactories = new ArrayList<>();

    public static ContainerProvider getInstance() throws InterruptedException, SchemaValidationException {
        return getInstance(JDBC_DATABASE_CONTAINER);
    }

    public static ContainerProvider getInstance(JdbcDatabaseContainer<?> dbContainer) throws InterruptedException, SchemaValidationException {
        if (containerProvider == null) {
            synchronized (ContainerProvider.class) {
                if (containerProvider == null) {
                    containerProvider = new ContainerProvider(dbContainer);
                }
            }
        }

        log.info("Got db helper instance %s!" + containerProvider);
        return containerProvider;
    }

    private ContainerProvider(JdbcDatabaseContainer<?> dbContainer) throws InterruptedException, SchemaValidationException {
        this.dbContainer = dbContainer;

        dbContainer.start();
        LocalDateTime start= LocalDateTime.now();
        while(!dbContainer.isRunning() && LocalDateTime.now().isBefore(start.plusSeconds(10))){
            Thread.sleep(2000);
        }

        this.persistenceConfiguration = new PersistenceConfiguration("PU_NAME");

        log.info("XXX "+ this.dbContainer.getDriverClassName());
        log.info("XXX "+ this.dbContainer.getJdbcUrl());
        log.info("XXX "+ this.dbContainer.getUsername());
        log.info("XXX "+ this.dbContainer.getPassword());

        persistenceConfiguration.property(JDBC_DRIVER, this.dbContainer.getDriverClassName());
        persistenceConfiguration.property(JDBC_URL, this.dbContainer.getJdbcUrl());
        persistenceConfiguration.property(JDBC_USER, this.dbContainer.getUsername());
        persistenceConfiguration.property(JDBC_PASSWORD, this.dbContainer.getPassword());

        persistenceConfiguration.managedClass(Person.class);

        persistenceConfiguration.validationMode(ValidationMode.CALLBACK);
        persistenceConfiguration.sharedCacheMode(SharedCacheMode.ALL);
        persistenceConfiguration.transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);

        this.currentEntityManagerFactory = createNewEntityManagerFactory();
    }

    public EntityManagerFactory createNewEntityManagerFactory() throws SchemaValidationException {
        this.currentEntityManagerFactory = persistenceConfiguration.createEntityManagerFactory();

        log.info("1. Created EMF: {} "+ this.currentEntityManagerFactory.getName());

        this.entityManagerFactories.add(this.currentEntityManagerFactory);

        this.currentEMFschemaManager = currentEntityManagerFactory.getSchemaManager();
        log.info("2. Created SchemaManager: {}"+ this.currentEMFschemaManager.toString());

        log.info("3. Creating database objects...");
        this.currentEMFschemaManager.create(true);
        log.info("4. Created database objects!");


        log.info("5. Validating database objects...");
        this.currentEMFschemaManager.validate();
        log.info("6. Validated database objects!");

        return this.currentEntityManagerFactory;
    }

    @Override
    public void close() throws Exception {
        entityManagerFactories.forEach(emf -> emf.getSchemaManager().truncate());

        entityManagerFactories.forEach(emf -> {
            try {
                ((AutoCloseable)emf).close();
            } catch (Exception e) {
                log.severe("Unable to close EMF: {}" + emf.getName());
            }
        });

        ((AutoCloseable) dbContainer).close();

        containerProvider = null;
    }
}
