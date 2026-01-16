package io.quarkus.qute.debug.java.app;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.quarkus.qute.Engine;
import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.Variant;
import io.quarkus.qute.debug.java.JavaProject;
import io.quarkus.qute.debug.java.JavaSource;
import io.quarkus.qute.debug.java.JavaTemplateAnnotation;

/**
 * Simulates QuteProcessor: - Iterates over JavaSource files -
 * Extracts @TemplateContents - Creates JavaTemplateEntry with templateId,
 * sourceUri, content - Provides Engine to render templates in memory with
 * optional debug port
 */
public class QuteProcessorSimulator {

	private final JavaProject project;
	private final Map<String, JavaTemplateEntry> templateMap = new HashMap<>();
	private final Engine engine;

	public QuteProcessorSimulator(JavaProject project, Consumer<EngineBuilder> observer) {
		this.project = project;
		indexTemplates();
		EngineBuilder builder = Engine.builder() //
				.addDefaults().addValueResolver(new ReflectionValueResolver()).addLocator((String id) -> {
					JavaTemplateEntry entry = templateMap.get(id);
					if (entry == null)
						return Optional.empty();
					return Optional.of(new TemplateLocator.TemplateLocation() {
						@Override
						public java.io.Reader read() {
							return new StringReader(entry.content);
						}

						@Override
						public Optional<java.net.URI> getSource() {
							return Optional.of(java.net.URI.create(entry.sourceUri));
						}

						@Override
						public Optional<Variant> getVariant() {
							return Optional.empty();
						}
					});
				});
		observer.accept(builder);
		this.engine = builder.build();
	}

	private void indexTemplates() {
		for (JavaSource source : project.getSources()) {
			List<JavaTemplateAnnotation> annotations = source.getTemplateAnnotations();
			for (int i = 0; i < annotations.size(); i++) {
				JavaTemplateAnnotation templateAnnotation = annotations.get(i);

				String content = templateAnnotation.content();
				String uri = templateAnnotation.quteUri().toString();
				String templateId = templateAnnotation.templateId();

				JavaTemplateEntry entry = new JavaTemplateEntry(templateId, content, uri);
				templateMap.put(templateId, entry);
			}
		}
	}

	public Engine getEngine() {
		return engine;
	}

	public Template getTemplate(JavaTemplateAnnotation templateAnnotation) {
		String templateId = templateAnnotation.templateId();
		Template template = engine.getTemplate(templateId);
		if (template != null) {
			return template;
		}
		return engine.parse(templateAnnotation.content(), null, templateId);
	}
}
