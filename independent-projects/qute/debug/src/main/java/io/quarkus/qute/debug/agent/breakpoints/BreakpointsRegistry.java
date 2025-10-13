package io.quarkus.qute.debug.agent.breakpoints;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;

import io.quarkus.qute.debug.agent.source.RemoteSource;
import io.quarkus.qute.debug.agent.source.SourceTemplateRegistry;

public class BreakpointsRegistry {

    /** Breakpoints mapped by template ID and line number. */
    private final Map<URI, Map<Integer, RemoteBreakpoint>> breakpoints;

    public BreakpointsRegistry() {
        this.breakpoints = new ConcurrentHashMap<>();
    }

    public Breakpoint[] setBreakpoints(SourceBreakpoint[] sourceBreakpoints, Source source) {
        URI uri = SourceTemplateRegistry.toUri(source);
        Map<Integer, RemoteBreakpoint> templateBreakpoints = uri != null
                ? this.breakpoints.computeIfAbsent(uri, k -> new HashMap<>())
                : null;
        if (templateBreakpoints != null) {
            templateBreakpoints.clear();
        }

        Breakpoint[] result = new Breakpoint[sourceBreakpoints.length];
        for (int i = 0; i < sourceBreakpoints.length; i++) {
            SourceBreakpoint sourceBreakpoint = sourceBreakpoints[i];
            int line = sourceBreakpoint.getLine();
            String condition = sourceBreakpoint.getCondition();
            RemoteBreakpoint breakpoint = new RemoteBreakpoint(source, line, condition);
            if (templateBreakpoints != null) {
                templateBreakpoints.put(line, breakpoint);
                breakpoint.setVerified(true);
            } else {
                breakpoint.setVerified(false);
            }
            result[i] = breakpoint;
        }
        return result;
    }

    public Set<URI> getSourceUris() {
        return breakpoints.keySet();
    }

    /**
     * Retrieves a breakpoint for the given template and line number.
     *
     * @param sourceUri
     *
     * @param templateId the template identifier.
     * @param line the line number.
     * @param engine
     * @return the matching breakpoint, or {@code null} if none exists.
     */
    public RemoteBreakpoint getBreakpoint(URI sourceUri, String templateId, int line,
            SourceTemplateRegistry sourceTemplateRegistry) {
        Map<Integer, RemoteBreakpoint> templateBreakpoints = findTemplateBreakpoints(sourceUri, templateId,
                sourceTemplateRegistry);
        return templateBreakpoints != null ? templateBreakpoints.get(line) : null;
    }

    private Map<Integer, RemoteBreakpoint> findTemplateBreakpoints(URI sourceUri, String templateId,
            SourceTemplateRegistry sourceTemplateRegistry) {
        if (sourceUri != null) {
            return this.breakpoints.get(sourceUri);
        }
        RemoteSource source = sourceTemplateRegistry.getSource(templateId, null);
        URI uri = source != null ? source.getUri() : null;
        return uri != null ? this.breakpoints.get(uri) : null;
    }

    public void reset() {
        this.breakpoints.clear();
    }
}
