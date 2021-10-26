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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.core.api.IEditService;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.emf.services.EditingContext;
import org.eclipse.sirius.web.emf.services.SiriusWebJSONResourceFactoryImpl;
import org.eclipse.sirius.web.emf.view.IJavaServiceProvider;
import org.eclipse.sirius.web.emf.view.ViewConverter;
import org.eclipse.sirius.web.persistence.entities.DocumentEntity;
import org.eclipse.sirius.web.persistence.repositories.IDocumentRepository;
import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.services.api.representations.IDynamicRepresentationDescriptionService;
import org.eclipse.sirius.web.view.View;
import org.eclipse.sirius.web.view.ViewPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to discover diagram descriptions dynamically from the existing user-defined documents.
 *
 * @author pcdavid
 */
@Service
public class DynamicRepresentationDescriptionService implements IDynamicRepresentationDescriptionService {
    private final Logger logger = LoggerFactory.getLogger(DynamicRepresentationDescriptionService.class);

    private final IDocumentRepository documentRepository;

    private final EPackage.Registry ePackageRegistry;

    private final List<IJavaServiceProvider> javaServiceProviders;

    private final IObjectService objectService;

    private final IEditService editService;

    private final boolean isStudioDefinitionEnabled;

    public DynamicRepresentationDescriptionService(IDocumentRepository documentRepository, EPackage.Registry ePackageRegistry, IObjectService objectService, IEditService editService,
            List<IJavaServiceProvider> javaServiceProviders, @Value("${org.eclipse.sirius.web.features.studioDefinition:false}") boolean isStudioDefinitionEnabled) {
        this.documentRepository = Objects.requireNonNull(documentRepository);
        this.ePackageRegistry = Objects.requireNonNull(ePackageRegistry);
        this.javaServiceProviders = Objects.requireNonNull(javaServiceProviders);
        this.objectService = Objects.requireNonNull(objectService);
        this.editService = Objects.requireNonNull(editService);
        this.isStudioDefinitionEnabled = isStudioDefinitionEnabled;
    }

    @Override
    public List<IRepresentationDescription> findDynamicRepresentationDescriptions(Optional<IEditingContext> optionalEditingContext) {
        List<IRepresentationDescription> dynamicRepresentationDescriptions = new ArrayList<>();
        if (this.isStudioDefinitionEnabled) {
            List<EPackage> accessibleEPackages = optionalEditingContext.map(this::getAccessibleEPackages).orElse(List.of());
            ViewConverter viewConverter = new ViewConverter(this.javaServiceProviders, this.objectService, this.editService, this.isStudioDefinitionEnabled);
            this.documentRepository.findAllByType(ViewPackage.eNAME, ViewPackage.eNS_URI).forEach(documentEntity -> {
                Resource resource = this.loadDocumentAsEMF(documentEntity);
                // @formatter:off
                this.getViewDefinitions(resource).forEach(view -> viewConverter.convert(view, accessibleEPackages).stream()
                        .filter(Objects::nonNull)
                        .forEach(dynamicRepresentationDescriptions::add));
                // @formatter:on
            });
        }
        return dynamicRepresentationDescriptions;
    }

    private List<EPackage> getAccessibleEPackages(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext) {
            Registry packageRegistry = ((EditingContext) editingContext).getDomain().getResourceSet().getPackageRegistry();
            // @formatter:off
            return packageRegistry.values().stream()
                                  .filter(EPackage.class::isInstance)
                                  .map(EPackage.class::cast)
                                  .collect(Collectors.toList());
            // @formatter:on
        } else {
            return List.of();
        }
    }

    private Stream<View> getViewDefinitions(Resource resource) {
        return resource.getContents().stream().filter(View.class::isInstance).map(View.class::cast);
    }

    private Resource loadDocumentAsEMF(DocumentEntity documentEntity) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.setPackageRegistry(this.ePackageRegistry);
        URI uri = URI.createURI(documentEntity.getId().toString());
        JsonResource resource = new SiriusWebJSONResourceFactoryImpl().createResource(uri);
        resourceSet.getResources().add(resource);
        try (var inputStream = new ByteArrayInputStream(documentEntity.getContent().getBytes())) {
            resource.load(inputStream, null);
        } catch (IOException | IllegalArgumentException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }
        return resource;
    }
}
