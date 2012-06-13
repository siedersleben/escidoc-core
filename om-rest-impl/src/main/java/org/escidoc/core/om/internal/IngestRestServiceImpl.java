/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
 * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
 * terms.
 */
package org.escidoc.core.om.internal;

import net.sf.oval.guard.Guarded;
import org.escidoc.core.domain.ObjectFactoryProvider;
import org.escidoc.core.domain.result.ResultTypeTO;
import org.escidoc.core.domain.service.ServiceUtility;
import org.escidoc.core.om.IngestRestService;
import org.escidoc.core.utils.io.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.escidoc.core.common.exceptions.EscidocException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.om.service.interfaces.IngestHandlerInterface;

import javax.xml.bind.JAXBElement;

/**
 * REST Service Implementation for Ingest.
 *
 * @author MIH
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 */
@Service
@Guarded(applyFieldConstraintsToConstructors = false, applyFieldConstraintsToSetters = false,
    assertParametersNotNull = false, checkInvariants = false, inspectInterfaces = true)
public class IngestRestServiceImpl implements IngestRestService {

    private final static Logger LOG = LoggerFactory.getLogger(IngestRestServiceImpl.class);

    @Autowired
    @Qualifier("service.IngestHandler")
    private IngestHandlerInterface ingestHandler;

    @Autowired
    private ServiceUtility serviceUtility;

    @Autowired
    private ObjectFactoryProvider factoryProvider;

    protected IngestRestServiceImpl() {
    }

    @Override
    public JAXBElement<ResultTypeTO> ingest(final Stream xmlStream)
        throws EscidocException {
        Object to;
        try {
            to = serviceUtility.fromXML(xmlStream);
        } catch (SystemException e) {
            throw new XmlCorruptedException(e.getMessage(), e);
        }
        return factoryProvider.getResultFactory().createResult(
            serviceUtility.fromXML(ResultTypeTO.class, this.ingestHandler.ingest(serviceUtility.toXML(to))));
    }
}