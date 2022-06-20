package io.quarkus.hibernate.reactive.deployment;

import jakarta.persistence.PersistenceException;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.resteasy.reactive.server.spi.UnwrappedExceptionBuildItem;

public class ResteasyReactiveServerIntegrationProcessor {

    @BuildStep
    public UnwrappedExceptionBuildItem unwrappedExceptions() {
        return new UnwrappedExceptionBuildItem(PersistenceException.class);
    }
}
