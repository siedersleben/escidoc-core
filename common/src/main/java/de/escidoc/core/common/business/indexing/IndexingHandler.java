/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
 * the specific language governing permissions and limitations under the License.
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

package de.escidoc.core.common.business.indexing;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.business.fedora.resources.listener.ResourceListener;
import de.escidoc.core.common.exceptions.system.ApplicationServerSystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.IOUtils;
import de.escidoc.core.common.util.configuration.EscidocConfiguration;
import de.escidoc.core.common.util.service.ConnectionUtility;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.stax.handler.SrwScanResponseHandler;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.index.IndexRequest;
import de.escidoc.core.index.IndexRequestBuilder;
import de.escidoc.core.index.IndexService;

/**
 * Handler for synchronous indexing via gsearch.
 *
 * @author Michael Hoppe
 */
@Service("common.business.indexing.IndexingHandler")
public class IndexingHandler implements ResourceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexingHandler.class);

    /**
     * Names of fields containing Primary-Keys.
     */
    private static final String[] INDEX_PRIM_KEY_FIELDS = { "PID", "distinction.rootPid" };

    private static final String SRW_QUERY =
        Constants.SRW_MAXIMUM_TERMS_PATTERN.matcher(Constants.SRW_SCAN_PARAMS).replaceFirst(
            Integer.toString(Constants.SRW_MAXIMUM_SCAN_TERMS));

    @Autowired
    @Qualifier("common.business.indexing.GsearchHandler")
    private GsearchHandler gsearchHandler;

    @Autowired
    @Qualifier("de.escidoc.core.index.IndexService")
    private IndexService indexService;

    @Autowired
    @Qualifier("business.TripleStoreUtility")
    private TripleStoreUtility tripleStoreUtility;

    @Autowired
    @Qualifier("common.business.indexing.IndexingCacheHandler")
    private IndexingCacheHandler indexingCacheHandler;

    @Autowired
    @Qualifier("escidoc.core.common.util.service.ConnectionUtility")
    private ConnectionUtility connectionUtility;

    private final DocumentBuilder docBuilder;

    private boolean notifyIndexerEnabled = true;

    private Collection<String> indexNames;

    private Map<String, Map<String, Map<String, Object>>> objectTypeParameters;

    /**
     * Protected constructor to prevent instantiation outside of the Spring-context.
     */
    protected IndexingHandler() throws SystemException {
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            this.docBuilder = docBuilderFactory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e) {
            throw new SystemException(e.getMessage(), e);
        }
        this.notifyIndexerEnabled =
            EscidocConfiguration.getInstance().getAsBoolean(EscidocConfiguration.ESCIDOC_CORE_NOTIFY_INDEXER_ENABLED);
    }

    // begin implementation of ResourceListener

    /**
     * Resource was created, so write indexes.
     *
     * @param id resource id
     * @throws SystemException The resource could not be stored.
     */
    @Override
    public void resourceCreated(final String id, final String xml) throws SystemException {
        if (!this.notifyIndexerEnabled) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing STARTING, xml is " + xml);
        }
        if (xml != null && xml.length() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writing xml in cache");
            }
            indexingCacheHandler.writeObjectInCache(id, xml);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("gsearchindexing caching xml via deviation handler " + " finished.");
            }
        }
        final String objectType = tripleStoreUtility.getObjectType(id);
        addResource(id, objectType, xml);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing whole indexing of resource " + id + " of type " + objectType + " finished");
        }
    }

    /**
     * Delete a resource from the indexes.
     *
     * @param id resource id
     * @throws SystemException The resource could not be deleted.
     */
    @Override
    public void resourceDeleted(final String id) throws SystemException {
        if (!this.notifyIndexerEnabled) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing STARTING deletion");
        }
        deleteResource(id);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing whole deletion of resource " + id + " finished");
        }
    }

    /**
     * Replace a resource in the indexes.
     *
     * @param id resource id
     * @throws SystemException The resource could not be deleted and newly created.
     */
    @Override
    public void resourceModified(final String id, final String xml) throws SystemException {
        if (!this.notifyIndexerEnabled) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing STARTING, xml is " + xml);
        }
        final String objectType = tripleStoreUtility.getObjectType(id);

        if (objectType != null) {
            indexingCacheHandler.removeObjectFromCache(id);
            if (xml != null && xml.length() > 0) {
                indexingCacheHandler.writeObjectInCache(id, xml);
            }
            addResource(id, objectType, xml);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing whole indexing of resource " + id + " of type " + objectType + " finished");
        }
    }

    // end implementation of ResourceListener

    /**
     * Add resource to index.
     *
     * @param resource   href of the resource to index.
     * @param objectType type of object to index.
     * @param xml        xml of the resource to index.
     * @throws SystemException e
     */
    private void addResource(final String resource, final String objectType, final String xml) throws SystemException {
        indexResource(resource, objectType,
            de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_UPDATE_VALUE, xml);
    }

    /**
     * delete resource from index.
     *
     * @param resource href of the resource to index.
     * @throws SystemException e
     */
    private void deleteResource(final String resource) throws SystemException {
        doIndexing(resource, null,
            de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_DELETE_VALUE, false, null, true);
    }

    /**
     * Check if indexing has to be done synchronously or asynchronously. If synchronously, immediately index. If
     * asynchronously, write in message-queue. If both, do both (resource can be indexed in more than one index).
     *
     * @param resource   href of the resource to index.
     * @param objectType type of object to index.
     * @param action     indexing-action (update or delete).
     * @param xml        object-representation in xml.
     * @throws SystemException e
     */
    private void indexResource(final String resource, final String objectType, final String action, final String xml)
        throws SystemException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Do indexing for resource " + resource + ", objectType: " + objectType);
        }

        // check if there exist indexing-parameters for given resource.
        // If not, do nothing.
        if (getObjectTypeParameters().get(objectType) != null) {
            boolean indexAsynch = false;
            boolean indexSynch = false;
            for (final Map<String, Object> indexParameters : getObjectTypeParameters().get(objectType).values()) {
                if (indexParameters.get("indexAsynchronous") != null
                    && Boolean.valueOf((String) indexParameters.get("indexAsynchronous"))) {
                    indexAsynch = true;
                }
                else {
                    indexSynch = true;
                }
            }
            if (indexSynch) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("indexing synchronously");
                }
                doIndexing(resource, objectType, action, false, xml, true);
            }
            if (indexAsynch) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("indexing asynchronously");
                }
                final IndexRequest indexRequest =
                    IndexRequestBuilder
                        .createIndexRequest().withResource(resource).withObjectType(objectType).withAction(action)
                        .withData(xml).withIsReindexerCaller(false).build();
                this.indexService.index(indexRequest);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gsearchindexing resource " + resource + " of type " + objectType + ", action=" + action
                + " finished");
        }
    }

    /**
     * Check indexing-action (update, delete or create-empty). If update, check indexing-configuration for resource and
     * do update for specified indexes.
     *
     * @param resource   String resource.
     * @param objectType String name of the resource (eg Item, Container...).
     * @param action     String indexing-action (update, delete, create-empty).
     * @param isAsynch   boolean called asynch.
     * @param xml        object-representation in xml.
     * @throws SystemException e
     */
    public void doIndexing(
        final String resource, final String objectType, final String action, final boolean isAsynch, final String xml,
        final boolean commitIndex) throws SystemException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("calling do Indexing with resource: " + resource + ", objectType: " + objectType
                + ", action: " + action + ", isAsynch: " + isAsynch + ", xml: " + xml);
        }
        if (action == null
            || action
                .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_UPDATE_VALUE)) {

            // get Index-Parameters for resourceName
            final Map<String, Map<String, Object>> resourceParameters = getObjectTypeParameters().get(objectType);
            if (resourceParameters == null) {
                return;
            }
            for (final String indexName : resourceParameters.keySet()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("indexing for index " + indexName);
                }
                doIndexing(resource, objectType, indexName, action, isAsynch, xml, commitIndex);
            }
        }
        else if (action
            .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_DELETE_VALUE)) {
            gsearchHandler.requestDeletion(resource, null, null, commitIndex);
            gsearchHandler.requestDeletion(resource, null, Constants.LATEST_VERSION_PID_SUFFIX, commitIndex);
            gsearchHandler.requestDeletion(resource, null, Constants.LATEST_RELEASE_PID_SUFFIX, commitIndex);
        }
        else if (action
            .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_CREATE_EMPTY_VALUE)) {
            gsearchHandler.requestCreateEmpty(null);
        }
        else if (action
            .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_COMMIT_VALUE)) {
            gsearchHandler.requestCommitWrites(null);
        }
    }

    /**
     * Check indexing-action (update, delete or create-empty). Do action for specified index.
     *
     * @param resource   String resource.
     * @param objectType String name of the resource (eg Item, Container...).
     * @param indexName  name of the index
     * @param action     String indexing-action (update, delete, create-empty).
     * @param isAsynch   boolean called asynch.
     * @param xml        object-representation in xml.
     * @throws SystemException e
     */
    public void doIndexing(
        final String resource, final String objectType, final String indexName, final String action,
        final boolean isAsynch, final String xml, final boolean commitIndex) throws SystemException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("indexing " + resource + ", objectType: " + objectType + ", indexName: " + indexName
                + ", action: " + action + ", isAsynch: " + isAsynch + ", xml: " + xml);
        }

        if (action
            .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_CREATE_EMPTY_VALUE)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("request createEmpty " + indexName);
            }
            gsearchHandler.requestCreateEmpty(indexName);
            return;
        }
        else if (action
            .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_COMMIT_VALUE)) {
            gsearchHandler.requestCommitWrites(indexName);
            return;
        }

        // get Index-Parameters for resourceName
        final Map<String, Map<String, Object>> resourceParameters = getObjectTypeParameters().get(objectType);
        if (resourceParameters == null) {
            return;
        }
        final Map<String, Object> parameters = resourceParameters.get(indexName);

        if (parameters == null) {
            return;
        }

        if (action == null
            || action
                .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_UPDATE_VALUE)) {
            try {
                // check the asynch-mode
                if (parameters.get("indexAsynchronous") == null
                    || !Boolean.valueOf((String) parameters.get("indexAsynchronous")).equals(isAsynch)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("is Asynch: " + isAsynch + " and indexAsynchronous-param is "
                            + parameters.get("indexAsynchronous") + ", so returning");
                    }
                    return;
                }
            }
            catch (final Exception e) {
                throw new SystemException(e.getMessage(), e);
            }
        }

        String versionedResource = resource;
        String pidSuffix = null;
        String latestReleasedVersion = null;
        // Check if latest released version has to get indexed
        if (parameters.get("indexReleasedVersion") != null
            && (Boolean.valueOf((String) parameters.get("indexReleasedVersion")) || "both".equals(parameters
                .get("indexReleasedVersion")))) {
            // get latest released version
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("index released version, so do checks");
            }
            latestReleasedVersion =
                tripleStoreUtility.getPropertiesElements(XmlUtility.getIdFromURI(resource),
                    TripleStoreUtility.PROP_LATEST_RELEASE_NUMBER);
            final String thisVersion =
                tripleStoreUtility.getPropertiesElements(XmlUtility.getIdFromURI(resource),
                    TripleStoreUtility.PROP_LATEST_VERSION_NUMBER);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("latest released version: " + latestReleasedVersion + ", thisVersion: " + thisVersion);
            }
            if (Boolean.valueOf((String) parameters.get("indexReleasedVersion"))) {
                if (latestReleasedVersion == null || thisVersion == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("returning");
                    }
                    return;
                }
                if (!latestReleasedVersion.equals(thisVersion)) {
                    // adapt resource
                    versionedResource = resource + ':' + latestReleasedVersion;
                }
            }
            else {
                pidSuffix =
                    latestReleasedVersion == null ? Constants.LATEST_VERSION_PID_SUFFIX : latestReleasedVersion
                        .equals(thisVersion) ? Constants.LATEST_RELEASE_PID_SUFFIX : Constants.LATEST_VERSION_PID_SUFFIX;
            }
        }

        if (action == null
            || action
                .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_UPDATE_VALUE)) {
            try {
                // check if only objects with certain prerequisite
                // shall be in the index
                // (prerequisite is an xpath-Expression)
                final int prerequisite = checkPrerequisites(xml, parameters, resource, null);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("prerequisites found " + prerequisite);
                }
                if (prerequisite == Constants.DO_NOTHING) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("returning");
                    }
                    return;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Updating index " + indexName + " with " + versionedResource);
                }
                if (prerequisite == Constants.DO_DELETE) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("request deletion " + indexName + " with " + resource);
                    }
                    gsearchHandler.requestDeletion(resource, indexName, pidSuffix, commitIndex);
                }
                else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("request indexing " + indexName + " with " + versionedResource);
                    }
                    if (pidSuffix != null && pidSuffix.equals(Constants.LATEST_RELEASE_PID_SUFFIX)) {
                        gsearchHandler.requestDeletion(versionedResource, indexName,
                            Constants.LATEST_VERSION_PID_SUFFIX, commitIndex);
                    }
                    if (pidSuffix != null && pidSuffix.equals(Constants.LATEST_VERSION_PID_SUFFIX)
                        && latestReleasedVersion != null) {
                        // reindex latest released version
                        gsearchHandler.requestIndexing(versionedResource + ':' + latestReleasedVersion, indexName,
                            Constants.LATEST_RELEASE_PID_SUFFIX, (String) parameters.get("indexFulltextVisibilities"),
                            commitIndex);
                    }
                    gsearchHandler.requestIndexing(versionedResource, indexName, pidSuffix, (String) parameters
                        .get("indexFulltextVisibilities"), commitIndex);
                }
            }
            catch (final Exception e) {
                throw new SystemException(e.getMessage(), e);
            }
        }
        else if (action
            .equalsIgnoreCase(de.escidoc.core.common.business.Constants.INDEXER_QUEUE_ACTION_PARAMETER_DELETE_VALUE)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("request deletion " + indexName + ", resource " + resource);
            }
            gsearchHandler.requestDeletion(resource, indexName, pidSuffix, commitIndex);
        }
    }

    /**
     * Check prerequisite-XPaths. -if parameter prerequisites exists: -if some prerequisite-deletion-value matches, then
     * delete object from index.
     * <p/>
     * -if some prerequisite-indexing-value matches, then update object in index.
     * <p/>
     * -if parameter prerequisites doesnt exist: -update index
     *
     * @param xml        String resource-xml.
     * @param parameters parameters for resource + index.
     * @param resource   String resource-identifier.
     * @param domObject  Dom-Object that holds resource-xml.
     * @return int action to take (delete, update, nothing)
     * @throws de.escidoc.core.common.exceptions.system.SystemException
     */
    private int checkPrerequisites(
        final String xml, final Map<String, Object> parameters, final String resource, Document domObject)
        throws SystemException {
        String thisXml = xml;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("prerequisites is " + parameters.get("prerequisites"));
        }
        if (parameters.get("prerequisites") == null) {
            return Constants.DO_UPDATE;
        }
        try {
            final HashMap<String, String> prerequisites = (HashMap<String, String>) parameters.get("prerequisites");
            if (prerequisites.get("indexingPrerequisiteXpath") == null
                && prerequisites.get("deletePrerequisiteXpath") == null) {
                return Constants.DO_UPDATE;
            }
            else {
                if (thisXml == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("xml is null, requesting it from cache");
                    }
                    thisXml = indexingCacheHandler.retrieveObjectFromCache(resource);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("xml is: " + thisXml);
                }
                if (domObject == null) {
                    domObject = getXmlAsDocument(thisXml);
                }
                if (prerequisites.get("indexingPrerequisiteXpath") != null) {
                    final Node updateNode =
                        XPathAPI.selectSingleNode(domObject, prerequisites.get("indexingPrerequisiteXpath"));
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("gsearchindexing xpath-exec on DOM-Object " + " finished");
                    }
                    if (updateNode != null) {
                        return Constants.DO_UPDATE;
                    }
                }
                if (prerequisites.get("deletePrerequisiteXpath") != null) {
                    final Node deleteNode =
                        XPathAPI.selectSingleNode(domObject, prerequisites.get("deletePrerequisiteXpath"));
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("gsearchindexing xpath-exec on DOM-Object " + " finished");
                    }
                    if (deleteNode != null) {
                        return Constants.DO_DELETE;
                    }
                }
            }
        }
        catch (final TransformerException e) {
            throw new SystemException(e.getMessage(), e);
        }
        return Constants.DO_NOTHING;
    }

    /**
     * Check if the given id already exists in the given index.
     *
     * @param id         resource id
     * @param objectType String name of the resource (eg Item, Container...).
     * @param indexName  name of the index (null or "all" means to search in all indexes)
     * @return true if the resource already exists
     * @throws SystemException Thrown if a framework internal error occurs.
     */
    public boolean exists(final String id, final String objectType, final String indexName) throws SystemException {
        boolean result = false;
        final Map<String, Map<String, Object>> resourceParameters = getObjectTypeParameters().get(objectType);

        if (id != null && resourceParameters != null) {
            if (indexName == null || indexName.trim().length() == 0 || "all".equalsIgnoreCase(indexName)) {
                for (final String indexName2 : resourceParameters.keySet()) {
                    result = exists(id, indexName2);
                    if (result) {
                        break;
                    }
                }
            }
            else {
                result = exists(id, indexName);
            }
        }
        return result;
    }

    /**
     * Check if the given id already exists in the given index.
     *
     * @param id        resource id
     * @param indexName name of the index
     * @return true if the resource already exists
     * @throws SystemException Thrown if a framework internal error occurs.
     */
    private boolean exists(final String id, final String indexName) throws SystemException {
        boolean result = false;

        try {
            final StringBuilder query = new StringBuilder("");
            for (int i = 0; i < INDEX_PRIM_KEY_FIELDS.length; i++) {
                if (query.length() > 0) {
                    query.append(" or ");
                }
                query.append(INDEX_PRIM_KEY_FIELDS[i]).append('=').append(id);
            }

            final HttpResponse response =
                connectionUtility.getRequestURL(new URL(EscidocConfiguration.getInstance().get(
                    EscidocConfiguration.SRW_URL)
                    + "/search/" + indexName + "?query=" + URLEncoder.encode(query.toString(), "UTF-8")));
            final Pattern numberOfRecordsPattern = Pattern.compile("numberOfRecords>(.*?)<");

            final Matcher m = numberOfRecordsPattern.matcher(EntityUtils.toString(response.getEntity(), HTTP.UTF_8));

            if (m.find()) {
                result = Integer.parseInt(m.group(1)) > 0;
            }
        }
        catch (final IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Return all PIDs contained in given index.
     *
     * @param objectType
     * @param indexName name of the index
     * @return List of PIDs
     * @throws SystemException Thrown if a framework internal error occurs.
     */
    public Set<String> getPids(final String objectType, final String indexName) throws SystemException {
        final Map<String, Map<String, Object>> resourceParameters = getObjectTypeParameters().get(objectType);
        Set<String> result = new HashSet<String>();

        if (resourceParameters != null) {
            if (indexName == null || indexName.trim().length() == 0 || "all".equalsIgnoreCase(indexName)) {
                for (final String indexName2 : resourceParameters.keySet()) {
                    result.addAll(getPids(indexName2));
                }
            }
            else {
                result = getPids(indexName);
            }
        }
        return result;
    }

    /**
     * Return all PIDs contained in given index.
     *
     * @param indexName name of the index
     * @return List of PIDs
     * @throws SystemException Thrown if a framework internal error occurs.
     */
    private Set<String> getPids(final String indexName) throws SystemException {
        try {

            final StaxParser sp = new StaxParser();
            final SrwScanResponseHandler handler = new SrwScanResponseHandler(sp);
            sp.addHandler(handler);

            String lastTerm = "";
            boolean running = true;
            while (running) {
                handler.resetNoOfDocumentTerms();
                final HttpResponse response =
                    connectionUtility
                        .getRequestURL(new URL(EscidocConfiguration.getInstance().get(EscidocConfiguration.SRW_URL)
                            + "/search/" + indexName
                            + Constants.SRW_TERM_PATTERN.matcher(SRW_QUERY).replaceFirst(lastTerm)));
                final String lastLastTerm = handler.getLastTerm();
                sp.parse(new ByteArrayInputStream(EntityUtils.toByteArray(response.getEntity())));
                lastTerm = handler.getLastTerm();
                if (handler.getNoOfDocumentTerms() == 0) {
                    running = false;
                }
                else if (lastTerm.equals(lastLastTerm)) {
                    throw new SystemException("duplicate PID in Scan Operation");
                }
            }
            return handler.getTerms();
        }
        catch (final IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
        catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    /**
     * Get Names of available indexes from gsearch-config.
     *
     * @return COllection with indexNames
     * @throws IOException e
     * @throws de.escidoc.core.common.exceptions.system.ApplicationServerSystemException
     */
    private Iterable<String> getIndexNames() throws ApplicationServerSystemException {
        if (this.indexNames == null) {
            // Get index names from gsearch-config
            final Map<String, Map<String, String>> indexConfig = gsearchHandler.getIndexConfigurations();
            this.indexNames = new ArrayList<String>();
            indexNames.addAll(indexConfig.keySet());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("configured indexNames: " + indexConfig.keySet());
            }
        }
        return this.indexNames;
    }

    /**
     * Get configuration for available indexes.
     *
     * @throws IOException     e
     * @throws SystemException e
     */
    private void getIndexConfigs() throws IOException, SystemException {
        // Build IndexInfo HashMap
        this.objectTypeParameters = new HashMap<String, Map<String, Map<String, Object>>>();
        final String searchPropertiesDirectory =
            EscidocConfiguration.getInstance().get(EscidocConfiguration.SEARCH_PROPERTIES_DIRECTORY);
        for (final String indexName : getIndexNames()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getting configuration for index " + indexName);
            }
            final Properties indexProps = new Properties();
            InputStream propStream = null;
            try {
                try {
                    propStream =
                        getInputStream('/' + searchPropertiesDirectory + "/index/" + indexName
                            + "/index.object-types.properties");
                }
                catch (IOException e) {
                    propStream =
                        IndexingHandler.class.getResourceAsStream('/' + searchPropertiesDirectory + "/index/"
                            + indexName + "/index.object-types.properties");
                }
                if (propStream == null) {
                    throw new SystemException(searchPropertiesDirectory + "/index/" + indexName
                        + "/index.object-types.properties " + "not found");
                }
                indexProps.load(propStream);
            }
            finally {
                IOUtils.closeStream(propStream);
            }
            final Pattern objectTypePattern = Pattern.compile(".*?\\.(.*?)\\..*");
            final Matcher objectTypeMatcher = objectTypePattern.matcher("");
            final Collection<String> objectTypes = new HashSet<String>();
            for (final Object o : indexProps.keySet()) {
                final String key = (String) o;
                if (key.startsWith("Resource")) {
                    final String propVal = indexProps.getProperty(key);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("found property " + key + ':' + propVal);
                    }
                    objectTypeMatcher.reset(key);
                    if (!objectTypeMatcher.matches()) {
                        throw new IOException(key + " is not a supported property");
                    }
                    final String objectType =
                        de.escidoc.core.common.business.Constants.RESOURCES_NS_URI + objectTypeMatcher.group(1);
                    if (objectTypeParameters.get(objectType) == null) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("initializing HashMap for objectType " + objectType);
                        }
                        objectTypeParameters.put(objectType, new HashMap<String, Map<String, Object>>());
                    }
                    if (objectTypeParameters.get(objectType).get(indexName) == null) {
                        objectTypeParameters.get(objectType).put(indexName, new HashMap<String, Object>());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("adding " + indexName + " to " + objectType);
                        }
                        objectTypes.add(objectType);
                    }
                    if (key.contains("Prerequisite")) {
                        if (objectTypeParameters.get(objectType).get(indexName).get("prerequisites") == null) {
                            objectTypeParameters.get(objectType).get(indexName).put("prerequisites",
                                new HashMap<String, String>());
                        }
                        ((Map<String, String>) objectTypeParameters.get(objectType).get(indexName).get("prerequisites"))
                            .put(key.replaceFirst(".*\\.", ""), propVal);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("adding prerequisite " + key + ':' + propVal);
                        }
                    }
                    else {
                        objectTypeParameters.get(objectType).get(indexName).put(key.replaceFirst(".*\\.", ""), propVal);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("adding parameter " + key + ':' + propVal);
                        }
                    }
                }
            }
            for (final String objectType : objectTypes) {
                if (objectTypeParameters.get(objectType).get(indexName).get("indexAsynchronous") == null) {
                    objectTypeParameters.get(objectType).get(indexName).put("indexAsynchronous", "false");
                }
                if (objectTypeParameters.get(objectType).get(indexName).get("indexReleasedVersion") == null) {
                    objectTypeParameters.get(objectType).get(indexName).put("indexReleasedVersion", "false");
                }
            }
        }
    }

    /**
     * Get XML-String as Dom-Document representation.
     *
     * @param xml xml
     * @return Document xml as dom-Document
     * @throws de.escidoc.core.common.exceptions.system.SystemException
     */
    private Document getXmlAsDocument(final String xml) throws SystemException {
        try {
            final InputStream in = new ByteArrayInputStream(xml.getBytes(XmlUtility.CHARACTER_ENCODING));
            return docBuilder.parse(new InputSource(in));
        }
        catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    /**
     * @return the objectTypeParameters
     * @throws WebserverSystemException e
     */
    public Map<String, Map<String, Map<String, Object>>> getObjectTypeParameters() throws WebserverSystemException {
        if (this.objectTypeParameters == null) {
            try {
                getIndexConfigs();
            }
            catch (final Exception e) {
                throw new WebserverSystemException(e.getMessage(), e);
            }
        }
        return this.objectTypeParameters;
    }

    /**
     * Get an InputStream for the given file.
     *
     * @param filename The name of the file.
     * @return The InputStream or null if the file could not be located.
     * @throws FileNotFoundException If access to the specified file fails.
     */
    private static InputStream getInputStream(final String filename) throws IOException {
        final ResourcePatternResolver applicationContext = new ClassPathXmlApplicationContext(new String[] {});
        String escidocHome = System.getenv("ESCIDOC_HOME");
        if (escidocHome == null) {
            escidocHome = System.getProperty("ESCIDOC_HOME");
        }
        final Resource[] resource = applicationContext.getResources("file:///" + escidocHome + "/conf/" + filename);
        if (resource.length == 0) {
            throw new FileNotFoundException("Unable to find file '" + filename + "' in classpath.");
        }
        return resource[0].getInputStream();
    }

}
