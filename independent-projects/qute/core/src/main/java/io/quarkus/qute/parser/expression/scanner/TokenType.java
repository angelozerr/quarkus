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
package io.quarkus.qute.parser.expression.scanner;

public enum TokenType {
    NamespacePart, //
    ObjectPart, //
    PropertyPart, //
    MethodPart, //
    OpenBracket, //
    CloseBracket, //
    InfixMethodPart, //
    InfixParameter, //

    Dot, //
    ColonSpace, //
    StartString, //
    EndString, //
    String, //

    Whitespace, //
    Unknown, //
    EOS;
}
