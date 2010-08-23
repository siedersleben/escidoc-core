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
package de.escidoc.core.test.om.container;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.w3c.dom.Document;

import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
import de.escidoc.core.test.EscidocRestSoapTestsBase;

/**
 * Test the mock implementation of the Container resource.
 * 
 * @author MSC
 * 
 */
public class ContainerSubmitTest extends ContainerTestBase {

    private String theContainerXml;

    private String theContainerId;

    /**
     * @param transport
     *            The transport identifier.
     */
    public ContainerSubmitTest(final int transport) {
        super(transport);
    }

    /**
     * Set up servlet test.
     * 
     * @throws Exception
     *             If anything fails.
     */
    public void setUp() throws Exception {
        String xmlData =
            getContainerTemplate("create_container_WithoutMembers_v1.1.xml");

        this.theContainerXml = create(xmlData);
        this.theContainerId = getObjidValue(this.theContainerXml);
        super.setUp();
    }

    /**
     * Test successful submitting a container in state "pending".
     * 
     * @test.name Submit Container - Pending
     * @test.id OM_SC_1
     * @test.input
     *          <ul>
     *          <li>id of an existing container in state pending</li>
     *          <li>timestamp of the last modification of the container</li>
     *          </ul>
     * @test.expected: No result, no exception, Container has been submitted,
     *                 Last modification date of container has been updated.
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    public void testOM_SC_1() throws Exception {

        String paramXml = getTheLastModificationParam(false, theContainerId);
        final Document paramDocument =
            EscidocRestSoapTestsBase.getDocument(paramXml);
        final String pendingLastModificationDate =
            getLastModificationDateValue(paramDocument);

        try {
            submit(theContainerId, paramXml);
        }
        catch (Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Submitting the pending item failed. ", e);
        }

        String submittedXml = null;
        try {
            submittedXml = retrieve(theContainerId);
        }
        catch (Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving the revised, submitted item failed. ", e);
        }
        final Document submittedDocument =
            EscidocRestSoapTestsBase.getDocument(submittedXml);
        assertDateBeforeAfter(pendingLastModificationDate,
            getLastModificationDateValue(submittedDocument));
        assertXmlEquals("Unexpected status. ", submittedDocument,
            XPATH_CONTAINER_STATUS, STATE_SUBMITTED);
        assertXmlEquals("Unexpected current version status", submittedDocument,
            XPATH_CONTAINER_CURRENT_VERSION_STATUS, STATE_SUBMITTED);
    }

    /**
     * Test successful submitting a container in state "in-revision".
     * 
     * @test.name Submit Container - In Revision
     * @test.id OM_SC_1-2
     * @test.input
     *          <ul>
     *          <li>id of an existing container in state in-revision</li>
     *          <li>timestamp of the last modification of the container</li>
     *          </ul>
     * @test.expected: No result, no exception, Container has been submitted,
     *                 Last modification date of container has been updated.
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    public void testOM_SC_1_2() throws Exception {

        String paramXml = getTheLastModificationParam(false, theContainerId);
        submit(theContainerId, paramXml);
        paramXml = getTheLastModificationParam(false, theContainerId);
        revise(theContainerId, paramXml);
        paramXml = getTheLastModificationParam(false, theContainerId);
        final Document paramDocument =
            EscidocRestSoapTestsBase.getDocument(paramXml);
        final String revisedLastModificationDate =
            getLastModificationDateValue(paramDocument);

        try {
            submit(theContainerId, paramXml);
        }
        catch (Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Submitting the revised item failed. ", e);
        }

        String submittedXml = null;
        try {
            submittedXml = retrieve(theContainerId);
        }
        catch (Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving the revised, submitted item failed. ", e);
        }
        final Document submittedDocument =
            EscidocRestSoapTestsBase.getDocument(submittedXml);
        assertDateBeforeAfter(revisedLastModificationDate,
            getLastModificationDateValue(submittedDocument));
        assertXmlEquals("Unexpected status. ", submittedDocument,
            XPATH_CONTAINER_STATUS, STATE_SUBMITTED);
        assertXmlEquals("Unexpected current version status", submittedDocument,
            XPATH_CONTAINER_CURRENT_VERSION_STATUS, STATE_SUBMITTED);
    }

    /**
     * Test declining submit of container with non existing id.
     * 
     * @throws Exception
     */
    public void testOM_SC_2_1() throws Exception {

        String param = getTheLastModificationParam(false, theContainerId);

        try {
            submit("bla", param);
            fail("No exception occured on submit with non existing id.");
        }
        catch (Exception e) {
            Class<?> ec = ContainerNotFoundException.class;
            EscidocRestSoapTestsBase.assertExceptionType(ec.getName()
                + " expected.", ec, e);
        }
    }

    /**
     * Test declining submitting of container with wrong time stamp.
     * 
     * @throws Exception
     */
    public void test_OM_SC_2_2() throws Exception {

        String param = getTheLastModificationParam(false, theContainerId);
        param =
            param.replaceFirst(
                "<param last-modification-date=\"([0-9TZ:\\.-])+\"",
                "<param last-modification-date=\"2005-01-30T11:36:42.015Z\"");

        try {
            submit(theContainerId, param);
            fail("No exception occured on submit with wrong time stamp.");
        }
        catch (Exception e) {
            Class<?> ec = OptimisticLockingException.class;
            EscidocRestSoapTestsBase.assertExceptionType(ec.getName()
                + " expected.", ec, e);
        }
    }

    /**
     * Test declining submitting of container with missing id
     * 
     * @throws Exception
     */
    public void testOM_SC_3_1() throws Exception {

        String param = getTheLastModificationParam(false, theContainerId);

        try {
            submit(null, param);
            fail("No exception occured on submit with missing id.");
        }
        catch (Exception e) {
            Class<?> ec = MissingMethodParameterException.class;
            EscidocRestSoapTestsBase.assertExceptionType(ec.getName()
                + " expected.", ec, e);
        }
    }

    /**
     * Test declining submitting of container with missing time stamp
     * 
     * @throws Exception
     */
    public void testOM_SC_3_2() throws Exception {

        try {
            submit(theContainerId, null);
            fail("No exception occured on submit with missing time stamp.");
        }
        catch (Exception e) {
            Class<?> ec = MissingMethodParameterException.class;
            EscidocRestSoapTestsBase.assertExceptionType(ec.getName()
                + " expected.", ec, e);
        }
    }

    /**
     * Clean up after test.
     * 
     * @throws Exception
     *             If anything fails.
     */
    public void tearDown() throws Exception {

        super.tearDown();
        theContainerXml = null;

        theContainerId = null;

        // TODO purge object from Fedora
    }

}
