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
package de.escidoc.core.common.util.stax.handler;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.PropertyMapKeys;
import de.escidoc.core.common.exceptions.system.IntegritySystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
import de.escidoc.core.common.util.logger.AppLogger;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.xml.Elements;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.stax.events.Attribute;
import de.escidoc.core.common.util.xml.stax.events.EndElement;
import de.escidoc.core.common.util.xml.stax.events.StartElement;
import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;

import javax.naming.directory.NoSuchAttributeException;
import java.util.HashMap;
import java.util.Map;

/**
 * Read all (currently used) values from the Whole Object Versioning data
 * stream.
 * 
 * FIXME replace the key-names in the propertiesValueMap (versionData) to
 * constants from TripleStoreUtility to get a constant value mapping.
 * 
 */
public class WovReadHandler extends DefaultHandler {

    private boolean inside = false;

    private int insideLevel = 0;

    private final StaxParser parser;

    private String versionId = null;

    private String versionCreatedDate = null;

    private boolean inLatestVersion = false;

    private boolean inCertainVersion = false;

    private boolean isParsed = false;

    /**
     * Values of the certain version. If the certain version is the latest
     * version, the are all latest version values set also.
     */
    private final Map<String, String> versionData = new HashMap<String, String>();

    private String curEventDate;

    private static final String ELEMENT_PATH = "/version-history/version";

    private boolean certainVersionIsLatestVersion = true;

    /**
     * WOV Read Handler.
     * 
     * @param parser
     *            Parser.
     * @param versionNumber
     *            The number of the object version.
     * @throws WebserverSystemException
     *             Thrown in case of internal error.
     */
    public WovReadHandler(final StaxParser parser, final String versionNumber)
        throws WebserverSystemException {

        this.parser = parser;
        this.versionId = versionNumber;
    }

    /**
     * WOV Read Handler.
     * 
     * @param parser
     *            Parser.
     * @throws WebserverSystemException
     *             Thrown in case of internal error.
     */
    public WovReadHandler(final StaxParser parser)
        throws WebserverSystemException {

        this.parser = parser;
    }

    /**
     * Get version data.
     * 
     * @return version data
     */
    public final Map<String, String> getVersionData() {
        return this.versionData;
    }

    @Override
    public StartElement startElement(StartElement element)
        throws IntegritySystemException {
        if (!isParsed) {
            if (ELEMENT_PATH.equals(parser.getCurPath())) {
                inside = true;
                insideLevel++;
                if (versionId != null) {

                    String objectId = getObjId(element);
                    if (objectId.endsWith(':' + versionId)) {
                        inCertainVersion = true;
                        try {
                            this.versionCreatedDate =
                                element
                                    .getAttribute(null, "timestamp").getValue();
                            versionData.put(
                                PropertyMapKeys.CURRENT_VERSION_VERSION_DATE,
                                this.versionCreatedDate);
                        }
                        catch (NoSuchAttributeException e) {
                            throw new IntegritySystemException(
                                "Version entry has no timestamp.", e);
                        }
                    }
                    else {
                        this.certainVersionIsLatestVersion = false;
                    }
                }
                else {
                    inLatestVersion = true;
                }
            }
            else if (inside) {
                String theName = element.getLocalName();
                if (theName.equals(Elements.ELEMENT_WOV_EVENT_USER)) {
                    if (certainVersionIsLatestVersion) {
                        setLatestVersionValues(theName, null, element);
                        setCurrentVersionValues(theName, null, element);
                    }
                    else if (inLatestVersion) {
                        setLatestVersionValues(theName, null, element);
                        setCurrentVersionValues(theName, null, element);
                    }
                    else if (inCertainVersion) {
                        setCurrentVersionValues(theName, null, element);
                    }
                }
                insideLevel++;
            }
        }
        return null;
    }

    @Override
    public EndElement endElement(EndElement element) {
        if ((!isParsed) && (inside)) {
            insideLevel--;

            if (insideLevel == 0) {
                inside = false;
                if (inLatestVersion) {
                    this.inLatestVersion = false;
                    isParsed = true;
                    return null;
                }
                if (inCertainVersion) {
                    this.inCertainVersion = false;
                    isParsed = true;
                    return null;
                }

            }
        }
        return null;
    }

    @Override
    public String characters(final String s, StartElement element)
        throws IntegritySystemException, XmlParserSystemException {

        if (!isParsed) {
            String theName = element.getLocalName();
            if (certainVersionIsLatestVersion) {
                setLatestVersionValues(theName, s, element);
                setCurrentVersionValues(theName, s, element);
            }
            else if (inLatestVersion) {
                setLatestVersionValues(theName, s, element);
                setCurrentVersionValues(theName, s, element);
            }
            else if (inCertainVersion) {
                setCurrentVersionValues(theName, s, element);
            }

        }
        return null;
    }

    /**
     * 
     * @param theName
     *            The element name
     * @param s
     *            The element value
     * @param element
     *            The element
     * @throws IntegritySystemException
     *             Thrown if required values are missed.
     */
    private void setLatestVersionValues(
        final String theName, final String s, final StartElement element)
        throws IntegritySystemException {

        if (theName.equals(Elements.ELEMENT_WOV_EVENT_DATE)) {
            this.curEventDate = s;
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_TIMESTAMP)) {
            versionData.put(PropertyMapKeys.LATEST_VERSION_DATE, s);
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_NUMBER)) {
            versionData.put(PropertyMapKeys.LATEST_VERSION_NUMBER, s);
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_STATUS)) {
            versionData.put(PropertyMapKeys.LATEST_VERSION_VERSION_STATUS, s);
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_COMMENT)) {
            versionData.put(PropertyMapKeys.LATEST_VERSION_COMMENT, XmlUtility
                .escapeForbiddenXmlCharacters(s));
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_PID)) {
            versionData.put(PropertyMapKeys.LATEST_VERSION_PID, s);
        }
        else if (this.curEventDate != null
            && this.curEventDate.equals(versionData
                .get(PropertyMapKeys.LATEST_VERSION_DATE))) {
            // found user in event related to version by timestamp
            // retrievable are
            // TODO
            if (theName.equals(Elements.ELEMENT_WOV_EVENT_USER_ID)) {
                versionData.put(PropertyMapKeys.LATEST_VERSION_MODIFIED_BY_ID,
                    s);
            }
            else if (theName.equals(Elements.ELEMENT_WOV_EVENT_USER)) {
                try {
                    versionData.put(
                        PropertyMapKeys.LATEST_VERSION_MODIFIED_BY_TITLE,
                        element.getAttribute(Constants.XLINK_NS_URI,
                            Elements.ATTRIBUTE_XLINK_TITLE).getValue());
                    versionData.put(
                        PropertyMapKeys.LATEST_VERSION_MODIFIED_BY_HREF,
                        element.getAttribute(Constants.XLINK_NS_URI,
                            Elements.ATTRIBUTE_XLINK_HREF).getValue());
                }
                catch (NoSuchAttributeException e) {
                    throw new IntegritySystemException(
                        "Missing xlink attribute in version history event user.", e);
                }
            }
        }
    }

    /**
     * Set the values to the version properties map. The current version key
     * elements are used to be consistent with the TripleStore!
     * 
     * @param theName
     *            The element name
     * @param s
     *            The element value
     * @param element
     *            The element
     * @throws IntegritySystemException
     *             Thrown if required values are missed.
     */
    private void setCurrentVersionValues(
        final String theName, final String s, final StartElement element)
        throws IntegritySystemException {

        if (theName.equals(Elements.ELEMENT_WOV_EVENT_DATE)) {
            this.curEventDate = s;
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_TIMESTAMP)) {
            versionData.put(PropertyMapKeys.CURRENT_VERSION_VERSION_DATE, s);
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_NUMBER)) {
            versionData.put(PropertyMapKeys.CURRENT_VERSION_VERSION_NUMBER, s);
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_STATUS)) {
            versionData.put(PropertyMapKeys.CURRENT_VERSION_STATUS, s);
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_COMMENT)) {
            versionData.put(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT,
                XmlUtility.escapeForbiddenXmlCharacters(s));
        }
        else if (theName.equals(Elements.ELEMENT_WOV_VERSION_PID)) {
            versionData.put(PropertyMapKeys.CURRENT_VERSION_PID, s);
        }
        else if (this.curEventDate != null
            && this.curEventDate.equals(versionData
                .get(PropertyMapKeys.CURRENT_VERSION_VERSION_DATE))) {
            // found user in event related to version by timestamp
            // retrievable are
            // TODO
            if (theName.equals(Elements.ELEMENT_WOV_EVENT_USER_ID)) {
                versionData.put(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID,
                    s);
            }
            else if (theName.equals(Elements.ELEMENT_WOV_EVENT_USER)) {
                try {
                    versionData.put(
                        PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE,
                        element.getAttribute(Constants.XLINK_NS_URI,
                            Elements.ATTRIBUTE_XLINK_TITLE).getValue());
                    versionData.put(
                        PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_HREF,
                        element.getAttribute(Constants.XLINK_NS_URI,
                            Elements.ATTRIBUTE_XLINK_HREF).getValue());
                }
                catch (NoSuchAttributeException e) {
                    throw new IntegritySystemException(
                        "Missing xlink attribute in version history event user.", e);
                }
            }
        }
    }

    /**
     * Get the objid from element node.
     * 
     * @param element
     *            Element
     * @return objid
     * @throws IntegritySystemException
     *             Thrown if href attribute is missing.
     */
    private static String getObjId(final StartElement element)
        throws IntegritySystemException {
        Attribute objectId;
        try {
            objectId = element.getAttribute(Constants.XLINK_URI, "href");
        }
        catch (NoSuchAttributeException e) {
            throw new IntegritySystemException("Version entry has no objid.", e);
        }
        return objectId.getValue();
    }
}
