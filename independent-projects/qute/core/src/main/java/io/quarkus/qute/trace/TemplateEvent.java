package io.quarkus.qute.trace;

import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;

public class TemplateEvent {

    private final TemplateInstance templateInstance;
    private final Engine engine;

    public TemplateEvent(TemplateInstance templateInstance, Engine engine) {
        this.templateInstance = templateInstance;
        this.engine = engine;
    }

    public TemplateInstance getTemplateInstance() {
        return templateInstance;
    }

    public Engine getEngine() {
        return engine;
    }
}
