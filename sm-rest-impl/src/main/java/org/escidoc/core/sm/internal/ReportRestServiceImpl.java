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
package org.escidoc.core.sm.internal;

import net.sf.oval.guard.Guarded;
import org.escidoc.core.domain.ObjectFactoryProvider;
import org.escidoc.core.domain.service.ServiceUtility;
import org.escidoc.core.domain.sm.report.ReportTypeTO;
import org.escidoc.core.domain.sm.report.parameter.ReportParametersTypeTO;
import org.escidoc.core.sm.ReportRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.escidoc.core.common.exceptions.application.invalid.InvalidSqlException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ReportDefinitionNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.sm.service.interfaces.ReportHandlerInterface;

import javax.xml.bind.JAXBElement;

/**
 * @author Michael Hoppe
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 */
@Guarded(applyFieldConstraintsToConstructors = false, applyFieldConstraintsToSetters = false,
    assertParametersNotNull = false, checkInvariants = false, inspectInterfaces = true)
public class ReportRestServiceImpl implements ReportRestService {

    @Autowired
    @Qualifier("service.ReportHandler")
    private ReportHandlerInterface reportHandler;

    @Autowired
    private ServiceUtility serviceUtility;

    @Autowired
    private ObjectFactoryProvider factoryProvider;

    /**
     *
     */
    public ReportRestServiceImpl() {
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.sm.ReportRestService#retrieve(org.escidoc.core.domain.sm.ReportParametersTO)
     */
    @Override
    public JAXBElement<ReportTypeTO> retrieve(final ReportParametersTypeTO reportParametersTO)
        throws AuthenticationException, AuthorizationException, XmlCorruptedException, XmlSchemaValidationException,
        ReportDefinitionNotFoundException, MissingMethodParameterException, InvalidSqlException, SystemException {

        return factoryProvider.getReportFactory().createReport(serviceUtility
            .fromXML(ReportTypeTO.class, this.reportHandler.retrieve(serviceUtility.toXML(reportParametersTO))));
    }
}