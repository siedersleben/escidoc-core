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

import javax.xml.bind.JAXBElement;

import net.sf.oval.guard.Guarded;

import org.escidoc.core.aa.GrantsRestService;
import org.escidoc.core.domain.service.ServiceUtility;
import org.escidoc.core.domain.sru.ResponseTypeTO;
import org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;

import java.util.Map;

/**
 * @author Michael Hoppe
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 */
@Guarded(applyFieldConstraintsToConstructors = false, applyFieldConstraintsToSetters = false,
    assertParametersNotNull = false, checkInvariants = false, inspectInterfaces = true)
public class GrantsRestServiceImpl implements GrantsRestService {

    @Autowired
    @Qualifier("service.UserAccountHandler")
    private UserAccountHandlerInterface userAccountHandler;

    @Autowired
    private ServiceUtility serviceUtility;

    /**
     *
     */
    protected GrantsRestServiceImpl() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.escidoc.core.aa.GrantsRestService#retrieveGrants(org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean)
     */
    @Override
    public JAXBElement<? extends ResponseTypeTO> retrieveGrants(final SruSearchRequestParametersBean queryParam)
            throws MissingMethodParameterException, InvalidSearchQueryException, AuthenticationException,
            AuthorizationException, SystemException {

        final Map<String, String[]> map = serviceUtility.handleSruRequest(queryParam, null, null, null);

        return (JAXBElement<? extends ResponseTypeTO>) serviceUtility.fromXML(
            this.userAccountHandler.retrieveGrants(map));
    }
}