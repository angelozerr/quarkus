package io.quarkus.qute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class QuickParserTest {

    @Test
    public void testLegacyParser() {
        Engine engine = Engine.builder()
                .addDefaultSectionHelpers()
                .useLegacyParser(true) // Explicitly use legacy parser
                .build();

        Template template = engine.parse("Hello {name}!");
        assertNotNull(template);
        assertEquals(1, template.getExpressions().size());
    }

    @Test
    public void testNewParser() {
        Engine engine = Engine.builder()
                .addDefaultSectionHelpers()
                .useLegacyParser(false) // Use new fault-tolerant parser
                .build();

        Template template = engine.parse("Hello {name}!");
        assertNotNull(template);

        // Check that the template is the AST TemplateNode
        assertEquals("io.quarkus.qute.parser.template.TemplateNode",
                template.getClass().getName(),
                "New parser should return TemplateNode AST");

        // Check that expressions are collected correctly
        assertEquals(1, template.getExpressions().size(),
                "Template should have 1 expression");
    }

    @Test
    public void testNewParserLineTracking() {
        Engine engine = Engine.builder()
                .addDefaultSectionHelpers()
                .useLegacyParser(false) // Use new parser
                .build();

        Template template = engine.parse("line1\n{foo}\nline3");

        // The AST node itself implements Origin
        io.quarkus.qute.parser.template.TemplateNode ast =
                (io.quarkus.qute.parser.template.TemplateNode) template;

        // Check that we can get line numbers
        assertEquals(1, ast.getLine(), "Template root should be on line 1");
    }
}
