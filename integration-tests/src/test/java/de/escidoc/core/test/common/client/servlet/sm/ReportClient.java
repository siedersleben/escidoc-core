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
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.test.common.client.servlet.sm;

import de.escidoc.core.test.common.client.servlet.ClientBase;
import de.escidoc.core.test.Constants;

/**
 * Offers access methods to the escidoc interface of the Statistic Report resource.
 *
 * @author Michael Hoppe
 */
public class ReportClient extends ClientBase {

    /**
     * Create an item in the escidoc framework.
     *
     * @param xml report-parameters xml.
     * @return The HttpMethod after the service call .
     * @throws Exception If the service call fails.
     */
    public Object retrieve(final String xml) throws Exception {

        return callEsciDoc("Report.retrieve", METHOD_RETRIEVE, Constants.HTTP_METHOD_POST,
            Constants.WEB_CONTEXT_URI_ESCIDOC + Constants.STATISTIC_REPORT_BASE_URI, new String[] {}, xml);
    }

}
