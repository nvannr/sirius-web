/***********************************************************************************************
 * Copyright (c) 2021 Obeo. All Rights Reserved.
 * This software and the attached documentation are the exclusive ownership
 * of its authors and was conceded to the profit of Obeo SARL.
 * This software and the attached documentation are protected under the rights
 * of intellectual ownership, including the section "Titre II  Droits des auteurs (Articles L121-1 L123-12)"
 * By installing this software, you acknowledge being aware of this rights and
 * accept them, and as a consequence you must:
 * - be in possession of a valid license of use conceded by Obeo only.
 * - agree that you have read, understood, and will comply with the license terms and conditions.
 * - agree not to do anything that could conflict with intellectual ownership owned by Obeo or its beneficiaries
 * or the authors of this software
 *
 * Should you not agree with these terms, you must stop to use this software and give it back to its legitimate owner.
 ***********************************************************************************************/
package org.eclipse.sirius.web.sample.services;

import java.util.Collections;
import java.util.List;

import org.eclipse.sirius.ext.emf.edit.EditingDomainServices;
import org.eclipse.sirius.web.emf.view.IJavaServiceProvider;
import org.eclipse.sirius.web.view.View;
import org.springframework.stereotype.Service;

/**
 * Provider for {@link org.eclipse.sirius.ext.emf.edit.EditingDomainServices} services.
 *
 * @author adaussy
 */
@Service
public class EditingDomainServicesProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        return Collections.singletonList(EditingDomainServices.class);
    }

}
