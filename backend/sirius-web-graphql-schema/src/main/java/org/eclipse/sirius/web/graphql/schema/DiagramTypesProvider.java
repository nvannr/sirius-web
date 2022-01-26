/*******************************************************************************
 * Copyright (c) 2019, 2021 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.graphql.schema;

/**
 * This class regroup all diagram (Input & output) types.
 *
 * @author hmarchadour
 */
public class DiagramTypesProvider {

    public static final String DIAGRAM_TYPE = "Diagram"; //$NON-NLS-1$

    public static final String CREATE_EDGE_TOOL_TYPE = "CreateEdgeTool"; //$NON-NLS-1$

    public static final String CREATE_NODE_TOOL_TYPE = "CreateNodeTool"; //$NON-NLS-1$

    public static final String DELETE_TOOL_TYPE = "DeleteTool"; //$NON-NLS-1$

    public static final String TOOL_SECTION_TYPE = "ToolSection"; //$NON-NLS-1$

    public static final String TOOL_SECTIONS_FIELD = "toolSections"; //$NON-NLS-1$

    public static final String AUTO_LAYOUT_FIELD = "autoLayout"; //$NON-NLS-1$

}
