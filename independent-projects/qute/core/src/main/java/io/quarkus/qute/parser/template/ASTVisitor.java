/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import io.quarkus.qute.parser.ASTVisitorBase;
import io.quarkus.qute.parser.expression.MethodPart;
import io.quarkus.qute.parser.expression.NamespacePart;
import io.quarkus.qute.parser.expression.ObjectPart;
import io.quarkus.qute.parser.expression.Parts;
import io.quarkus.qute.parser.expression.PropertyPart;
import io.quarkus.qute.parser.injection.LanguageInjectionNode;
import io.quarkus.qute.parser.template.sections.CaseSection;
import io.quarkus.qute.parser.template.sections.CustomSection;
import io.quarkus.qute.parser.template.sections.EachSection;
import io.quarkus.qute.parser.template.sections.ElseSection;
import io.quarkus.qute.parser.template.sections.ForSection;
import io.quarkus.qute.parser.template.sections.FragmentSection;
import io.quarkus.qute.parser.template.sections.IfSection;
import io.quarkus.qute.parser.template.sections.IncludeSection;
import io.quarkus.qute.parser.template.sections.InsertSection;
import io.quarkus.qute.parser.template.sections.IsSection;
import io.quarkus.qute.parser.template.sections.LetSection;
import io.quarkus.qute.parser.template.sections.SetSection;
import io.quarkus.qute.parser.template.sections.SwitchSection;
import io.quarkus.qute.parser.template.sections.WhenSection;
import io.quarkus.qute.parser.template.sections.WithSection;

/**
 * A visitor for abstract syntax trees.
 *
 * @author Angelo ZERR
 *
 */
public abstract class ASTVisitor extends ASTVisitorBase<Node> {

    /**
     * Visits the given AST node following the type-specific visit (after
     * <code>endVisit</code>).
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void postVisit(Node node) {
        // default implementation: do nothing
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(TemplateNode node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(ParameterDeclaration node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(CData node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(Comment node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(Text node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(CaseSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(CustomSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(EachSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(ElseSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(ForSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(FragmentSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(IfSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(IncludeSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(InsertSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(IsSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(LanguageInjectionNode node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(LetSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(SetSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(SwitchSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(WhenSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(WithSection node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(Parameter node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(Expression node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(Parts node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(NamespacePart node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(ObjectPart node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(PropertyPart node) {
        return true;
    }

    /**
     * Visits the given type-specific AST node.
     * <p>
     * The default implementation does nothing and return true. Subclasses may
     * reimplement.
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if the children of this node should be visited, and
     *         <code>false</code> if the children of this node should be skipped
     */
    public boolean visit(MethodPart node) {
        return true;
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(TemplateNode node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(ParameterDeclaration node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(CData node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(Comment node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(Text node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(CaseSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(CustomSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(EachSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(ElseSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(ForSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(FragmentSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(IfSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(IncludeSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(InsertSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(IsSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(LanguageInjectionNode node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(LetSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(SetSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(SwitchSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(WhenSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(WithSection node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(Parameter node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(Expression node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(Parts node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(NamespacePart node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(ObjectPart node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(PropertyPart node) {
        // default implementation: do nothing
    }

    /**
     * End of visit the given type-specific AST node.
     * <p>
     * The default implementation does nothing. Subclasses may reimplement.
     * </p>
     *
     * @param node the node to visit
     */
    public void endVisit(MethodPart node) {
        // default implementation: do nothing
    }
}
