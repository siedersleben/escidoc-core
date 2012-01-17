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
package de.escidoc.core.sm.internal;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.escidoc.core.domain.service.ServiceUtility;
import org.escidoc.core.domain.sm.ReportDefinitionListTO;
import org.escidoc.core.domain.sru.RequestType;
import org.escidoc.core.domain.sru.ResponseType;
import org.escidoc.core.domain.sru.parameters.SruRequestTypeFactory;
import org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.sm.ReportDefinitionsRestService;
import de.escidoc.core.sm.service.interfaces.ReportDefinitionHandlerInterface;

/**
 * @author Michael Hoppe
 *
 */
public class ReportDefinitionsRestServiceImpl implements ReportDefinitionsRestService {

    @Autowired
    @Qualifier("service.ReportDefinitionHandler")
    private ReportDefinitionHandlerInterface reportDefinitionHandler;

    /**
     * 
     */
    public ReportDefinitionsRestServiceImpl() {
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.sm.ReportDefinitionsRestService#retrieveReportDefinitions(SruSearchRequestParametersBean)
     */
    @Override
    public JAXBElement<? extends ResponseType> retrieveReportDefinitions(final SruSearchRequestParametersBean filter, final String roleId, final String userId, final String omitHighlighting)
        throws InvalidSearchQueryException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
		final List<String> additionalParams = new LinkedList<String>();
		additionalParams.add(roleId);
		additionalParams.add(userId);
		additionalParams.add(omitHighlighting);

		final JAXBElement<? extends RequestType> requestTO = SruRequestTypeFactory
				.createRequestTO(filter, additionalParams);

		return ((JAXBElement<? extends ResponseType>) ServiceUtility.fromXML(
				Constants.SRU_CONTEXT_PATH , this.reportDefinitionHandler
						.retrieveReportDefinitions(ServiceUtility.toMap(requestTO))));
    }

}
