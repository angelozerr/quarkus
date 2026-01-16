package io.quarkus.qute.debug.java;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public final class JavaMiniParser {

	private final String source;
	private URI sourceUri;

	private int index = 0;
	private int line = 1;
	private int braceDepth = 0;

	private String packageName = "";

	private final Deque<TypeContext> typeStack = new ArrayDeque<>();
	private PendingTemplate pendingTemplate;

	private final List<JavaTemplateAnnotation> templates = new ArrayList<>();

	public JavaMiniParser(String source) {
		this.source = source;
	}

	public URI getSourceUri() {
		return sourceUri;
	}

	public List<JavaTemplateAnnotation> parse() {
		while (!eof()) {
			char c = peek();

			if (c == '\n') {
				line++;
				index++;
				continue;
			}

			if (c == '/' && peek(1) == '/') {
				skipLineComment();
				continue;
			}

			if (c == '/' && peek(1) == '*') {
				skipBlockComment();
				continue;
			}

			if (c == '"' || c == '\'') {
				skipString(c);
				continue;
			}

			if (c == '{') {
				braceDepth++;
				index++;
				continue;
			}

			if (c == '}') {
				braceDepth--;
				popTypesIfNeeded();
				index++;
				continue;
			}

			if (isIdentifierStart(c)) {
				String ident = readIdentifier();

				if ("package".equals(ident)) {
					packageName = readQualifiedName();
				}

				if ("class".equals(ident) || "record".equals(ident)) {
					handleTypeDeclaration();
				}

				continue;
			}

			if (c == '@') {
				handleAnnotation();
				continue;
			}

			index++;
		}
		return templates;
	}

	// ---------------------------------------------------------------------

	private void handleAnnotation() {
		int annotationLine = line - 1;
		index++; // skip @
		String name = readIdentifier();

		if (!"TemplateContents".equals(name)) {
			return;
		}

		skipWhitespace();
		if (peek() != '(')
			return;
		index++;

		skipWhitespace();
		String content = readTemplateLiteral();

		pendingTemplate = new PendingTemplate(annotationLine, content);
	}

	private void handleTypeDeclaration() {
		skipWhitespace();
		String typeName = readIdentifier();
		if (sourceUri == null) {
			sourceUri = URI.create("file://home/" + typeName + ".java");
		}
		TypeContext ctx = new TypeContext(typeName, braceDepth + 1);
		typeStack.push(ctx);

		if (pendingTemplate != null) {
			String qualifiedName = buildQualifiedName();
			String templateId = qualifiedName + "_template_" + templates.size();
			templates.add(new JavaTemplateAnnotation(templateId, qualifiedName, sourceUri, pendingTemplate.line,
					pendingTemplate.content, URI.create("qute-java://" + qualifiedName + "@io.quarkus.qute.TemplateContents")));
			pendingTemplate = null;
		}
	}

	// ---------------------------------------------------------------------

	private String buildQualifiedName() {
		StringBuilder sb = new StringBuilder();

		if (!packageName.isEmpty()) {
			sb.append(packageName).append('.');
		}

		Iterator<TypeContext> it = typeStack.descendingIterator();
		while (it.hasNext()) {
			sb.append(it.next().name());
			if (it.hasNext()) {
				sb.append('$');
			}
		}
		return sb.toString();
	}

	private void popTypesIfNeeded() {
		while (!typeStack.isEmpty() && typeStack.peek().braceDepth > braceDepth) {
			typeStack.pop();
		}
	}

	// ---------------------------------------------------------------------
	// Lexing helpers
	// ---------------------------------------------------------------------

	private boolean eof() {
		return index >= source.length();
	}

	private char peek() {
		return source.charAt(index);
	}

	private char peek(int offset) {
		int i = index + offset;
		return i < source.length() ? source.charAt(i) : '\0';
	}

	private void skipWhitespace() {
		while (!eof() && Character.isWhitespace(peek())) {
			if (peek() == '\n')
				line++;
			index++;
		}
	}

	private boolean isIdentifierStart(char c) {
		return Character.isJavaIdentifierStart(c);
	}

	private String readIdentifier() {
		int start = index;
		while (!eof() && Character.isJavaIdentifierPart(peek())) {
			index++;
		}
		return source.substring(start, index);
	}

	private String readQualifiedName() {
		skipWhitespace();
		int start = index;
		while (!eof() && (Character.isJavaIdentifierPart(peek()) || peek() == '.')) {
			index++;
		}
		return source.substring(start, index);
	}

	private void skipLineComment() {
		while (!eof() && peek() != '\n')
			index++;
	}

	private void skipBlockComment() {
		index += 2;
		while (!eof() && !(peek() == '*' && peek(1) == '/')) {
			if (peek() == '\n')
				line++;
			index++;
		}
		index += 2;
	}

	private void skipString(char quote) {
		index++;
		while (!eof()) {
			char c = peek();
			if (c == '\\') {
				index += 2;
				continue;
			}
			if (c == quote) {
				index++;
				return;
			}
			if (c == '\n')
				line++;
			index++;
		}
	}

	private String readTemplateLiteral() {
		skipWhitespace();
		if (peek() == '"') {
			if (peek(1) == '"' && peek(2) == '"') {
				index += 3;
				int start = index;
				while (!(peek() == '"' && peek(1) == '"' && peek(2) == '"')) {
					if (peek() == '\n')
						line++;
					index++;
				}
				String s = source.substring(start, index);
				index += 3;
				return s;
			} else {
				index++;
				int start = index;
				while (peek() != '"')
					index++;
				String s = source.substring(start, index);
				index++;
				return s;
			}
		}
		return "";
	}

	// ---------------------------------------------------------------------

	record TypeContext(String name, int braceDepth) {
	}

	record PendingTemplate(int line, String content) {
	}
}
