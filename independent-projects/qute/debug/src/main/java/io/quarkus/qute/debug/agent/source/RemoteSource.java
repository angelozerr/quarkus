package io.quarkus.qute.debug.agent.source;

import java.net.URI;
import java.nio.file.Path;

import org.eclipse.lsp4j.debug.Source;

public class RemoteSource extends Source {

    private final transient URI uri;

    private final String templateId;

    public RemoteSource(URI uri, String templateId) {
        this.uri = uri;
        this.templateId = templateId;
        try {
            super.setPath(Path.of(uri).toString());
        } catch (Exception e) {
            super.setPath(uri.toASCIIString());
        }
    }

    public URI getUri() {
        return uri;
    }

    public String getTemplateId() {
        return templateId;
    }
}
