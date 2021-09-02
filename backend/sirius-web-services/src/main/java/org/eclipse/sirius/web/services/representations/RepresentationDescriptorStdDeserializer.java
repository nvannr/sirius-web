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
package org.eclipse.sirius.web.services.representations;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.web.representations.IRepresentation;
import org.eclipse.sirius.web.services.api.representations.RepresentationDescriptor;
import org.eclipse.sirius.web.spring.collaborative.api.IRepresentationDeserializer;

/**
 * Custom deserializer to customize the Jackson ObjectMapper.
 *
 * @author pcdavid
 */
public class RepresentationDescriptorStdDeserializer extends StdDeserializer<RepresentationDescriptor> {
    private static final long serialVersionUID = 7923505671577359470L;

    private final List<IRepresentationDeserializer> representationDeserializers;

    public RepresentationDescriptorStdDeserializer(List<IRepresentationDeserializer> representationDeserializers) {
        this(null, representationDeserializers);
    }

    public RepresentationDescriptorStdDeserializer(Class<?> valueClass, List<IRepresentationDeserializer> representationDeserializers) {
        super(valueClass);
        this.representationDeserializers = Objects.requireNonNull(representationDeserializers);
    }

    @Override
    public RepresentationDescriptor deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        RepresentationDescriptor representationDescriptor = null;
        ObjectCodec objectCodec = jsonParser.getCodec();
        if (objectCodec instanceof ObjectMapper) {
            ObjectMapper mapper = (ObjectMapper) objectCodec;
            ObjectNode root = mapper.readTree(jsonParser);
            String kind = root.get("kind").asText(); //$NON-NLS-1$
            IRepresentation representation = null;
            var optionalRepresentationImplementationClass = this.getRepresentationImplementationClass(kind);
            if (optionalRepresentationImplementationClass.isPresent()) {
                representation = mapper.readValue(root.get("representation").toString(), optionalRepresentationImplementationClass.get()); //$NON-NLS-1$
            }
            // @formatter:off
            representationDescriptor = RepresentationDescriptor.newRepresentationDescriptor(root.get("id").asText()) //$NON-NLS-1$
                    .projectId(UUID.fromString(root.get("projectId").asText())) //$NON-NLS-1$
                    .descriptionId(UUID.fromString(root.get("descriptionId").asText())) //$NON-NLS-1$
                    .targetObjectId(root.get("targetObjectId").asText()) //$NON-NLS-1$
                    .label(root.get("label").asText()) //$NON-NLS-1$
                    .kind(kind)
                    .representation(representation)
                    .build();
            // @formatter:on
        }
        return representationDescriptor;
    }

    private Optional<Class<? extends IRepresentation>> getRepresentationImplementationClass(String kind) {
        for (var deserializer : this.representationDeserializers) {
            Optional<Class<? extends IRepresentation>> actualClass = deserializer.getImplementationClass(kind);
            if (actualClass.isPresent()) {
                return actualClass;
            }
        }
        return Optional.empty();
    }

}
