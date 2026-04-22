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
package io.quarkus.qute.parser.template.sections;

import java.util.List;

import io.quarkus.qute.parser.template.ASTVisitor;
import io.quarkus.qute.parser.template.Parameter;
import io.quarkus.qute.parser.template.SectionKind;

/**
 * Include section.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#include_helpers
 */
public class IncludeSection extends CustomSection {

    public static final String TAG = "include";

    public IncludeSection(int start, int end) {
        super(TAG, start, end);
    }

    @Override
    public SectionKind getSectionKind() {
        return SectionKind.INCLUDE;
    }

    @Override
    protected void initializeParameters(List<Parameter> parameters) {
        // the first parameter (included template id) cannot have some expressions
        // the other parameters can have expression
        if (parameters.size() > 1) {
            for (int i = 1; i < parameters.size(); i++) {
                parameters.get(i).setCanHaveExpression(true);
            }
        }
    }

    /**
     * Returns the template id defined in parameter template of the include section
     * and null otherwise.
     *
     * @return the template id defined in parameter template of the include section
     *         and null otherwise.
     */
    public String getReferencedTemplateId() {
        Parameter templateParameter = getTemplateParameter();
        if (templateParameter == null) {
            return null;
        }
        return templateParameter.getValue();
    }

    /**
     * Returns the template parameter of the include section and null otherwise.
     *
     * @return the template parameter of the include section and null otherwise.
     */
    public Parameter getTemplateParameter() {
        return super.getParameterAtIndex(0);
    }

    @Override
    protected void accept0(ASTVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            List<Parameter> parameters = getParameters();
            for (Parameter parameter : parameters) {
                acceptChild(visitor, parameter);
            }
            acceptChildren(visitor, getChildren());
        }
        visitor.endVisit(this);
    }

    @Override
    public boolean canSupportUnterminatedSection() {
        return true;
    }
}
