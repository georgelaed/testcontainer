package com.test.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/***
 * "The ValidatorFactory object built by the bootstrap process should be cached and shared amongst Validator consumers."
 */
@Slf4j
public class EntityValidator implements AutoCloseable{

    private static volatile EntityValidator entityValidator;
    private final ValidatorFactory validatorFactory;

    public static EntityValidator getInstance() {
        if (entityValidator == null) {
            synchronized (EntityValidator.class) {
                if (entityValidator == null) {
                    entityValidator = new EntityValidator();
                    log.info("Created singleton instance of EntityValidator: {}!", entityValidator);
                }
            }
        }

        log.debug("Got EntityValidator instance {}!", entityValidator);

        return entityValidator;
    }

    private EntityValidator() {
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    public Set<ConstraintViolation<Object>> validate(Object object) {
        return this.validatorFactory.getValidator().validate(object);
    }

    @Override
    public void close() {
        log.info("Closing EntityValidator {}...", this);

        validatorFactory.close();

        entityValidator = null;

        log.info("EntityValidator {} closed!", this.hashCode());
    }
}
