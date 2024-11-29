package com.test.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;
import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.SchemaValidationException;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import lombok.extern.slf4j.Slf4j;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import java.util.UUID;

import static jakarta.persistence.PersistenceConfiguration.JDBC_DRIVER;
import static jakarta.persistence.PersistenceConfiguration.JDBC_PASSWORD;
import static jakarta.persistence.PersistenceConfiguration.JDBC_URL;
import static jakarta.persistence.PersistenceConfiguration.JDBC_USER;

@Slf4j
public class EntityManagerFactoryProvider implements AutoCloseable {

    private static final JdbcDatabaseContainer<?> MYSQL = new MySQLContainer<>("mysql:lts");
    private static final JdbcDatabaseContainer<?> JDBC_DATABASE_CONTAINER = MYSQL;

    private static volatile EntityManagerFactoryProvider entityManagerFactoryProvider;
    private final JdbcDatabaseContainer<?> dbContainer;

    private EntityManagerFactory entityManagerFactory;

    public static EntityManagerFactoryProvider getInstance() {
        return getInstance(JDBC_DATABASE_CONTAINER);
    }

    public static EntityManagerFactoryProvider getInstance(JdbcDatabaseContainer<?> dbContainer) {
        if (entityManagerFactoryProvider == null) {
            synchronized (EntityManagerFactoryProvider.class) {
                if (entityManagerFactoryProvider == null) {
                    entityManagerFactoryProvider = new EntityManagerFactoryProvider(dbContainer);
                }
            }
        }

        log.info("Got DB Helper instance {}!", entityManagerFactoryProvider);
        return entityManagerFactoryProvider;
    }

    private EntityManagerFactoryProvider(JdbcDatabaseContainer<?> dbContainer) {
        this.dbContainer = dbContainer;
        this.dbContainer.start();
    }

    private PersistenceConfiguration createPersistenceConfiguration() {
        var persistenceConfiguration = new PersistenceConfiguration("neo.core.requestMetrics" + UUID.randomUUID());

        persistenceConfiguration.property(JDBC_DRIVER, this.dbContainer.getDriverClassName());
        persistenceConfiguration.property(JDBC_URL, this.dbContainer.getJdbcUrl());
        persistenceConfiguration.property(JDBC_USER, this.dbContainer.getUsername());
        persistenceConfiguration.property(JDBC_PASSWORD, this.dbContainer.getPassword());


        persistenceConfiguration.validationMode(ValidationMode.CALLBACK);
        persistenceConfiguration.sharedCacheMode(SharedCacheMode.NONE);
        persistenceConfiguration.transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);

        return persistenceConfiguration;
    }

    /***
     * "Usually, there is exactly one EntityManagerFactory for each persistence unit"
     * @param
     * @return
     * @throws SchemaValidationException
     */
    public EntityManagerFactory createEntityManagerFactory() throws SchemaValidationException {
        log.info("#### Getting EMF...");

        if (this.entityManagerFactory != null) {
            log.info("@@@@ EMF already exists!: {} ", this.entityManagerFactory.getName());
            var schemaManager = this.entityManagerFactory.getSchemaManager();

            schemaManager.validate();
            schemaManager.truncate();
            schemaManager.drop(true);

            this.entityManagerFactory.close();
            this.entityManagerFactory = null;
        }

        var persistenceConfiguration = this.createPersistenceConfiguration();

        this.entityManagerFactory = persistenceConfiguration.createEntityManagerFactory();

        log.info("1. Created EMF: {} ", this.entityManagerFactory.getName());

        var schemaManager = entityManagerFactory.getSchemaManager();
        log.info("2. Created SchemaManager: {}", schemaManager.toString());

        log.info("3. Creating database objects...");
        schemaManager.create(true);
        log.info("4. Created database objects!");

        log.info("5. Validating database objects...");
        schemaManager.validate();
        log.info("6. Validated database objects!");

        return this.entityManagerFactory;
    }

    @Override
    public void close() throws Exception {
        ((AutoCloseable) entityManagerFactory).close();
        ((AutoCloseable) dbContainer)         .close();

        entityManagerFactoryProvider = null;
    }
}
