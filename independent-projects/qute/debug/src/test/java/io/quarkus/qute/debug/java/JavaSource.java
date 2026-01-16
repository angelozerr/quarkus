package io.quarkus.qute.debug.java;

import java.util.List;

/**
 * Represents a Java source file (IDE / JDT / IntelliJ side). Contains full
 * content, path, and utilities to get FQN.
 */
public class JavaSource {

	private final String path;
	private final List<JavaTemplateAnnotation> templateAnnotations;

	public JavaSource(String content) {
		JavaMiniParser parser = new JavaMiniParser(content);
		templateAnnotations = parser.parse();
		path = parser.getSourceUri().toString();
	}

	public String getPath() {
		return path;
	}
	
	public List<JavaTemplateAnnotation> getTemplateAnnotations() {
		return templateAnnotations;
	}
}
