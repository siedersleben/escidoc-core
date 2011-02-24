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
 * Copyright 2006-2010 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.common.business.filter;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.fedora.resources.ResourceType;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.servlet.EscidocServlet;
import de.escidoc.core.common.util.configuration.EscidocConfiguration;
import de.escidoc.core.common.util.service.ConnectionUtility;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.xml.XmlUtility;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract super class for all types of SRU requests.
 * 
 * @author sche
 * 
 * @spring.bean id="de.escidoc.core.common.business.filter.SRURequest"
 */
public class SRURequest {
    // map from resource type to the corresponding admin index
    private static final Map<ResourceType, String> ADMIN_INDEXES =
        new HashMap<ResourceType, String>() {
            private static final long serialVersionUID = -8847071005592073142L;

            {
                put(ResourceType.CONTAINER, "item_container_admin");
                put(ResourceType.CONTENT_MODEL, "content_model_admin");
                put(ResourceType.CONTENT_RELATION, "content_relation_admin");
                put(ResourceType.CONTEXT, "context_admin");
                put(ResourceType.ITEM, "item_container_admin");
                put(ResourceType.OU, "ou_admin");
            }
        };

    private ConnectionUtility connectionUtility = null;

    /**
     * Send an explain request to the SRW servlet and write the response to the
     * given writer. The given resource type determines the SRW index to use.
     * 
     * @param output
     *            writer to which the SRW response is written
     * @param resourceType
     *            resource type for which "explain" will be called
     * 
     * @throws WebserverSystemException
     *             Thrown if the connection to the SRW servlet failed.
     */
    public final void explain(
        final Writer output, final ResourceType resourceType)
        throws WebserverSystemException {
        try {
            final String url =
                EscidocConfiguration.getInstance().get(
                    EscidocConfiguration.SRW_URL)
                    + "/search/"
                    + ADMIN_INDEXES.get(resourceType)
                    + "?operation=explain&version=1.1";
            final HttpResponse response =
                connectionUtility.getRequestURL(new URL(url), null);

            if (response != null) {
                final HttpEntity entity = response.getEntity();
                BufferedReader input = null;
                try {
                    input =
                        new BufferedReader(new InputStreamReader(
                            entity.getContent(), getCharset(entity
                                .getContentType().getValue())));
                    String line;
                    while ((line = input.readLine()) != null) {
                        output.write(line);
                        output.write('\n');
                    }
                }
                finally {
                    if (input != null) {
                        try {
                            input.close();
                        }
                        catch (IOException e) {
                            // ignore this exception
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new WebserverSystemException(e);
        }
    }

    /**
     * Extract the charset information from the given content type header.
     * 
     * @param contentType
     *            content type header
     * @return charset information
     */
    private static String getCharset(final String contentType) {
        String result = XmlUtility.CHARACTER_ENCODING;

        if (contentType != null) {
            // FIXME better use javax.mail.internet.ContentType
            final String[] parameters = contentType.split(";");

            for (final String parameter : parameters) {
                if (parameter.startsWith("charset")) {
                    final String[] charset = parameter.split("=");

                    if (charset.length > 1) {
                        result = charset[1];
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Send a searchRetrieve request to the SRW servlet and write the response
     * to the given writer. The given resource type determines the SRW index to
     * use.
     * 
     * @param output
     *            Writer to which the SRW response is written.
     * @param resourceTypes
     *            Resource types to be expected in the SRW response.
     * @param parameters
     *            SRU request parameters
     * 
     * @throws WebserverSystemException
     *             Thrown if the connection to the SRW servlet failed.
     */
    public final void searchRetrieve(
        final Writer output, final ResourceType[] resourceTypes,
        final SRURequestParameters parameters) throws WebserverSystemException {
        searchRetrieve(output, resourceTypes, parameters.getQuery(),
            parameters.getLimit(), parameters.getOffset(),
            parameters.getUser(), parameters.getRole(),
            parameters.getRecordPacking());
    }

    /**
     * Send a searchRetrieve request to the SRW servlet and write the response
     * to the given writer. The given resource type determines the SRW index to
     * use.
     * 
     * @param output
     *            Writer to which the SRW response is written.
     * @param resourceTypes
     *            Resource types to be expected in the SRW response.
     * @param query
     *            Contains a query expressed in CQL to be processed by the
     *            server.
     * @param limit
     *            The number of records requested to be returned. The value must
     *            be 0 or greater. Default value if not supplied is determined
     *            by the server. The server MAY return less than this number of
     *            records, for example if there are fewer matching records than
     *            requested, but MUST NOT return more than this number of
     *            records.
     * @param offset
     *            The position within the sequence of matched records of the
     *            first record to be returned. The first position in the
     *            sequence is 1. The value supplied MUST be greater than 0.
     * @param user
     *            foreign user id to limit the access rights
     * @param role
     *            foreign role id to limit the access rights
     * @param recordPacking
     *            A string to determine how the record should be escaped in the
     *            response. Defined values are 'string' and 'xml'. The default
     *            is 'xml'.
     * 
     * @throws WebserverSystemException
     *             Thrown if the connection to the SRW servlet failed.
     */
    public final void searchRetrieve(
        final Writer output, final ResourceType[] resourceTypes,
        final String query, final int limit, final int offset,
        final String user, final String role, final RecordPacking recordPacking)
        throws WebserverSystemException {
        try {
            final StringBuilder internalQuery = new StringBuilder();

            if (query == null) {
                for (final ResourceType resourceType : resourceTypes) {
                    if (internalQuery.length() > 0) {
                        internalQuery.append(" OR ");
                    }
                    internalQuery.append("\"type\"=");
                    internalQuery.append(resourceType.getLabel());
                }
            }
            else {
                internalQuery.append(query);
            }

            String url =
                EscidocConfiguration.getInstance().get(
                    EscidocConfiguration.SRW_URL)
                    + "/search/"
                    + ADMIN_INDEXES.get(resourceTypes[0])
                    + '?'
                    + Constants.SRU_PARAMETER_OPERATION
                    + "=searchRetrieve&"
                    + Constants.SRU_PARAMETER_VERSION
                    + "=1.1&"
                    + Constants.SRU_PARAMETER_QUERY
                    + '='
                    + URLEncoder.encode(internalQuery.toString(),
                        XmlUtility.CHARACTER_ENCODING)
                    + '&'
                    + Constants.SRU_PARAMETER_START_RECORD + '=' + offset;

            if (limit != LuceneRequestParameters.DEFAULT_LIMIT) {
                url +=
                    '&' + Constants.SRU_PARAMETER_MAXIMUM_RECORDS + '=' + limit;
            }
            if (user != null) {
                url += '&' + Constants.SRU_PARAMETER_USER + '=' + user;
            }
            if (role != null) {
                url += '&' + Constants.SRU_PARAMETER_ROLE + '=' + role;
            }
            if (recordPacking != null) {
                url +=
                    '&' + Constants.SRU_PARAMETER_RECORD_PACKING + '='
                        + recordPacking.getType();
            }
            if (!UserContext.isRestAccess()) {
                url +=
                    '&' + Constants.SRU_PARAMETER_RECORD_SCHEMA
                        + "=eSciDocSoap";
            }

            final Cookie cookie =
                new BasicClientCookie(EscidocServlet.COOKIE_LOGIN,
                    UserContext.getHandle());
            final HttpResponse response =
                connectionUtility.getRequestURL(new URL(url), cookie);

            if (response != null) {
                final HttpEntity entity = response.getEntity();
                BufferedReader input = null;
                try {
                    input =
                        new BufferedReader(new InputStreamReader(
                            entity.getContent(), getCharset(entity
                                .getContentType().getValue())));
                    String line;
                    while ((line = input.readLine()) != null) {
                        output.write(line);
                        output.write('\n');
                    }
                }
                finally {
                    if (input != null) {
                        try {
                            input.close();
                        }
                        catch (IOException e) {
                            // ignore this exception
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new WebserverSystemException(e);
        }
    }

    /**
     * Set the connection utility.
     * 
     * @param connectionUtility
     *            ConnectionUtility.
     * 
     * @spring.property ref="escidoc.core.common.util.service.ConnectionUtility"
     */
    public void setConnectionUtility(final ConnectionUtility connectionUtility) {
        this.connectionUtility = connectionUtility;
    }
}
