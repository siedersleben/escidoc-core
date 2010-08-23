package de.escidoc.core.test.oai.setdefinition;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.escidoc.core.test.EscidocRestSoapTestsBase;
import de.escidoc.core.test.common.client.servlet.Constants;

public class SetDefinitionFilterTest extends SetDefinitionTestBase {
    /**
     * @param transport
     *            The transport identifier.
     */
    public SetDefinitionFilterTest(final int transport) {
        super(transport);
    }

    /**
     * Set up test.
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Override
    public void setUp() throws Exception {

        super.setUp();
    }

    /**
     * Clean up after test.
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Override
    public void tearDown() throws Exception {

        super.tearDown();
        // delete(itemId);
    }

    /**
     * Test successful retrieving one existing set definition resource using
     * filter set specification.
     * 
     * @throws Exception
     *             e
     */
    public void testRetrieveBySetSpecification() throws Exception {
        Document createdSetDefinitionDocument =
            createSuccessfully("escidoc_setdefinition_for_create.xml");
        String objid = getObjidValue(createdSetDefinitionDocument);
        String setSpecValue =
            selectSingleNode(createdSetDefinitionDocument,
                "/set-definition/specification").getTextContent();
        final String filterParamXml =
            "<param>" + getFilter(FILTER_SET_SPECIFICATION, setSpecValue)
                + "</param>";

        String retrievedSetDefinitionsXml = null;
        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParamXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSetDefinitions(retrievedSetDefinitionsXml);
        final Document retrievedDocument =
            EscidocRestSoapTestsBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument,
                XPATH_SET_DEFINITION_LIST_SET_DEFINITION);
        assertEquals("Unexpected number of set definitions.", 1,
            setDefinitionNodes.getLength());
        String objidInTheList = null;
        if (getTransport() == Constants.TRANSPORT_REST) {
            String href =
                selectSingleNode(retrievedDocument,
                    XPATH_SET_DEFINITION_LIST_SET_DEFINITION + "/@href")
                    .getNodeValue();
            objidInTheList = getIdFromHrefValue(href);
        }
        else {
            objidInTheList =
                selectSingleNode(retrievedDocument,
                    XPATH_SET_DEFINITION_LIST_SET_DEFINITION + "/@objid")
                    .getNodeValue();
        }
        assertEquals("objid of the set definition is wrong", objid,
            objidInTheList);
    }

    /**
     * Test successful retrieving one existing set definition resource using
     * filter set specification.
     * 
     * @throws Exception
     *             e
     */
    public void testRetrieveBySetSpecificationCQL() throws Exception {
        Document createdSetDefinitionDocument =
            createSuccessfully("escidoc_setdefinition_for_create.xml");
        String objid = getObjidValue(createdSetDefinitionDocument);
        String setSpecValue =
            selectSingleNode(createdSetDefinitionDocument,
                "/set-definition/specification").getTextContent();
        final Map<String, String[]> filterParams =
            new HashMap<String, String[]>();
        filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\""
            + FILTER_SET_SPECIFICATION + "\"=\"" + setSpecValue });
        String retrievedSetDefinitionsXml = null;

        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSrwResponse(retrievedSetDefinitionsXml);
        final Document retrievedDocument =
            EscidocRestSoapTestsBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument,
                XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION);
        assertEquals("Unexpected number of set definitions.", 1,
            setDefinitionNodes.getLength());
        String objidInTheList = null;
        if (getTransport() == Constants.TRANSPORT_REST) {
            String href =
                selectSingleNode(retrievedDocument,
                    XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION + "/@href")
                    .getNodeValue();
            objidInTheList = getIdFromHrefValue(href);
        }
        else {
            objidInTheList =
                selectSingleNode(retrievedDocument,
                    XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION + "/@objid")
                    .getNodeValue();
        }
        assertEquals("objid of the set definition is wrong", objid,
            objidInTheList);
    }

    /**
     * Test successful retrieving existing set definitions resource using filter
     * set specification pattern.
     * 
     * @throws Exception
     *             e
     */
    public void testRetrieveBySetSpecificationLike() throws Exception {
        createSuccessfully("escidoc_setdefinition_for_create.xml");
        // String setSpecValue = selectSingleNode(createdSetDefinitionDocument,
        // "/set-definition/specification").getTextContent();
        final String filterParamXml =
            "<param>" + getFilter(FILTER_SET_SPECIFICATION, "%Set1%")
                + "</param>";

        String retrievedSetDefinitionsXml = null;
        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParamXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSetDefinitions(retrievedSetDefinitionsXml);
        final Document retrievedDocument =
            EscidocRestSoapTestsBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument,
                XPATH_SET_DEFINITION_LIST_SET_DEFINITION);

        int length = setDefinitionNodes.getLength();
        boolean moreThenOne = length > 1;
        assertEquals("Wrong number in the list", true, moreThenOne);

    }

    /**
     * Test successful retrieving existing set definitions resource using filter
     * set specification pattern.
     * 
     * @throws Exception
     *             e
     */
    public void testRetrieveBySetSpecificationLikeCQL() throws Exception {
        createSuccessfully("escidoc_setdefinition_for_create.xml");
        final Map<String, String[]> filterParams =
            new HashMap<String, String[]>();
        filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\""
            + FILTER_SET_SPECIFICATION + "\"=\"%Set1%" });
        String retrievedSetDefinitionsXml = null;

        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSrwResponse(retrievedSetDefinitionsXml);
        final Document retrievedDocument =
            EscidocRestSoapTestsBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument,
                XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION);
        int length = setDefinitionNodes.getLength();
        boolean moreThenOne = length > 1;
        assertEquals("Wrong number in the list", true, moreThenOne);
    }

    /**
     * Test successful retrieving existing set definitions without filter.
     * 
     * @throws Exception
     *             e
     */
    public void testRetrieveSetDefinitions() throws Exception {
        // Document createdSetDefinitionDocument =
        // createSuccessfully("escidoc_setdefinition_for_create.xml");
        // String objid = getObjidValue(createdSetDefinitionDocument);
        // String setSpecValue = selectSingleNode(createdSetDefinitionDocument,
        // "/set-definition/specification").getTextContent();
        final String filterParamXml =
            "<param>" + "<limit>6</limit>" + "<offset>0</offset>" + "</param>";

        String retrievedSetDefinitionsXml = null;
        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParamXml);
        }
        catch (final Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSetDefinitions(retrievedSetDefinitionsXml);
        final Document retrievedDocument =
            EscidocRestSoapTestsBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument,
                XPATH_SET_DEFINITION_LIST_SET_DEFINITION);

        int length = setDefinitionNodes.getLength();
        boolean moreThenOne = length >= 1;
        assertEquals("Wrong number in the list", true, moreThenOne);

    }

    /**
     * Test successful retrieving existing set definitions without filter.
     * 
     * @throws Exception
     *             e
     */
    public void testRetrieveSetDefinitionsCQL() throws Exception {
        final Map<String, String[]> filterParams =
            new HashMap<String, String[]>();
        filterParams.put(FILTER_PARAMETER_MAXIMUMRECORDS, new String[] {"6"});
        String retrievedSetDefinitionsXml = null;

        try {
            retrievedSetDefinitionsXml = retrieveSetDefinitions(filterParams);
        }
        catch (final Exception e) {
            EscidocRestSoapTestsBase.failException(
                "Retrieving of list of set definitions failed. ", e);
        }
        assertXmlValidSrwResponse(retrievedSetDefinitionsXml);
        final Document retrievedDocument =
            EscidocRestSoapTestsBase.getDocument(retrievedSetDefinitionsXml);
        final NodeList setDefinitionNodes =
            selectNodeList(retrievedDocument,
                XPATH_SRW_SET_DEFINITION_LIST_SET_DEFINITION);
        int length = setDefinitionNodes.getLength();
        boolean moreThenOne = length >= 1;
        assertEquals("Wrong number in the list", true, moreThenOne);
    }

    /**
     * Test successfully retrieving an explain response.
     * 
     * @test.name explainTest
     * @test.id explainTest
     * @test.input
     * @test.expected: valid explain response.
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void explainTest() throws Exception {
        final Map<String, String[]> filterParams =
            new HashMap<String, String[]>();

        filterParams.put(EscidocRestSoapTestsBase.FILTER_PARAMETER_EXPLAIN,
            new String[] {""});

        String result = null;

        try {
            result = retrieveSetDefinitions(filterParams);
        }
        catch (Exception e) {
            EscidocRestSoapTestsBase.failException(e);
        }
        assertXmlValidSrwResponse(result);        
    }
}
