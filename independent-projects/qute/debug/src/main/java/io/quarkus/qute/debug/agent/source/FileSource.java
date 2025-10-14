package io.quarkus.qute.debug.agent.source;

import java.net.URI;
import java.nio.file.Path;

public class FileSource extends RemoteSource {

    public FileSource(URI uri, String templateId) {
        super(uri, templateId);
        try {
            super.setPath(Path.of(uri).toString());
        } catch (Exception e) {
            super.setPath(uri.toASCIIString());
        }
    }

}
