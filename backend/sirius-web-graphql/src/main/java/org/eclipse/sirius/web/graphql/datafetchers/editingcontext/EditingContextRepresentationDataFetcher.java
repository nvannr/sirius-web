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
package org.eclipse.sirius.web.graphql.datafetchers.editingcontext;

import java.util.Objects;

import org.eclipse.sirius.components.annotations.spring.graphql.QueryDataFetcher;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.sirius.web.graphql.schema.EditingContextTypeProvider;
import org.eclipse.sirius.web.services.api.representations.IRepresentationService;
import org.eclipse.sirius.web.services.api.representations.RepresentationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.DataFetchingEnvironment;

/**
 * The data fetcher used to retrieve a representation of an editing context.
 * <p>
 * It will be used to fetch the data for the following GraphQL field:
 * </p>
 *
 * <pre>
 * type EditingContext {
 *   representation(representationId: ID!): Representation
 * }
 * </pre>
 *
 * @author sbegaudeau
 */
@QueryDataFetcher(type = EditingContextTypeProvider.TYPE, field = EditingContextTypeProvider.REPRESENTATION_FIELD)
public class EditingContextRepresentationDataFetcher implements IDataFetcherWithFieldCoordinates<RepresentationMetadata> {

    private final IRepresentationService representationService;

    private final Logger logger = LoggerFactory.getLogger(EditingContextRepresentationDataFetcher.class);

    public EditingContextRepresentationDataFetcher(IRepresentationService representationService) {
        this.representationService = Objects.requireNonNull(representationService);
    }

    @Override
    public RepresentationMetadata get(DataFetchingEnvironment environment) throws Exception {
        String editingContextId = environment.getSource();
        String representationId = environment.getArgument(EditingContextTypeProvider.REPRESENTATION_ID_ARGUMENT);
        try {
            // @formatter:off
            return this.representationService.getRepresentationDescriptorForProjectId(editingContextId, representationId)
                    .map(RepresentationDescriptor::getRepresentation)
                    .map(representation -> new RepresentationMetadata(representation.getId(), representation.getKind(), representation.getLabel(), representation.getDescriptionId()))
                    .orElse(null);
            // @formatter:on
        } catch (IllegalArgumentException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }
        return null;
    }

}
