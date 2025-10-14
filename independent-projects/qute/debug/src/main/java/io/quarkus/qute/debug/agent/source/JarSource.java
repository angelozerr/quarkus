package io.quarkus.qute.debug.agent.source;

import java.net.URI;

public class JarSource extends RemoteSource {

    public JarSource(URI uri, String templateId, SourceReferenceRegistry sourceReferenceRegistry) {
        super(uri, templateId);
        super.setSourceReference(sourceReferenceRegistry.registerSourceReference(uri));
        super.setName(getPathInsideJar(uri));
    }

    /**
     * Extracts the internal path of a resource inside a JAR URI.
     * <p>
     * Example:
     * jar:file:///C:/Users/.../quarkus-renarde-3.1.2.jar!/templates/tags/ifError.html
     * --> quarkus-renarde-3.1.2.jar!/templates/tags/ifError.html
     * <p>
     * If extraction fails, it gracefully falls back to the file name (e.g.
     * "ifError.html").
     */
    private static String getPathInsideJar(URI uri) {
        if (uri == null) {
            return "";
        }

        String s = uri.toString();

        // Only process JAR URIs
        if (!s.startsWith("jar:file:")) {
            return getFileNameFallback(s);
        }

        // Remove the "jar:file:/" prefix
        s = s.substring("jar:file:/".length());

        // Normalize potential leading slashes (Windows-specific)
        while (s.startsWith("/")) {
            s = s.substring(1);
        }

        // Try to locate the ".jar!" separator
        int jarIndex = s.lastIndexOf(".jar!");
        if (jarIndex == -1) {
            // If we can't find ".jar!", fallback to the file name
            return getFileNameFallback(s);
        }

        // Extract everything from the JAR name onward
        int startIndex = s.lastIndexOf('/', jarIndex - 1);
        if (startIndex == -1) {
            return getFileNameFallback(s);
        }

        return s.substring(startIndex + 1);
    }

    /**
     * Fallback method that extracts the last segment (file name) from a path or URI
     * string.
     */
    private static String getFileNameFallback(String s) {
        int idx = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (idx != -1 && idx < s.length() - 1) {
            return s.substring(idx + 1);
        }
        return s; // Return the whole string if no separator found
    }

}
