/*******************************************************************************
 * Copyright (c) 2022 Obeo.
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
package org.eclipse.sirius.web.graphql.datafetchers.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import org.eclipse.sirius.components.annotations.spring.graphql.SubscriptionDataFetcher;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.components.collaborative.forms.api.IFormEventProcessor;
import org.eclipse.sirius.components.collaborative.forms.api.RelatedElementsConfiguration;
import org.eclipse.sirius.components.collaborative.forms.dto.PropertiesEventInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.sirius.web.graphql.schema.SubscriptionTypeProvider;
import org.reactivestreams.Publisher;

import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Flux;

/**
 * The data fetcher used to send the refreshed properties to a subscription.
 *
 * @author sbegaudeau
 */
@SubscriptionDataFetcher(type = SubscriptionTypeProvider.TYPE, field = SubscriptionRelatedElementsEventDataFetcher.RELATED_ELEMENTS_EVENT_FIELD)
public class SubscriptionRelatedElementsEventDataFetcher implements IDataFetcherWithFieldCoordinates<Publisher<IPayload>> {

    public static final String RELATED_ELEMENTS_EVENT_FIELD = "relatedElementsEvent"; //$NON-NLS-1$

    private final ObjectMapper objectMapper;

    private final IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    public SubscriptionRelatedElementsEventDataFetcher(ObjectMapper objectMapper, IEditingContextEventProcessorRegistry editingContextEventProcessorRegistry) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.editingContextEventProcessorRegistry = Objects.requireNonNull(editingContextEventProcessorRegistry);
    }

    @Override
    public Publisher<IPayload> get(DataFetchingEnvironment environment) throws Exception {
        Object argument = environment.getArgument(SubscriptionTypeProvider.INPUT_ARGUMENT);
        var input = this.objectMapper.convertValue(argument, PropertiesEventInput.class);
        var propertiesConfiguration = new RelatedElementsConfiguration(input.getObjectIds());

        // @formatter:off
        return this.editingContextEventProcessorRegistry.getOrCreateEditingContextEventProcessor(input.getEditingContextId())
                .flatMap(processor -> processor.acquireRepresentationEventProcessor(IFormEventProcessor.class, propertiesConfiguration, input))
                .map(representationEventProcessor -> representationEventProcessor.getOutputEvents(input))
                .orElse(Flux.empty());
        // @formatter:on
    }

}
