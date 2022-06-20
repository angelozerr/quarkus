package io.quarkus.qute.debug.agent;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.Variable;

public abstract class RemoteScope {

    private static final AtomicInteger variablesReferenceCounter = new AtomicInteger();

    private Collection<Variable> variables;

    private final Scope scope;

    public RemoteScope(String name) {
        this.scope = new Scope();
        scope.setName(name);
        scope.setVariablesReference(variablesReferenceCounter.incrementAndGet());
    }

    public String getName() {
        return scope.getName();
    }

    public int getVariablesReference() {
        return scope.getVariablesReference();
    }

    public Collection<Variable> getVariables() {
        if (variables == null) {
            variables = createVariables();
        }
        return variables;
    }

    public Scope getData() {
        return scope;
    }

    protected abstract Collection<Variable> createVariables();
}
