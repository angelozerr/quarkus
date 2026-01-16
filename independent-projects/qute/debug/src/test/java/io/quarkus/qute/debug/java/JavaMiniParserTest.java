package io.quarkus.qute.debug.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class JavaMiniParserTest {

	@Test
	void parseInnerClassTemplate() {
	    String src = """
	        package org.acme;

	        public class HelloResource {

	            @TemplateContents("Hello {name}")
	            record Hello(String name) {}
	        }
	        """;

	    JavaMiniParser parser = new JavaMiniParser(src);
	    var templates = parser.parse();

	    assertEquals(1, templates.size());
	    var t = templates.get(0);

	    assertEquals("org.acme.HelloResource$Hello", t.qualifiedName());
	    assertEquals("Hello {name}", t.content());
	    assertEquals("file://HelloResource.java", t.sourceUri().toString());
	    assertEquals(5, t.startLine());
	    assertEquals("qute-java://org.acme.HelloResource$Hello@TemplateContents", t.quteUri().toString());
	}

}
