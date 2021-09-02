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
package org.eclipse.sirius.web.services.representations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.persistence.entities.ProjectEntity;
import org.eclipse.sirius.web.persistence.entities.RepresentationEntity;
import org.eclipse.sirius.web.representations.IRepresentation;
import org.eclipse.sirius.web.representations.IRepresentationMetadata;
import org.eclipse.sirius.web.representations.SemanticRepresentationMetadata;
import org.eclipse.sirius.web.services.api.id.IDParser;
import org.eclipse.sirius.web.services.api.representations.RepresentationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to convert representation entities to representation descriptor data transfer objects and vice versa.
 *
 * @author sbegaudeau
 */
public class RepresentationMapper {

    private final Logger logger = LoggerFactory.getLogger(RepresentationMapper.class);

    private final ObjectMapper objectMapper;

    public RepresentationMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    /**
     * Converts a RepresentationEntity into an IRepresentationMetadata. Use this when you only need the representation's
     * metadata and not its actual content.
     *
     * @param representationEntity
     *            a representation entity.
     * @return a lightweight DTO with only the representation's metadata.
     */
    public IRepresentationMetadata toRepresentationMetadataDTO(RepresentationEntity representationEntity) {
        // @formatter:off
        return SemanticRepresentationMetadata.newRepresentationMetadata(representationEntity.getId().toString())
                                             .descriptionId(UUID.fromString(representationEntity.getDescriptionId()))
                                             .label(representationEntity.getLabel())
                                             .kind(representationEntity.getKind())
                                             .targetObjectId(representationEntity.getTargetObjectId())
                                             .build();
        // @formatter:on
    }

    /**
     * Converts a RepresentationEntity into a full RepresentationDescriptor. Use this only when you need the
     * representation's actual content in addition to its metadata.
     *
     * @param representationEntity
     *            a representation entity.
     * @param representationType
     *            the concrete Java class to use the deserialize the representation's content.
     * @return a heavy-weight DTO with the representation's metadata and complete deserialized content (as an instanceof
     *         of the specified representationType).
     */
    public RepresentationDescriptor toDTO(RepresentationEntity representationEntity, Class<? extends IRepresentation> representationType) {
        try {
            IRepresentation representation = this.objectMapper.readValue(representationEntity.getContent(), representationType);
            // @formatter:off
            return RepresentationDescriptor.newRepresentationDescriptor(representationEntity.getId().toString())
                    .label(representationEntity.getLabel())
                    .projectId(representationEntity.getProject().getId())
                    .descriptionId(UUID.fromString(representationEntity.getDescriptionId()))
                    .targetObjectId(representationEntity.getTargetObjectId())
                    .kind(representationEntity.getKind())
                    .representation(representation)
                    .build();
            // @formatter:on
        } catch (JsonProcessingException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }
        return null;
    }

    public RepresentationEntity toEntity(RepresentationDescriptor representationDescriptor, ProjectEntity projectEntity) {
        RepresentationEntity representationEntity = new RepresentationEntity();

        var representationId = new IDParser().parse(representationDescriptor.getId());
        representationEntity.setId(representationId.get());
        representationEntity.setProject(projectEntity);
        representationEntity.setLabel(representationDescriptor.getLabel());
        representationEntity.setDescriptionId(representationDescriptor.getDescriptionId().toString());
        representationEntity.setTargetObjectId(representationDescriptor.getTargetObjectId());
        representationEntity.setKind(representationDescriptor.getKind());
        try {
            String content = this.objectMapper.writeValueAsString(representationDescriptor.getRepresentation());
            representationEntity.setContent(content);
        } catch (JsonProcessingException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }

        return representationEntity;
    }

}
