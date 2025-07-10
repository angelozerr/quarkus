package io.quarkus.qute;

import java.io.Reader;
import java.net.URL;
import java.util.Optional;

/**
 * Locates template sources. The locator with higher priority takes precedence.
 * Quarkus automatically register all CDI beans that implement this interface
 * with {@link EngineBuilder#addLocator(TemplateLocator)}.
 *
 * @see Engine#getTemplate(String)
 */
public interface TemplateLocator extends WithPriority {

    /**
     * Must return {@link Optional#empty()} if it's not possible to locate a template with the specified id.
     *
     * @param id
     * @return the template location for the given id
     */
    Optional<TemplateLocation> locate(String id);
    
    interface TemplateLocation {

        /**
         * A {@link Reader} instance produced by a locator is immediately closed right after the template content is parsed.
         *
         * @return the reader
         */
        Reader read();

        /**
         *
         * @return the template variant
         */
        Optional<Variant> getVariant();

        /**
         * Returns the URL pointing to the template resource.
         * <p>
         * This is typically a {@code file://} URL located in the runtime classpath
         * (e.g. inside {@code target/classes} or within a JAR), rather than the original
         * source file under {@code src/main/resources}.
         * <p>
         * The returned URL should represent the exact resource that was loaded
         * and parsed by the template engine.
         */
        default Optional<URL> getSource() {
            return Optional.empty();
        }

    }

}
