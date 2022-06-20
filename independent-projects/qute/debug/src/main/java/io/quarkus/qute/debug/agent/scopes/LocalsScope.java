package io.quarkus.qute.debug.agent.scopes;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.lsp4j.debug.Variable;

import io.quarkus.qute.debug.agent.RemoteScope;

public class LocalsScope extends RemoteScope {

    public LocalsScope() {
        super("Locals");
    }

    protected Collection<Variable> createVariables() {
        Collection<Variable> variables = new ArrayList<>();
        // Variable variable = new VariableData("name", "Fred", "String");
        // variables.add(variable);
        return variables;
    }

}
