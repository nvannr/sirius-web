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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.persistence.entities.ProjectEntity;
import org.eclipse.sirius.web.persistence.entities.RepresentationEntity;
import org.eclipse.sirius.web.persistence.repositories.IProjectRepository;
import org.eclipse.sirius.web.persistence.repositories.IRepresentationRepository;
import org.eclipse.sirius.web.representations.IRepresentation;
import org.eclipse.sirius.web.representations.IRepresentationMetadata;
import org.eclipse.sirius.web.representations.ISemanticRepresentationMetadata;
import org.eclipse.sirius.web.services.api.id.IDParser;
import org.eclipse.sirius.web.services.api.representations.IRepresentationService;
import org.eclipse.sirius.web.services.api.representations.RepresentationDescriptor;
import org.eclipse.sirius.web.spring.collaborative.api.IDanglingRepresentationDeletionService;
import org.eclipse.sirius.web.spring.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.web.spring.collaborative.api.IStdDeserializerProvider;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * The service to manipulate representations.
 *
 * @author gcoutable
 */
@Service
public class RepresentationService implements IRepresentationService, IRepresentationPersistenceService, IDanglingRepresentationDeletionService {

    private static final String TIMER_NAME = "siriusweb_representation_save"; //$NON-NLS-1$

    private final IObjectService objectService;

    private final IProjectRepository projectRepository;

    private final IRepresentationRepository representationRepository;

    private final IStdDeserializerProvider<IRepresentation> representationDeserializerProvider;

    private final ObjectMapper objectMapper;

    private final Timer timer;

    public RepresentationService(IObjectService objectService, IProjectRepository projectRepository, IRepresentationRepository representationRepository, ObjectMapper objectMapper,
            IStdDeserializerProvider<IRepresentation> representationDeserializerProvider, MeterRegistry meterRegistry) {
        this.objectService = Objects.requireNonNull(objectService);
        this.projectRepository = Objects.requireNonNull(projectRepository);
        this.representationRepository = Objects.requireNonNull(representationRepository);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.representationDeserializerProvider = Objects.requireNonNull(representationDeserializerProvider);
        this.timer = Timer.builder(TIMER_NAME).register(meterRegistry);
    }

    @Override
    public boolean hasRepresentations(String objectId) {
        return this.representationRepository.hasRepresentations(objectId);
    }

    @Override
    public Optional<IRepresentationMetadata> getRepresentationDescriptorForProjectId(String projectId, String representationId) {
        var projectUUID = new IDParser().parse(projectId);
        var representationUUID = new IDParser().parse(representationId);

        if (projectUUID.isPresent() && representationUUID.isPresent()) {
            return this.representationRepository.findByIdAndProjectId(representationUUID.get(), projectUUID.get()).map(new RepresentationMapper(this.objectMapper)::toRepresentationMetadataDTO);
        }

        return Optional.empty();
    }

    @Override
    public List<RepresentationDescriptor> getRepresentationDescriptorsForProjectId(String projectId) {
        // @formatter:off
        return new IDParser().parse(projectId)
                .map(this.representationRepository::findAllByProjectId)
                .orElseGet(List::of)
                .stream()
                .map(representationEntity -> {
                    String kind = representationEntity.getKind();
                    Optional<Class<? extends IRepresentation>> implementationType = this.representationDeserializerProvider.getImplementationClass(kind);
                    return new RepresentationMapper(this.objectMapper).toDTO(representationEntity, implementationType.get());
                })
                .collect(Collectors.toUnmodifiableList());
        // @formatter:on
    }

    @Override
    public List<IRepresentationMetadata> getRepresentationDescriptorsForObjectId(String objectId) {
        // @formatter:off
        return this.representationRepository.findAllByTargetObjectId(objectId).stream()
                .map(new RepresentationMapper(this.objectMapper)::toRepresentationMetadataDTO)
                .collect(Collectors.toUnmodifiableList());
        // @formatter:on
    }

    @Override
    public void save(IEditingContext editingContext, ISemanticRepresentationMetadata representationMetadata, IRepresentation representation) {
        long start = System.currentTimeMillis();

        var editingContextId = new IDParser().parse(editingContext.getId());
        if (editingContextId.isPresent()) {
            // @formatter:off
            var representationDescriptor = RepresentationDescriptor.newRepresentationDescriptor(representationMetadata.getId())
                    .projectId(editingContextId.get())
                    .descriptionId(representationMetadata.getDescriptionId())
                    .targetObjectId(representationMetadata.getTargetObjectId())
                    .label(representationMetadata.getLabel())
                    .representation(representation)
                    .kind(representationMetadata.getKind())
                    .build();
            // @formatter:on

            var optionalProjectEntity = this.projectRepository.findById(representationDescriptor.getProjectId());
            if (optionalProjectEntity.isPresent()) {
                ProjectEntity projectEntity = optionalProjectEntity.get();
                RepresentationEntity representationEntity = new RepresentationMapper(this.objectMapper).toEntity(representationDescriptor, projectEntity);
                this.representationRepository.save(representationEntity);
            }
        }

        long end = System.currentTimeMillis();
        this.timer.record(end - start, TimeUnit.MILLISECONDS);
    }

    @Override
    public Optional<IRepresentationMetadata> getRepresentation(UUID representationId) {
        // @formatter:off
        return this.representationRepository.findById(representationId)
                .map(new RepresentationMapper(this.objectMapper)::toRepresentationMetadataDTO);
        // @formatter:off
    }

    @Override
    public boolean existsById(UUID representationId) {
        return this.representationRepository.existsById(representationId);
    }

    @Override
    public void delete(UUID representationId) {
        this.representationRepository.deleteById(representationId);
    }

    @Override
    public boolean isDangling(IEditingContext editingContext, IRepresentationMetadata representationMetadata) {
        if (representationMetadata instanceof ISemanticRepresentationMetadata) {
            ISemanticRepresentationMetadata semanticRepresentationMetadata = (ISemanticRepresentationMetadata) representationMetadata;
            String targetObjectId = semanticRepresentationMetadata.getTargetObjectId();
            Optional<Object> optionalObject = this.objectService.getObject(editingContext, targetObjectId);
            return optionalObject.isEmpty();
        }
        return false;
    }

    @Override
    public void deleteDanglingRepresentations(String editingContextId) {
        new IDParser().parse(editingContextId).ifPresent(this.representationRepository::deleteDanglingRepresentations);
    }
}
