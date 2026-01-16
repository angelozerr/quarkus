package io.quarkus.qute.debug.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Java project with multiple JavaSource files.
 */
public class JavaProject {

	private final List<JavaSource> sources = new ArrayList<>();

	public void addSource(String source) {
		addSource(new JavaSource(source));
	}

	public void addSource(JavaSource source) {
		sources.add(source);
	}

	public List<JavaSource> getSources() {
		return sources;
	}

	public JavaTemplateAnnotation findByUri(String javaElementUri) {
		for (JavaSource source : getSources()) {
			List<JavaTemplateAnnotation> annotations = source.getTemplateAnnotations();
			for (JavaTemplateAnnotation t : annotations) {
				if (t.quteUri().toString().equals(javaElementUri)) {
					return t;
				}
			}
		}
		return null;
	}

	public JavaTemplateAnnotation findByTemplateId(String templateId) {
		for (JavaSource source : getSources()) {
			List<JavaTemplateAnnotation> annotations = source.getTemplateAnnotations();
			for (JavaTemplateAnnotation t : annotations) {
				if (t.templateId().toString().equals(templateId)) {
					return t;
				}
			}
		}
		return null;
	}
}
