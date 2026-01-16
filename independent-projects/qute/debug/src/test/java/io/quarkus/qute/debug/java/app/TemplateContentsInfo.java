package io.quarkus.qute.debug.java.app;

/**
 * Metadata extracted from @TemplateContents in JavaSource
 */
public final class TemplateContentsInfo {

	private final String templateId;
	private final String template;

	public TemplateContentsInfo(String templateId, String template) {
		this.templateId = templateId;
		this.template = template;
	}

	public String getTemplateId() {
		return templateId;
	}

	public String getTemplate() {
		return template;
	}
}
