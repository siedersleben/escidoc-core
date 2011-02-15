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
package de.escidoc.core.om.business.stax.handler.container;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.business.fedora.Utility;
import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.logger.AppLogger;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.xml.stax.events.Attribute;
import de.escidoc.core.common.util.xml.stax.events.StartElement;
import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Creating Struct Maps.
 * 
 * @author FRS
 * 
 * @om
 */
public class StructMapCreateHandler extends DefaultHandler {

    private final StaxParser parser;

    private final String structMapPath;

    private final List<String> entries = new ArrayList<String>();

    private static AppLogger logger =
        new AppLogger(StructMapCreateHandler.class.getName());

    /**
     * Struct Map Handler.
     * 
     * @param structMapPath
     *            XPath of struct-map
     * @param parser
     *            StAX parser.
     */
    public StructMapCreateHandler(final String structMapPath,
        final StaxParser parser) {
        this.structMapPath = structMapPath;
        this.parser = parser;
    }

    /**
     * Struct Map Handler.
     * 
     * @param parser
     *            StAX parser.
     */
    public StructMapCreateHandler(final StaxParser parser) {
        this.structMapPath = "/container/struct-map";
        this.parser = parser;
    }

    /**
     * StAX startElement.
     * 
     * @param element
     *            StAX startElement
     * @return StartElement
     * 
     * @throws InvalidContentException
     *             Thrown if content has invalid values.
     * @throws TripleStoreSystemException
     *             Thrown if request of TripleStore failed.
     * @throws WebserverSystemException
     *             Thrown in case of internal error.
     * @throws MissingAttributeValueException
     *             Thrown if objid was not given as parameter.
     */
    @Override
    public StartElement startElement(final StartElement element)
        throws InvalidContentException, TripleStoreSystemException,
        WebserverSystemException, MissingAttributeValueException {
        String curPath = parser.getCurPath();

        if (curPath.startsWith(structMapPath)) {
            if (curPath.equals(structMapPath + "/item")) {
                String itemId =
                    checkRefElement(element, Constants.ITEM_OBJECT_TYPE, "item");
                entries.add(itemId);
            }
            else if (curPath.equals(structMapPath + "/container")) {
                String containerId =
                    checkRefElement(element, Constants.CONTAINER_OBJECT_TYPE,
                        "container");
                entries.add(containerId);
            }

        }

        return element;
    }

    /**
     * Check if entries of struct-map are valid references.
     * 
     * @param element
     *            StAX start element.
     * @param objectType
     *            Type of resource (Item, Container, etc.)
     * @param elementName
     *            Name of element.
     * @return entry id
     * 
     * @throws InvalidContentException
     *             Thrown if content has invalid values.
     * @throws TripleStoreSystemException
     *             Thrown if request of TripleStore failed.
     * @throws WebserverSystemException
     *             Thrown in case of internal error.
     * @throws MissingAttributeValueException
     *             Thrown if objid was not given as parameter.
     */
    private String checkRefElement(
        final StartElement element, final String objectType,
        final String elementName) throws InvalidContentException,
        TripleStoreSystemException, WebserverSystemException,
        MissingAttributeValueException {
        String entryId = null;
        int indexOfObjId = element.indexOfAttribute(null, "objid");
        int indexOfHref = element.indexOfAttribute(Constants.XLINK_URI, "href");
        if (indexOfObjId != -1) {
            entryId = element.getAttribute(indexOfObjId).getValue();
            if (entryId.length() == 0) {
                String message =
                    "Value of attribute 'objid' of the element '"
                        + parser.getCurPath() + "' is missing.";
                logger.error(message);
                throw new MissingAttributeValueException(message);
            }
        }
        else if (indexOfHref != -1) {
            Attribute xlinkHref = element.getAttribute(indexOfHref);
            String xlinkHrefValue = xlinkHref.getValue();
            if (xlinkHrefValue.length() == 0) {
                String message =
                    "Value of attribute 'objid' of the element '"
                        + parser.getCurPath() + "' is missing.";
                logger.error(message);
                throw new MissingAttributeValueException(message);
            }
            String xlinkPrefix = xlinkHref.getPrefix();
            entryId = Utility.getId(xlinkHrefValue);
            if (!xlinkHrefValue.equals("/ir/" + elementName + "/" + entryId)) {
                String message =
                    "The value of attribute " + element.getLocalName() + "."
                        + xlinkPrefix + ":href has to look like: ir/"
                        + elementName + "/" + entryId;
                logger.error(message);
                throw new InvalidContentException(message);
            }
        }
        if (!TripleStoreUtility.getInstance().exists(entryId)) {
            String message = "Referenced object in struct-map does not exist.";
            logger.error(message);
            throw new InvalidContentException(message);
        }
        if (!objectType.equals(TripleStoreUtility.getInstance().getObjectType(
            entryId))) {
            throw new InvalidContentException(
                "Referenced object in struct-map is no " + elementName + ".");
        }
        return entryId;
    }

    /**
     * @return Returns the logger.
     */
    public static AppLogger getLogger() {
        return logger;
    }

    /**
     * Get struct-map entries.
     * 
     * @return struct-map entries.
     */
    public List<String> getEntries() {
        return entries;
    }

}