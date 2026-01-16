package io.quarkus.qute.debug.java;

import java.net.URI;

public record JavaTemplateAnnotation(String templateId, String qualifiedName, URI sourceUri, int startLine,
		String content, URI quteUri) {
}
