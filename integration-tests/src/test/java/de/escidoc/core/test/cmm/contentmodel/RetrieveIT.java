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
package de.escidoc.core.test.cmm.contentmodel;

import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.remote.application.notfound.ContentModelNotFoundException;
import de.escidoc.core.test.EscidocAbstractTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test the mock implementation of the item resource.
 *
 * @author Michael Schneider
 */
public class RetrieveIT extends ContentModelTestBase {

    private String contentModelId;

    private String contentModelXml;

    /**
     * Set up servlet test.
     *
     * @throws Exception If anything fails.
     */
    @Before
    public void setUp() throws Exception {

        contentModelXml =
            EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTENT_MODEL_PATH + "/rest",
                "content-model-all-for-create.xml");
        contentModelXml = create(contentModelXml);
        contentModelId = getObjidValue(contentModelXml);
    }

    /**
     * Test retrieving an existing ContentModel. The excepted result is a XML representation of the created
     * ContentModel, corresponding to XML-schema "ContentModel.xsd"
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testCtmRCt1() throws Exception {

        Document contentModel = getDocument(this.contentModelXml);

        String retrievedXML = retrieve(this.contentModelId);

        validateContentModel(retrievedXML, getContentModelTitle(contentModel),
            getContentModelDescription(contentModel), getContentModelMdRecordDefinitions(contentModel),
            getContentModelResourceDefinitions(contentModel), getContentModelContentStreamDefinitions(contentModel),
            false);
    }

    /**
     * Test retrieving a not existing ContentModel.
     *
     * @throws Exception If anything fails.
     */
    @Test(expected = ContentModelNotFoundException.class)
    public void testCtmRCt2() throws Exception {

        retrieve(UNKNOWN_ID);
    }

    /**
     * Test retrieving a ContentModel with providing an id of an existing resource of another type.
     *
     * @throws Exception If anything fails.
     */
    @Test(expected = ContentModelNotFoundException.class)
    public void testCtmRCt2_2() throws Exception {

        retrieve(CONTEXT_ID);
    }

    /**
     * Test retrieving an ContentModel without id.
     *
     * @throws Exception If anything fails.
     */
    @Test(expected = MissingMethodParameterException.class)
    public void testCtmRCt3() throws Exception {

        retrieve(null);
    }

    /**
     * Test retrieve ContentModel Properties.
     */
    @Test
    public void testRetrieveContentModelResources() throws Exception {

        String subResource = null;
        try {
            subResource = retrieveResources(this.contentModelId);
        }
        catch (final NoSuchMethodException e) {
            return;
        }
        selectSingleNodeAsserted(getDocument(subResource), "/resources/version-history");
        assertXmlValidContentModel(subResource);
    }

    /**
     * Test retrieve of Content Model properties.
     * see issue INFR-1369.
     * 
     * @throws Exception
     */
    @Test
    public void retrieveProperties() throws Exception {

        String propertiesXml = retrieveProperties(this.contentModelId);
        assertXmlValidContentModel(propertiesXml);
    }

    /**
     * Test retrieve ContentModel content stream.
     */
    @Test
    public void testRetrieveContentModelContentStreams() throws Exception {
        String subResource = retrieveContentStreams(this.contentModelId);
        selectSingleNodeAsserted(getDocument(subResource), "/content-streams");
        assertXmlValidContentModel(subResource);
    }

}
