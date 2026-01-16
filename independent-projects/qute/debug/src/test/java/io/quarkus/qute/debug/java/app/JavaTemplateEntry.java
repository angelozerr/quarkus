package io.quarkus.qute.debug.java.app;

/**
 * Represents one embedded Qute template extracted from a Java source.
 *
 * content = exact character sequence consumed by Qute (TemplateContents only)
 * sourceUri = qute-java:/path/to/Class.java
 */
final class JavaTemplateEntry {

	final String templateId;
	final String content;     // exact Qute template
	final String sourceUri;   // qute-java:/...

	JavaTemplateEntry(String templateId, String content, String sourceUri) {
		this.templateId = templateId;
		this.content = content;
		this.sourceUri = sourceUri;
	}
}
