/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package io.quarkus.qute.parser.template;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import io.quarkus.qute.Expression;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Variant;
import io.quarkus.qute.parser.CancelChecker;
import io.quarkus.qute.parser.template.ParameterDeclaration.JavaTypeRangeOffset;

public class TemplateNode extends Node implements Template {

    private CancelChecker cancelChecker;

    private String templateId;

    private String generatedId;

    private Optional<Variant> variant = Optional.empty();

    private Optional<URI> source = Optional.empty();

    private String text;

    // Line offsets - lazy computed and cached
    private volatile int[] lineOffsets;

    // Cached collections for Template interface
    private List<Expression> expressions;
    private List<io.quarkus.qute.ParameterDeclaration> paramDeclarations;
    private Map<String, Template.Fragment> fragments;

    public TemplateNode(String text) {
        super(0, text.length());
        this.text = text;
        super.setClosed(true);
    }

    /**
     * Get line offsets array, computing it lazily on first access.
     * This is only needed when converting offsets to line numbers (for error messages, etc.)
     * but not needed for LSP/IDE features that work with offsets directly.
     *
     * @return array of line start offsets
     */
    private int[] getLineOffsets() {
        if (lineOffsets == null) {
            synchronized (this) {
                if (lineOffsets == null) {
                    java.util.ArrayList<Integer> offsets = new java.util.ArrayList<>();
                    offsets.add(0);
                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == '\n') {
                            offsets.add(i + 1);
                        } else if (c == '\r' && (i + 1 >= text.length() || text.charAt(i + 1) != '\n')) {
                            offsets.add(i + 1);
                        }
                    }
                    lineOffsets = offsets.stream().mapToInt(Integer::intValue).toArray();
                }
            }
        }
        return lineOffsets;
    }

    /**
     * Get the line number (1-based) for the given offset.
     *
     * @param offset the offset
     * @return the line number (1-based)
     */
    public int getLineNumber(int offset) {
        int[] offsets = getLineOffsets();
        for (int i = 1; i < offsets.length; i++) {
            if (offset < offsets[i]) {
                return i;
            }
        }
        return offsets.length;
    }

    /**
     * Get the start offset of the given line number (1-based).
     *
     * @param lineNumber the line number (1-based)
     * @return the start offset of the line
     */
    public int getLineStartOffset(int lineNumber) {
        int[] offsets = getLineOffsets();
        if (lineNumber <= 0 || lineNumber > offsets.length) {
            return 0;
        }
        return offsets[lineNumber - 1];
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.Template;
    }

    public String getNodeName() {
        return "#template";
    }

    @Override
    public TemplateNode getOwnerTemplate() {
        return this;
    }

    public void setCancelChecker(CancelChecker cancelChecker) {
        this.cancelChecker = cancelChecker;
    }

    public CancelChecker getCancelChecker() {
        return cancelChecker;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setGeneratedId(String generatedId) {
        this.generatedId = generatedId;
    }

    public void setVariant(Optional<Variant> variant) {
        this.variant = variant;
    }

    public void setSource(Optional<URI> source) {
        this.source = source;
    }

    public void checkCanceled() {
        if (cancelChecker != null) {
            cancelChecker.checkCanceled();
        }
    }

    public String getText() {
        return text;
    }

    public String getText(RangeOffset range) {
        return getText(range.getStart(), range.getEnd());
    }

    public String getText(int start, int end) {
        String text = getText();
        return text.substring(start, end);
    }

    public ParameterDeclaration findInParameterDeclarationByAlias(String alias) {
        Optional<ParameterDeclaration> result = super.getChildren().stream() //
                .filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
                .filter(parameter -> alias.equals(((ParameterDeclaration) parameter).getAlias())) //
                .map(n -> ((ParameterDeclaration) n)) //
                .findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    public Set<String> getJavaTypesSupportedInNativeMode() {
        Set<String> javaTypesSupportedInNativeMode = new HashSet<>();
        // From parameter declaration
        for (Node node : super.getChildren()) {
            if (node.getKind() == NodeKind.ParameterDeclaration) {
                ParameterDeclaration parameter = (ParameterDeclaration) node;
                List<JavaTypeRangeOffset> classNameRanges = parameter.getJavaTypeNameRanges();
                for (RangeOffset classNameRange : classNameRanges) {
                    String className = this.getText(classNameRange);
                    javaTypesSupportedInNativeMode.add(className);
                }
            }
        }
        return javaTypesSupportedInNativeMode;
    }

    @Override
    protected void accept0(ASTVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getChildren());
        }
        visitor.endVisit(this);
    }

    public TemplateConfiguration getConfiguration() {
        // TODO: manage config
        return new TemplateConfiguration();
    }

    // ========== Template interface implementation ==========

    @Override
    public TemplateInstance instance() {
        // TODO: Implement proper template instance
        throw new UnsupportedOperationException("Template rendering not yet implemented with new parser");
    }

    @Override
    public List<Expression> getExpressions() {
        if (expressions == null) {
            expressions = collectExpressions();
        }
        return expressions;
    }

    @Override
    public Expression findExpression(Predicate<Expression> predicate) {
        return getExpressions().stream().filter(predicate).findFirst().orElse(null);
    }

    @Override
    public String getGeneratedId() {
        return generatedId != null ? generatedId : templateId;
    }

    @Override
    public String getId() {
        return templateId;
    }

    @Override
    public Optional<Variant> getVariant() {
        return variant;
    }

    @Override
    public List<io.quarkus.qute.ParameterDeclaration> getParameterDeclarations() {
        if (paramDeclarations == null) {
            paramDeclarations = collectParameterDeclarations();
        }
        return paramDeclarations;
    }

    @Override
    public Template.Fragment getFragment(String id) {
        if (fragments == null) {
            fragments = collectFragments();
        }
        return fragments.get(id);
    }

    @Override
    public Set<String> getFragmentIds() {
        if (fragments == null) {
            fragments = collectFragments();
        }
        return fragments.keySet();
    }

    @Override
    public List<io.quarkus.qute.TemplateNode> getNodes() {
        // TODO: Need to convert AST nodes to Quarkus TemplateNode interface
        // For now, return empty list
        return Collections.emptyList();
    }

    @Override
    public Collection<io.quarkus.qute.TemplateNode> findNodes(Predicate<io.quarkus.qute.TemplateNode> predicate) {
        return getNodes().stream().filter(predicate).toList();
    }

    @Override
    public io.quarkus.qute.SectionNode getRootNode() {
        // TODO: Need to create/convert root section node
        throw new UnsupportedOperationException("Root node conversion not yet implemented");
    }

    @Override
    public Optional<URI> getSource() {
        return source;
    }

    // Helper methods to collect data from AST

    private List<io.quarkus.qute.Expression> collectExpressions() {
        List<io.quarkus.qute.Expression> result = new ArrayList<>();
        accept(new ASTVisitor() {
            @Override
            public boolean visit(io.quarkus.qute.parser.template.Expression node) {
                // AST Expression already implements io.quarkus.qute.Expression
                result.add(node);
                return true;
            }
        });
        return Collections.unmodifiableList(result);
    }

    private List<io.quarkus.qute.ParameterDeclaration> collectParameterDeclarations() {
        List<io.quarkus.qute.ParameterDeclaration> result = new ArrayList<>();
        accept(new ASTVisitor() {
            @Override
            public boolean visit(ParameterDeclaration node) {
                // TODO: Convert AST ParameterDeclaration to Quarkus ParameterDeclaration
                // For now, collect them (will need adapter/wrapper)
                return false;
            }
        });
        return Collections.unmodifiableList(result);
    }

    private Map<String, Template.Fragment> collectFragments() {
        Map<String, Template.Fragment> result = new HashMap<>();
        accept(new ASTVisitor() {
            @Override
            public boolean visit(io.quarkus.qute.parser.template.sections.FragmentSection node) {
                // TODO: Extract fragment from section
                return false;
            }
        });
        return result;
    }
}
