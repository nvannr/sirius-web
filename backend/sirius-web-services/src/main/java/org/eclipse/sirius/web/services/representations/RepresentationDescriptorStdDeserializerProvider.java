/***********************************************************************************************
 * Copyright (c) 2021 Obeo. All Rights Reserved.
 * This software and the attached documentation are the exclusive ownership
 * of its authors and was conceded to the profit of Obeo S.A.S.
 * This software and the attached documentation are protected under the rights
 * of intellectual ownership, including the section "Titre II  Droits des auteurs (Articles L121-1 L123-12)"
 * By installing this software, you acknowledge being aware of these rights and
 * accept them, and as a consequence you must:
 * - be in possession of a valid license of use conceded by Obeo only.
 * - agree that you have read, understood, and will comply with the license terms and conditions.
 * - agree not to do anything that could conflict with intellectual ownership owned by Obeo or its beneficiaries
 * or the authors of this software.
 *
 * Should you not agree with these terms, you must stop to use this software and give it back to its legitimate owner.
 ***********************************************************************************************/
package org.eclipse.sirius.web.services.representations;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.web.services.api.representations.RepresentationDescriptor;
import org.eclipse.sirius.web.spring.collaborative.api.IRepresentationDeserializer;
import org.eclipse.sirius.web.spring.collaborative.api.IStdDeserializerProvider;
import org.springframework.stereotype.Service;

/**
 * Provides a custom deserializer for representation descriptors.
 *
 * @author pcdavid
 */
@Service
public class RepresentationDescriptorStdDeserializerProvider implements IStdDeserializerProvider<RepresentationDescriptor> {
    private final List<IRepresentationDeserializer> representationDeserializers;

    public RepresentationDescriptorStdDeserializerProvider(List<IRepresentationDeserializer> representationDeserializers) {
        this.representationDeserializers = Objects.requireNonNull(representationDeserializers);
    }

    @Override
    public Class<RepresentationDescriptor> getType() {
        return RepresentationDescriptor.class;
    }

    @Override
    public StdDeserializer<RepresentationDescriptor> getDeserializer() {
        return new RepresentationDescriptorStdDeserializer(this.representationDeserializers);
    }

    @Override
    public Optional<Class<? extends RepresentationDescriptor>> getImplementationClass(String kind) {
        return Optional.empty();
    }

}
