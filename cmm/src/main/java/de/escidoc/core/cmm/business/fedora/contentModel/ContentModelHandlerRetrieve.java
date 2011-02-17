package de.escidoc.core.cmm.business.fedora.contentModel;

import de.escidoc.core.common.business.PropertyMapKeys;
import de.escidoc.core.common.business.fedora.Constants;
import de.escidoc.core.common.business.fedora.HandlerBase;
import de.escidoc.core.common.business.fedora.datastream.Datastream;
import de.escidoc.core.common.business.fedora.resources.cmm.ContentModel;
import de.escidoc.core.common.business.fedora.resources.cmm.DsTypeModel;
import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
import de.escidoc.core.common.exceptions.system.EncodingSystemException;
import de.escidoc.core.common.exceptions.system.FedoraSystemException;
import de.escidoc.core.common.exceptions.system.IntegritySystemException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
import de.escidoc.core.common.util.date.Iso8601Util;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.factory.ContentModelXmlProvider;
import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ContentModelHandlerRetrieve extends HandlerBase {

    private ContentModel contentModel;

    // TODO ContentModelHandlerBase ?
    public ContentModel getContentModel() {
        return this.contentModel;
    }

    // TODO ContentModelHandlerBase ?
    protected void setContentModel(final String id)
        throws ContentModelNotFoundException, SystemException {

        try {
            this.contentModel = new ContentModel(id);
        }
        catch (final StreamNotFoundException e) {
            throw new ContentModelNotFoundException(e.getMessage(), e);
        }
        catch (final ResourceNotFoundException e) {
            throw new ContentModelNotFoundException(e.getMessage(), e);
        }
    }

    // TODO ContentHandlerRetrieve ?
    protected String render() throws WebserverSystemException,
        ContentModelNotFoundException, TripleStoreSystemException,
        IntegritySystemException, XmlParserSystemException,
        EncodingSystemException, FedoraSystemException {

        String result = null;
        Map<String, Object> values = new HashMap<String, Object>();

        values.putAll(getCommonValues(getContentModel()));
        values.putAll(getPropertiesValues(getContentModel()));
        values.putAll(getMdRecordDefinitionsValues(getContentModel()));
        values.putAll(getResourceDefinitionsValues(getContentModel()));
        values.put(XmlTemplateProvider.CONTENT_STREAMS,
            renderContentStreams(false));
        values.putAll(getResourcesValues(getContentModel()));

        result =
            ContentModelXmlProvider.getInstance().getContentModelXml(values);
        return result;
    }

    // TODO ContentHandlerRetrieve ?
    protected String renderProperties() throws ContentModelNotFoundException,
        WebserverSystemException, TripleStoreSystemException,
        IntegritySystemException, XmlParserSystemException,
        EncodingSystemException, FedoraSystemException {

        Map<String, String> values = getCommonValues(getContentModel());
        values.putAll(getPropertiesValues(getContentModel()));
        values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
            XmlTemplateProvider.TRUE);
        return ContentModelXmlProvider
            .getInstance().getContentModelPropertiesXml(values);
    }

    // TODO ContentHandlerRetrieve ?
    protected String renderResources() throws ContentModelNotFoundException,
        WebserverSystemException, TripleStoreSystemException,
        IntegritySystemException, XmlParserSystemException,
        EncodingSystemException, FedoraSystemException {

        Map<String, String> values = getCommonValues(getContentModel());
        values.putAll(getResourcesValues(getContentModel()));
        values.put(XmlTemplateProvider.IS_ROOT_RESOURCES,
            XmlTemplateProvider.TRUE);
        return ContentModelXmlProvider
            .getInstance().getContentModelResourcesXml(values);
    }

    // TODO ContentHandlerRetrieve ?
    protected String renderResourceDefinitions() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    // TODO ContentHandlerRetrieve ?
    protected String renderResourceDefinition(final String name) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    // TODO ContentModelHandlerRetrieve ?
    protected String renderContentStreams(final boolean isRoot)
        throws WebserverSystemException, EncodingSystemException,
        FedoraSystemException, IntegritySystemException,
        TripleStoreSystemException {

        Map<String, Object> values = new HashMap<String, Object>();

        Map<String, String> commonValues = getCommonValues(getContentModel());
        values.putAll(commonValues);

        StringBuffer content = new StringBuffer();
        for (String s : ((HashMap<String, Datastream>) getContentModel()
                .getContentStreams()).keySet()) {
            String contentStreamName = s;
            content.append(renderContentStream(contentStreamName, false));
        }
        values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_HREF,
            getContentModel().getHref() + Constants.CONTENT_STREAMS_URL_PART);
        values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_TITLE,
            "Content streams of Item " + getContentModel().getId());

        if (content.length() == 0) {
            return "";
        }

        if (isRoot) {
            values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                XmlTemplateProvider.TRUE);
        }
        values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_CONTENT,
            content.toString());
        return ContentModelXmlProvider.getInstance().getContentStreamsXml(
            values);

    }

    // TODO ContentModelHandlerRetrieve ?
    protected String renderContentStream(final String name, final boolean isRoot)
        throws WebserverSystemException, IntegritySystemException,
        TripleStoreSystemException {

        Map<String, Object> values = new HashMap<String, Object>();

        Datastream ds = null;

        if (isRoot) {
            values.put("isRootContentStream", XmlTemplateProvider.TRUE);
        }
        Map<String, String> commonValues = getCommonValues(getContentModel());
        values.putAll(commonValues);

        ds = getContentModel().getContentStream(name);

        values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_NAME, ds.getName());
        values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_TITLE, ds.getLabel());
        String location = ds.getLocation();
        if (ds.getControlGroup().equals("M")
            || ds.getControlGroup().equals("X")) {
            values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_STORAGE,
                Constants.STORAGE_INTERNAL_MANAGED);
            location =
                getContentModel().getHref() + Constants.CONTENT_STREAM_URL_PART
                    + "/" + ds.getName()
                    + Constants.CONTENT_STREAM_CONTENT_URL_EXTENSION;
            if (ds.getControlGroup().equals("X")) {
                try {
                    values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_CONTENT,
                        ds.toStringUTF8());
                }
                catch (EncodingSystemException e) {
                    throw new WebserverSystemException(e);
                }
            }
        }
        else if (ds.getControlGroup().equals("E")) {
            values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_STORAGE,
                Constants.STORAGE_EXTERNAL_MANAGED);
            location =
                getContentModel().getHref() + Constants.CONTENT_STREAM_URL_PART
                    + "/" + ds.getName()
                    + Constants.CONTENT_STREAM_CONTENT_URL_EXTENSION;

        }
        else if (ds.getControlGroup().equals("R")) {
            values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_STORAGE,
                Constants.STORAGE_EXTERNAL_URL);
        }
        values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_MIME_TYPE,
            ds.getMimeType());
        values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_HREF, location);

        return ContentModelXmlProvider
            .getInstance().getContentStreamXml(values);
    }

    /*
     * Values for rendering
     */

    // TODO ContentHandlerRetrieve ?
    /**
     * Get Common values from ContentModel.
     * 
     * @param contentModel
     *            The ContentModel.
     * @return Map with common ContentModel values.
     * 
     * @throws WebserverSystemException
     *             Thrown if values extracting failed.
     */
    private Map<String, String> getCommonValues(final ContentModel contentModel)
        throws WebserverSystemException {

        Map<String, String> values = new HashMap<String, String>();

        values.put(XmlTemplateProvider.OBJID, getContentModel().getId());
        values.put(XmlTemplateProvider.TITLE, getContentModel().getTitle());

        values.put(XmlTemplateProvider.HREF, getContentModel().getHref());

        try {
            values.put(XmlTemplateProvider.VAR_LAST_MODIFICATION_DATE,
                Iso8601Util.getIso8601(Iso8601Util.parseIso8601(contentModel
                    .getLastModificationDate())));
        }
        catch (ParseException e) {
            try {
                throw new WebserverSystemException(
                    "Unable to parse last-modification-date '"
                        + contentModel.getLastModificationDate()
                        + "' of content model '" + contentModel.getId() + "'!",
                    e);
            }
            catch (FedoraSystemException e1) {
                throw new WebserverSystemException(e1);
            }
        }
        catch (FedoraSystemException e) {
            throw new WebserverSystemException(e);
        }

        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_NAMESPACE_PREFIX,
                de.escidoc.core.common.business.Constants.CONTENT_MODEL_NAMESPACE_PREFIX);
        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_NAMESPACE,
                de.escidoc.core.common.business.Constants.CONTENT_MODEL_NAMESPACE_URI);
        values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL,
            XmlUtility.getEscidocBaseUrl());
        values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE_PREFIX,
            de.escidoc.core.common.business.Constants.XLINK_NS_PREFIX);
        values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE,
            de.escidoc.core.common.business.Constants.XLINK_NS_URI);

        values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS,
            de.escidoc.core.common.business.Constants.PROPERTIES_NS_URI);
        values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS_PREFIX,
            de.escidoc.core.common.business.Constants.PROPERTIES_NS_PREFIX);
        values
            .put(
                XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
                de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
        values
            .put(
                XmlTemplateProvider.ESCIDOC_SREL_NS,
                de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_URI);
        values.put("versionNamespacePrefix",
            de.escidoc.core.common.business.Constants.VERSION_NS_PREFIX);
        values.put("versionNamespace",
            de.escidoc.core.common.business.Constants.VERSION_NS_URI);
        values.put("releaseNamespacePrefix",
            de.escidoc.core.common.business.Constants.RELEASE_NS_PREFIX);
        values.put("releaseNamespace",
            de.escidoc.core.common.business.Constants.RELEASE_NS_URI);

        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_STREAM_NS_PREFIX,
                de.escidoc.core.common.business.Constants.CONTENT_STREAMS_NAMESPACE_PREFIX);
        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_STREAM_NS,
                de.escidoc.core.common.business.Constants.CONTENT_STREAMS_NAMESPACE_URI);

        return values;
    }

    // TODO ContentModelHandlerRetrieve ?
    /**
     * Prepare properties values from content model resource as velocity values.
     * 
     * @param contentModel
     *            The Content Model.
     * @return Map with properties values (for velocity template)
     * 
     * @throws TripleStoreSystemException
     * @throws WebserverSystemException
     * @throws IntegritySystemException
     * @throws XmlParserSystemException
     * @throws EncodingSystemException
     * @throws FedoraSystemException
     * @throws ContentModelNotFoundException
     */
    private Map<String, String> getPropertiesValues(
        final ContentModel contentModel) throws TripleStoreSystemException,
        WebserverSystemException, IntegritySystemException,
        XmlParserSystemException, EncodingSystemException,
        FedoraSystemException, ContentModelNotFoundException {

        // retrieve properties from resource (the resource decided where are the
        // data to load, TripleStore or Wov)

        Map<String, String> properties = contentModel.getResourceProperties();

        Map<String, String> values = new HashMap<String, String>();

        values.put(XmlTemplateProvider.VAR_PROPERTIES_TITLE, "Properties");
        values.put(XmlTemplateProvider.VAR_PROPERTIES_HREF,
            contentModel.getHref() + Constants.PROPERTIES_URL_PART);

        // FIXME description not in map? (FRS)
        String debug = contentModel.getDescription();
        // properties.get(PropertyMapKeys.LATEST_VERSION_DESCRIPTION);
        values.put(XmlTemplateProvider.VAR_DESCRIPTION, debug);

        try {
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CREATION_DATE,
                contentModel.getCreationDate());
        }
        catch (TripleStoreSystemException e) {
            throw new ContentModelNotFoundException(e);
        }

        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CREATED_BY_TITLE,
            properties.get(PropertyMapKeys.CREATED_BY_TITLE));
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CREATED_BY_HREF,
            de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                + properties.get(PropertyMapKeys.CREATED_BY_ID));
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CREATED_BY_ID,
            properties.get(PropertyMapKeys.CREATED_BY_ID));

        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_STATUS,
            contentModel.getStatus());
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_STATUS_COMMENT,
            properties.get(PropertyMapKeys.PUBLIC_STATUS_COMMENT));

        if (contentModel.hasObjectPid()) {
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_OBJECT_PID,
                contentModel.getObjectPid());
        }

        if (contentModel.isLocked()) {
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_STATUS,
                de.escidoc.core.common.business.Constants.STATUS_LOCKED);
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_DATE,
                contentModel.getLockDate());
            String lockOwnerId = contentModel.getLockOwner();
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_OWNER_ID,
                lockOwnerId);
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_OWNER_HREF,
                de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                    + lockOwnerId);
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_OWNER_TITLE,
                contentModel.getLockOwnerTitle());
        }
        else {
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LOCK_STATUS,
                de.escidoc.core.common.business.Constants.STATUS_UNLOCKED);
        }

        // version
        final StringBuffer versionIdBase =
            new StringBuffer(contentModel.getId()).append(":");

        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_HREF,
            contentModel.getVersionHref());
        // de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE
        // + currentVersionId);
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_ID,
            contentModel.getFullId());
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_TITLE,
            "This Version");
        values.put(
            XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_NUMBER,
            contentModel.getVersionId());
        // properties.get(TripleStoreUtility.PROP_CURRENT_VERSION_NUMBER));
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_DATE,
            contentModel.getVersionDate());
        // properties.get(TripleStoreUtility.PROP_VERSION_DATE));
        values.put(
            XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_STATUS,
            contentModel.getVersionStatus());
        // properties.get(TripleStoreUtility.PROP_CURRENT_VERSION_STATUS));
        values.put(
            XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_VALID_STATUS,
            properties.get(PropertyMapKeys.CURRENT_VERSION_VALID_STATUS));
        values.put(
            XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_COMMENT,
            properties.get(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT));

        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_MODIFIED_BY_ID,
                properties.get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_MODIFIED_BY_TITLE,
                properties
                    .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));

        // href is rest only value
        values
            .put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_CURRENT_VERSION_MODIFIED_BY_HREF,
                de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                    + properties
                        .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));

        // PID ---------------------------------------------------
        if (contentModel.hasVersionPid()) {
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_VERSION_PID,
                contentModel.getVersionPid());
        }

        String latestVersionId = contentModel.getLatestVersionId();
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_VERSION_HREF,
            de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE
                + latestVersionId);
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_VERSION_TITLE,
            "Latest Version");
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_VERSION_ID,
            latestVersionId);
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_VERSION_NUMBER,
            properties.get(PropertyMapKeys.LATEST_VERSION_NUMBER));
        values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_VERSION_DATE,
            properties.get(PropertyMapKeys.LATEST_VERSION_DATE));

        // if contentModel is released
        // -------------------------------------------------
        if (properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER) != null) {

            values.put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_RELEASE_NUMBER,
                properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));

            // ! changes versionIdBase
            final String latestRevisonId =
                versionIdBase
                    .append(
                        properties
                            .get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER))
                    .toString();
            values
                .put(
                    XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_RELEASE_HREF,
                    de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE
                        + latestRevisonId);
            values.put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_RELEASE_TITLE,
                "Latest public version");
            values.put(XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_RELEASE_ID,
                latestRevisonId);
            values.put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_RELEASE_DATE,
                properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_DATE));

            String latestReleasePid = contentModel.getLatestReleasePid();
            if (latestReleasePid != null) {
                values.put(
                    XmlTemplateProvider.VAR_CONTENT_MODEL_LATEST_RELEASE_PID,
                    latestReleasePid);
            }
        }

        return values;
    }

    // TODO ContentModelHandlerRetrieve ?
    private Map<String, Object> getMdRecordDefinitionsValues(
        final ContentModel contentModel) throws IntegritySystemException,
        WebserverSystemException {

        // get name and schema-href for every md-record datastream definition
        // from DC_COMPOSITE datastream

        // <dsCompositeModel
        // xmlns="info:fedora/fedora-system:def/dsCompositeModel#"
        // xmlns:schema="http://ecm.sourceforge.net/types/dscompositeschema/0/1/#">
        // <dsTypeModel ID="escidoc">
        // <form MIME="text/xml"/>
        // <extensions name="SCHEMA">
        // <schema:schema type="xsd" datastream="escidoc_xsd"/>
        // </extensions>
        // </dsTypeModel>
        // </dsCompositeModel>

        Map<String, Object> values = new HashMap<String, Object>();
        List<Map<String, String>> mdRecordDefinitions =
            new ArrayList<Map<String, String>>();

        // get dsTypeModel/@ID entries from datastream DS-COMPOSITE-MODEL
        List<DsTypeModel> datastreamEntries =
            getContentModel().getMdRecordDefinitionIDs();

        if (datastreamEntries != null) {
            for (DsTypeModel datastreamEntry1 : datastreamEntries) {
                DsTypeModel datastreamEntry = datastreamEntry1;

                Map<String, String> mdRecordDefinition =
                        new HashMap<String, String>();
                mdRecordDefinition.put("name", datastreamEntry.getName());
                if (datastreamEntry.hasSchema()) {
                    mdRecordDefinition
                            .put(
                                    "schemaHref",
                                    de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE
                                            + getContentModel().getId()
                                            + "/md-record-definitions/md-record-definition/"
                                            + datastreamEntry.getName() + "/schema/content");
                }

                mdRecordDefinitions.add(mdRecordDefinition);
            }
        }

        if (!mdRecordDefinitions.isEmpty()) {
            values.put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_MDRECORD_DEFINITIONS,
                mdRecordDefinitions);
        }

        return values;
    }

    // TODO ContentModelHandlerRetrieve ?
    private Map<String, Object> getResourceDefinitionsValues(
        final ContentModel contentModel) throws TripleStoreSystemException,
        WebserverSystemException {

        // for every xslt service definition bound to this content model
        // get the operation name and create a URL for the datastream "xslt"
        // (while create or update the URL of the XSLT may point to extern, then
        // the XSLT is retrieved and stored in the service definition object)

        Map<String, Object> values = new HashMap<String, Object>();
        List<Map<String, String>> resourceDefinitions =
            new ArrayList<Map<String, String>>();

        // FIXME do not use triplestore

        List<String> methodNames = new ArrayList<String>();
        // <info:fedora/fedora-system:def/model#hasService>
        List<String> sdefs =
            getTripleStoreUtility().getPropertiesElementsVector(
                getContentModel().getId(),
                "info:fedora/fedora-system:def/model#hasService");
        // <info:fedora/fedora-system:def/model#definesMethod>
        // and
        // TODO <http://escidoc.de/core/01/tmp/transforms>
        for (String sdef : sdefs) {
            methodNames.add(getTripleStoreUtility().getProperty(
                    sdef,
                    "info:fedora/fedora-system:def/model#definesMethod"));
        }

        if (!methodNames.isEmpty()) {
            for (String methodName1 : methodNames) {
                String methodName = methodName1;

                Map<String, String> resourceDefinition =
                        new HashMap<String, String>();
                resourceDefinition.put("name", methodName);
                resourceDefinition.put("xsltHref", getContentModel().getHref()
                        + "/resource-definitions/resource-definition/" + methodName
                        + "/xslt/content");
                // FIXME get from service deployment
                // <http://escidoc.de/core/01/tmp/transforms>
                // it's just a name or
                resourceDefinition.put("mdRecordName", "escidoc");
                // it's a URL
                // resourceDefinition.put("xmlHref", "http://xml.to/transform");

                resourceDefinitions.add(resourceDefinition);
            }
        }

        if (!resourceDefinitions.isEmpty()) {
            values.put(
                XmlTemplateProvider.VAR_CONTENT_MODEL_RESOURCE_DEFINITIONS,
                resourceDefinitions);
        }

        return values;
    }

    // TODO ContentModelHandlerRetrieve ?
    private Map<String, String> getResourcesValues(
        final ContentModel contentModel) throws WebserverSystemException {

        Map<String, String> values = new HashMap<String, String>();
        values.put(XmlTemplateProvider.RESOURCES_TITLE, "Resources");
        values.put("resourcesHref", contentModel.getHref() + "/resources");

        return values;
    }
}
