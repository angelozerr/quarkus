package io.quarkus.qute.debug.agent.source;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.lsp4j.debug.SourceResponse;

public class SourceReferenceRegistry {

    /**
     * Counter used to assign a unique ID to each frame.
     */
    private static final AtomicInteger sourceReferenceIdCounter = new AtomicInteger();

    private final Map<Integer, SourceResponse> sourceReferences = new ConcurrentHashMap<>();

    public SourceResponse getSourceReference(int sourceReference) {
        return sourceReferences.get(sourceReference);
    }

    public int registerSourceReference(URI uri) {
        int sourceReference = sourceReferenceIdCounter.incrementAndGet();
        String content = readFromJarUri(uri);
        String mimeType = "text/html";
        var response = new SourceResponse();
        response.setContent(content);
        response.setMimeType(mimeType);
        sourceReferences.put(sourceReference, response);
        return sourceReference;
    }

    public static String readFromJarUri(URI uri) {
        try {
            URL url = uri.toURL();
            try (InputStream in = url.openStream()) {
                return new String(in.readAllBytes());
            }
        } catch (Exception e) {
            return "";
        }
    }
}
