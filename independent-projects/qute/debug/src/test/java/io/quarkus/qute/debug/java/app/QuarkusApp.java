package io.quarkus.qute.debug.java.app;

import java.util.function.Consumer;

import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.debug.RenderTemplateInThread;
import io.quarkus.qute.debug.java.JavaProject;

public class QuarkusApp {

	private final JavaProject javaProject;
	private QuteProcessorSimulator quteProcessor;

	public QuarkusApp(JavaProject javaProject, Consumer<EngineBuilder> observer) {
		this.javaProject = javaProject;
		this.quteProcessor = new QuteProcessorSimulator(javaProject, observer);
	}

	public RenderTemplateInThread render(String uri, StringBuilder renderResult, Consumer<TemplateInstance> configure)
			throws InterruptedException {
		var templateAnnotation = javaProject.findByUri(uri);
		if (templateAnnotation != null) {
			Template template = quteProcessor.getTemplate(templateAnnotation);
			return new RenderTemplateInThread(template, renderResult, configure);
			
		}
		return null;
	}
}
