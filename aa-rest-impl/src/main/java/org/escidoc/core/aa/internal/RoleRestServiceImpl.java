/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package org.escidoc.core.aa.internal;

import net.sf.oval.guard.Guarded;
import org.escidoc.core.domain.ObjectFactoryProvider;
import org.escidoc.core.domain.aa.role.RoleResourcesTypeTO;
import org.escidoc.core.domain.aa.role.RoleTypeTO;
import org.escidoc.core.domain.service.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.escidoc.core.aa.RoleRestService;
import de.escidoc.core.aa.service.interfaces.RoleHandlerInterface;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.RoleNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.RoleInUseViolationException;
import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
import de.escidoc.core.common.exceptions.system.SystemException;

import javax.xml.bind.JAXBElement;

/**
 * @author Michael Hoppe
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 *
 */
@Guarded(applyFieldConstraintsToConstructors = false, applyFieldConstraintsToSetters = false,
    assertParametersNotNull = false, checkInvariants = false, inspectInterfaces = true)
public class RoleRestServiceImpl implements RoleRestService {

    @Autowired
    @Qualifier("service.RoleHandler")
    private RoleHandlerInterface roleHandler;

    @Autowired
    private ServiceUtility serviceUtility;

    @Autowired
    private ObjectFactoryProvider factoryProvider;

    /**
     * 
     */
    protected RoleRestServiceImpl() {
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.RoleRestService#create(org.escidoc.core.domain.aa.JAXBElement<RoleTypeTO>)
     */
    @Override
    public JAXBElement<RoleTypeTO> create(final RoleTypeTO roleTo)
        throws UniqueConstraintViolationException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getRoleFactory().createRole(serviceUtility.fromXML(
            RoleTypeTO.class, this.roleHandler.create(serviceUtility.toXML(roleTo))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.RoleRestService#delete(java.lang.String)
     */
    @Override
    public void delete(final String id)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException, RoleNotFoundException,
        RoleInUseViolationException, SystemException {
        this.roleHandler.delete(id);
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.RoleRestService#retrieve(java.lang.String)
     */
    @Override
    public JAXBElement<RoleTypeTO> retrieve(final String id)
        throws RoleNotFoundException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException {
        return factoryProvider.getRoleFactory().createRole(serviceUtility.fromXML(
            RoleTypeTO.class, this.roleHandler.retrieve(id)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.RoleRestService#update(java.lang.String, org.escidoc.core.domain.aa.JAXBElement<RoleTypeTO>)
     */
    @Override
    public JAXBElement<RoleTypeTO> update(final String id, final RoleTypeTO roleTo)
        throws RoleNotFoundException, XmlCorruptedException, XmlSchemaValidationException,
        MissingAttributeValueException, UniqueConstraintViolationException, OptimisticLockingException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getRoleFactory().createRole(serviceUtility.fromXML(
            RoleTypeTO.class, this.roleHandler.update(id, serviceUtility.toXML(roleTo))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.RoleRestService#retrieveResources(java.lang.String)
     */
    @Override
    public JAXBElement<RoleResourcesTypeTO> retrieveResources(final String id)
        throws AuthenticationException, AuthorizationException, MissingMethodParameterException, RoleNotFoundException,
        SystemException {
        return factoryProvider.getRoleFactory().createResources(serviceUtility.fromXML(
            RoleResourcesTypeTO.class, this.roleHandler.retrieveResources(id)));
    }

}