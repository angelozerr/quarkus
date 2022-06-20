package io.quarkus.qute.debug.agent;

import io.quarkus.qute.trace.ResolveEvent;
import io.quarkus.qute.trace.TemplateEvent;
import io.quarkus.qute.trace.TraceListener;

public class DebuggerTraceListener implements TraceListener {

    private final DebuggeeAgent agent;

    public DebuggerTraceListener(DebuggeeAgent agent) {
        this.agent = agent;
    }

    @Override
    public void beforeResolve(ResolveEvent event) {

    }

    @Override
    public void afterResolve(ResolveEvent event) {
        agent.onTemplateNode(event);
    }

    @Override
    public void startTemplate(TemplateEvent event) {
        agent.onStartTemplate(event);

    }

    @Override
    public void endTemplate(TemplateEvent event) {
        agent.onEndTemplate(event);
    }

}
