/*******************************************************************************
 * Copyright (c) 2021 Obeo.
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
package org.eclipse.sirius.web.services.explorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.representations.IStatus;
import org.eclipse.sirius.web.representations.Success;
import org.eclipse.sirius.web.services.explorer.api.IRenameTreeItemHandler;
import org.eclipse.sirius.web.spring.collaborative.api.ChangeKind;
import org.eclipse.sirius.web.spring.collaborative.projects.EditingContextEventProcessor;
import org.eclipse.sirius.web.trees.TreeItem;
import org.springframework.stereotype.Service;

/**
 * Handles representation renaming triggered via a tree item from the explorer.
 *
 * @author sbegaudeau
 */
@Service
public class RenameRepresentationTreeItemHandler implements IRenameTreeItemHandler {

    @Override
    public boolean canHandle(IEditingContext editingContext, TreeItem treeItem, String newLabel) {
        return !ExplorerDescriptionProvider.DOCUMENT_KIND.equals(treeItem.getKind()) && !treeItem.getKind().contains("::"); //$NON-NLS-1$
    }

    @Override
    public IStatus handle(IEditingContext editingContext, TreeItem treeItem, String newLabel) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(EditingContextEventProcessor.REPRESENTATION_ID, treeItem.getId());
        parameters.put(EditingContextEventProcessor.REPRESENTATION_LABEL, newLabel);
        return new Success(ChangeKind.REPRESENTATION_TO_RENAME, parameters);
    }
}
