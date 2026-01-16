package io.quarkus.qute.debug.java.ide;

import java.util.concurrent.CompletableFuture;

import io.quarkus.qute.debug.client.JavaSourceLocationArguments;
import io.quarkus.qute.debug.client.JavaSourceLocationResponse;
import io.quarkus.qute.debug.client.JavaSourceResolver;
import io.quarkus.qute.debug.java.JavaProject;
import io.quarkus.qute.debug.java.JavaTemplateAnnotation;

/**
 * Resolves qute-java:// URIs to Java files and line numbers. Simulates IDE
 * resolution (IJ / JDT) for startLine of @TemplateContents.
 */
public class IDEJavaSourceResolver implements JavaSourceResolver {

	private final JavaProject project;

	public IDEJavaSourceResolver(JavaProject project) {
		this.project = project;
	}

	@Override
	public CompletableFuture<JavaSourceLocationResponse> resolveJavaSource(JavaSourceLocationArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			JavaTemplateAnnotation t = project.findByUri(args.getJavaElementUri());
			if (t != null) {
				JavaSourceLocationResponse response = new JavaSourceLocationResponse();
				response.setJavaFileUri(t.sourceUri().toString());
				response.setStartLine(t.startLine());
				return response;
			}
			return new JavaSourceLocationResponse();
		});
	}

}
