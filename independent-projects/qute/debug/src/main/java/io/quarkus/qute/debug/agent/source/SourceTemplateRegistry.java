package io.quarkus.qute.debug.agent.source;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.debug.Source;

import io.quarkus.qute.Engine;
import io.quarkus.qute.debug.agent.breakpoints.BreakpointsRegistry;

/**
 * Registry responsible for resolving and managing mappings between Qute
 * template IDs and their corresponding source files.
 * <p>
 * This class is used by the Qute debugger to locate template source files
 * during debugging sessions, supporting various base paths and file extensions.
 */
public class SourceTemplateRegistry {

    private final Map<String /* template id */, RemoteSource> templateIdToSource = new HashMap<>();

    private final Engine engine;
    private final List<String> basePaths;
    private final List<String> fileExtensions;

    private BreakpointsRegistry breakpointsRegistry;

    /**
     * Creates a registry with default base paths and file extensions.
     * <ul>
     * <li>Default base paths include:
     * <ul>
     * <li>{@code src/main/resources/templates/}</li>
     * <li>{@code templates/}</li>
     * <li>{@code content/}</li>
     * </ul>
     * </li>
     * <li>Default file extensions include:
     * <ul>
     * <li>.qute, .html, .qute.html</li>
     * <li>.yaml, .qute.yaml, .yml, .qute.yml</li>
     * <li>.txt, .qute.txt</li>
     * <li>.md, .qute.md</li>
     * </ul>
     * </li>
     * </ul>
     */
    public SourceTemplateRegistry(BreakpointsRegistry breakpointsRegistry, Engine engine) {
        this(breakpointsRegistry, engine,
                List.of("src/main/resources/templates/", /* Roq ... */"templates/", "content/"), //
                List.of(".qute", ".html", ".qute.html", ".yaml", ".qute.yaml", ".yml", ".qute.yml", ".txt", ".qute.txt",
                        ".md", ".qute.md"));
    }

    /**
     * Creates a registry with custom base paths and file extensions.
     *
     * @param basePaths List of possible base directories where templates might
     *        be located
     * @param fileExtensions List of supported file extensions for template files
     */
    public SourceTemplateRegistry(BreakpointsRegistry breakpointsRegistry, Engine engine, List<String> basePaths,
            List<String> fileExtensions) {
        this.breakpointsRegistry = breakpointsRegistry;
        this.engine = engine;
        this.basePaths = basePaths;
        this.fileExtensions = fileExtensions;
    }

    /**
     * Attempts to resolve a {@link Source} for a given template ID.
     * <p>
     * If the source was previously registered, it will be returned directly.
     * Otherwise, this method will try to infer the source location using known base
     * paths and file extensions.
     *
     * @param templateId The Qute template identifier
     * @param previousSource The previously known source, used to infer relative
     *        paths
     * @param sourceUri
     * @return The resolved {@link Source} or {@code null} if none found
     */
    public RemoteSource getSource(String templateId, Source previousSource) {
        RemoteSource source = templateIdToSource.get(templateId);
        if (source != null) {
            // Already cached.
            return source;
        }

        URI sourceUri = getSourceUriFromEngine(templateId, this.engine);
        if (sourceUri == null) {
            sourceUri = getGuesseddSourceUri(templateId, previousSource);
        }
        if (sourceUri != null) {
            source = new RemoteSource(sourceUri, templateId);
            templateIdToSource.put(templateId, source);
            return source;
        }

        /*
         * if (previousSource != null) {
         * // Try to infer source path relative to the previous source location
         * String path = previousSource.getPath().replace("\\", "/");
         * var existingBasePaths = getExistingBasePaths();
         * if (!existingBasePaths.isEmpty()) {
         * source = findSource(templateId, path, existingBasePaths);
         * if (source != null) {
         * return source;
         * }
         * }
         * source = findSource(templateId, path, basePaths);
         * if (source != null) {
         * return source;
         * }
         * }
         *
         * var existingBasePaths = getExistingBasePaths();
         * if (!existingBasePaths.isEmpty()) {
         * source = findSource(templateId, "", existingBasePaths);
         * if (source != null) {
         * return source;
         * }
         * }
         *
         * for (var entry : templateUriToSource.entrySet()) {
         * var uri = entry.getKey();
         * if (uri.getSchemeSpecificPart().endsWith(templateId)) {
         * templateIdToSource.put(templateId, entry.getValue());
         * return entry.getValue();
         * }
         * }
         */
        return null;
    }

    private URI getGuesseddSourceUri(String templateId, Source previousSource) {
        Set<URI> existingUris = new HashSet<>(breakpointsRegistry.getSourceUris());
        existingUris.addAll(templateIdToSource.values()
                .stream()
                .map(RemoteSource::getUri)
                .collect(Collectors.toSet()));
        if (existingUris.isEmpty()) {
            return null;
        }

        for (URI uri : existingUris) {
            if (uri.getSchemeSpecificPart().endsWith(templateId)) {
                return uri;
            }
        }
        return null;
    }

    private static URI getSourceUriFromEngine(String templateId, Engine engine) {
        var location = engine.locate(templateId);
        if (location.isPresent()) {
            var source = location.get().getSource();
            if (source.isPresent()) {
                return source.get();
            }
        }
        return null;
    }

    /*
     * private List<String> getExistingBasePaths() {
     * if (templateUriToSource.isEmpty()) {
     * return Collections.emptyList();
     * }
     * return templateUriToSource.keySet().stream() // transforme URI en Path
     * .map(uri -> {
     * try {
     * return Paths.get(uri).getParent().toString();
     * } catch (IllegalArgumentException e) {
     * // Cas des URI relatives comme file:src/...
     * String path = uri.getSchemeSpecificPart().replace("\\", "/");
     * int index = path.lastIndexOf('/');
     * if (index != -1) {
     * return path.substring(0, index);
     * }
     * return path;
     * }
     *
     * }) //
     * .distinct() // supprime les doublons
     * .sorted() // trie
     * .collect(Collectors.toList());
     * }
     *
     * private Source findSource(String templateId, String path, List<String> basePaths) {
     * for (var basePath : basePaths) {
     * int index = path.isEmpty() ? 0 : path.indexOf(basePath);
     * if (index != -1) {
     * String sourcePath = getValidSourcePath(
     * (path.isEmpty() ? basePath + "/" : path.substring(0, index + basePath.length())) + templateId);
     * if (sourcePath != null) {
     * Source source = new Source();
     * source.setPath(sourcePath);
     * templateIdToSource.put(templateId, source);
     * return source;
     * }
     * }
     * }
     * return null;
     * }
     */

    /**
     * Validates if a source path exists on the filesystem. If the path without
     * extension does not exist, this method tries with each supported file
     * extension.
     *
     * @param sourcePath Base path to validate
     * @return The first valid path found, or {@code null} if none exist
     */
    private String getValidSourcePath(String sourcePath) {
        if (Files.exists(Paths.get(sourcePath))) {
            return sourcePath;
        }
        for (var fileExtension : fileExtensions) {
            String sourcePathWithExt = sourcePath + fileExtension;
            if (Files.exists(Paths.get(sourcePathWithExt))) {
                return sourcePathWithExt;
            }
        }
        return null;
    }

    /**
     * Computes the template ID from a given source.
     *
     * @param source The source file
     * @return The template ID or {@code null} if it couldn't be determined
     */
    public String getTemplateId(Source source) {
        return toTemplateId(source);
    }

    /**
     * Converts a {@link Source} path to a template ID by stripping the base path.
     *
     * @param source The source
     * @return The template ID, or {@code null} if no base path matched
     */
    private String toTemplateId(Source source) {
        String path = source.getPath().replace("\\", "/");
        for (var basePath : basePaths) {
            int index = path.indexOf(basePath);
            if (index != -1) {
                return path.substring(index + basePath.length());
            }
        }
        return null;
    }

    /**
     * Returns the list of supported file extensions.
     *
     * @return List of supported file extensions
     */
    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    public static URI toUri(Source source) {
        String path = source.getPath();
        if (path == null) {
            return null;
        }
        try {
            return Paths.get(path).toUri();
        } catch (Exception e) {

        }
        try {
            return new URI("file", null, source.getPath(), null);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
