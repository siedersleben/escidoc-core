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
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package org.escidoc.core.aa.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.oval.guard.Guarded;
import org.escidoc.core.aa.UserAccountRestService;
import org.escidoc.core.domain.ObjectFactoryProvider;
import org.escidoc.core.domain.aa.grants.CurrentGrantsTypeTO;
import org.escidoc.core.domain.aa.grants.GrantTypeTO;
import org.escidoc.core.domain.aa.useraccount.UserAccountResourcesTypeTO;
import org.escidoc.core.domain.aa.useraccount.UserAccountTypeTO;
import org.escidoc.core.domain.aa.useraccount.attributes.AttributeTypeTO;
import org.escidoc.core.domain.aa.useraccount.attributes.AttributesTypeTO;
import org.escidoc.core.domain.aa.useraccount.preferences.PreferenceTypeTO;
import org.escidoc.core.domain.aa.useraccount.preferences.PreferencesTypeTO;
import org.escidoc.core.domain.result.ResultTypeTO;
import org.escidoc.core.domain.service.ServiceUtility;
import org.escidoc.core.domain.taskparam.optimisticlocking.OptimisticLockingTaskParamTO;
import org.escidoc.core.domain.taskparam.revokegrant.RevokeGrantTaskParamTO;
import org.escidoc.core.domain.taskparam.revokegrants.RevokeGrantsTaskParamTO;
import org.escidoc.core.domain.taskparam.updatepassword.UpdatePasswordTaskParamTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface;
import de.escidoc.core.common.exceptions.application.invalid.InvalidScopeException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.GrantNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.PreferenceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.RoleNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.UserAccountNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.UserAttributeNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyActiveException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyDeactiveException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
import de.escidoc.core.common.exceptions.application.violated.AlreadyRevokedException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
import de.escidoc.core.common.exceptions.system.SystemException;

import javax.xml.bind.JAXBElement;

/**
 * @author Michael Hoppe
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 */
@Guarded(applyFieldConstraintsToConstructors = false, applyFieldConstraintsToSetters = false,
    assertParametersNotNull = false, checkInvariants = false, inspectInterfaces = true)
public class UserAccountRestServiceImpl implements UserAccountRestService {

    @Autowired
    @Qualifier("service.UserAccountHandler")
    private UserAccountHandlerInterface userAccountHandler;

    @Autowired
    private ServiceUtility serviceUtility;

    @Autowired
    private ObjectFactoryProvider factoryProvider;

    /**
     *
     */
    protected UserAccountRestServiceImpl() {
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#create(org.escidoc.core.domain.aa.UserAccountTO)
     */
    @Override
    public JAXBElement<UserAccountTypeTO> create(final UserAccountTypeTO userAccountTO)
        throws UniqueConstraintViolationException, InvalidStatusException, XmlCorruptedException,
        XmlSchemaValidationException, OrganizationalUnitNotFoundException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getUserAccountFactory().createUserAccount(serviceUtility
            .fromXML(UserAccountTypeTO.class, this.userAccountHandler.create(serviceUtility.toXML(userAccountTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieve(java.lang.String)
     */
    @Override
    public JAXBElement<UserAccountTypeTO> retrieve(final String id)
        throws UserAccountNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        return factoryProvider.getUserAccountFactory().createUserAccount(
            serviceUtility.fromXML(UserAccountTypeTO.class, this.userAccountHandler.retrieve(id)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#update(java.lang.String, org.escidoc.core.domain.aa.UserAccountTO)
     */
    @Override
    public JAXBElement<UserAccountTypeTO> update(final String id, final UserAccountTypeTO userAccountTO)
        throws UserAccountNotFoundException, UniqueConstraintViolationException, InvalidStatusException,
        XmlCorruptedException, XmlSchemaValidationException, MissingMethodParameterException,
        MissingAttributeValueException, OptimisticLockingException, AuthenticationException, AuthorizationException,
        OrganizationalUnitNotFoundException, SystemException {
        return factoryProvider.getUserAccountFactory().createUserAccount(serviceUtility.fromXML(UserAccountTypeTO.class,
            this.userAccountHandler.update(id, serviceUtility.toXML(userAccountTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#delete(java.lang.String)
     */
    @Override
    public void delete(final String id)
        throws UserAccountNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        this.userAccountHandler.delete(id);
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveCurrentUser()
     */
    @Override
    public JAXBElement<UserAccountTypeTO> retrieveCurrentUser()
        throws UserAccountNotFoundException, AuthenticationException,
        AuthorizationException, SystemException {
        return factoryProvider.getUserAccountFactory().createUserAccount(
            serviceUtility.fromXML(UserAccountTypeTO.class, this.userAccountHandler.retrieveCurrentUser()));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#updatePassword(java.lang.String, java.lang.String)
     */
    @Override
    public void updatePassword(final String id, final UpdatePasswordTaskParamTO taskParam)
        throws UserAccountNotFoundException, InvalidStatusException, XmlCorruptedException,
        MissingMethodParameterException, OptimisticLockingException, AuthenticationException, AuthorizationException,
        SystemException {
        this.userAccountHandler.updatePassword(id, serviceUtility.toXML(taskParam));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#updatePreferences(java.lang.String,
     * org.escidoc.core.domain.aa.UserAccountPreferenceListTO)
     */
    @Override
    public JAXBElement<PreferencesTypeTO> updatePreferences(final String id, final PreferencesTypeTO preferencesTypeTO)
        throws UserAccountNotFoundException, XmlCorruptedException, XmlSchemaValidationException,
        OptimisticLockingException, SystemException, AuthenticationException, AuthorizationException,
        MissingMethodParameterException, MissingAttributeValueException {
        return factoryProvider.getUserAccountPreferencesFactory().createPreferences(
            serviceUtility.fromXML(PreferencesTypeTO.class, this.userAccountHandler.updatePreferences(id,
                serviceUtility.toXML(preferencesTypeTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#activate(java.lang.String, java.lang.String)
     */
    @Override
    public void activate(final String id, final OptimisticLockingTaskParamTO taskParam)
        throws AlreadyActiveException,
        UserAccountNotFoundException, XmlCorruptedException, MissingMethodParameterException,
        MissingAttributeValueException, OptimisticLockingException, AuthenticationException, AuthorizationException,
        SystemException {
        this.userAccountHandler.activate(id, serviceUtility.toXML(taskParam));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#deactivate(java.lang.String, java.lang.String)
     */
    @Override
    public void deactivate(final String id, final OptimisticLockingTaskParamTO taskParam)
        throws AlreadyDeactiveException, UserAccountNotFoundException,
        XmlCorruptedException, MissingMethodParameterException, MissingAttributeValueException,
        OptimisticLockingException, AuthenticationException, AuthorizationException, SystemException {
        this.userAccountHandler.deactivate(id, serviceUtility.toXML(taskParam));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveResources(java.lang.String)
     */
    @Override
    public JAXBElement<UserAccountResourcesTypeTO> retrieveResources(final String id)
        throws UserAccountNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getUserAccountFactory().createResources(
            serviceUtility.fromXML(UserAccountResourcesTypeTO.class, this.userAccountHandler.retrieveResources(id)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveCurrentGrants(java.lang.String)
     */
    @Override
    public JAXBElement<CurrentGrantsTypeTO> retrieveCurrentGrants(final String id)
        throws UserAccountNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getGrantFactory().createCurrentGrants(
            serviceUtility.fromXML(CurrentGrantsTypeTO.class, this.userAccountHandler.retrieveCurrentGrants(id)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#createGrant(org.escidoc.core.domain.aa.GrantTO)
     */
    @Override
    public JAXBElement<GrantTypeTO> createGrant(final String id, final GrantTypeTO grantTypeTO)
        throws AlreadyExistsException, UserAccountNotFoundException,
        InvalidScopeException, RoleNotFoundException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getGrantFactory().createGrant(serviceUtility
            .fromXML(GrantTypeTO.class, this.userAccountHandler.createGrant(id, serviceUtility.toXML(grantTypeTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveGrant(java.lang.String, java.lang.String)
     */
    @Override
    public JAXBElement<GrantTypeTO> retrieveGrant(final String id, final String grantId)
        throws UserAccountNotFoundException,
        GrantNotFoundException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException {
        return factoryProvider.getGrantFactory().createGrant(
            serviceUtility.fromXML(GrantTypeTO.class, this.userAccountHandler.retrieveGrant(id, grantId)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#revokeGrant(java.lang.String, java.lang.String)
     */
    @Override
    public void revokeGrant(final String id, final String grantId, final RevokeGrantTaskParamTO taskParam)
        throws UserAccountNotFoundException, GrantNotFoundException,
        AlreadyRevokedException, XmlCorruptedException, MissingAttributeValueException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        this.userAccountHandler.revokeGrant(id, grantId, serviceUtility.toXML(taskParam));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#revokeGrants(java.lang.String, java.lang.String)
     */
    @Override
    public void revokeGrants(final String id, final RevokeGrantsTaskParamTO taskParam)
        throws UserAccountNotFoundException, GrantNotFoundException, AlreadyRevokedException, XmlCorruptedException,
        MissingAttributeValueException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        this.userAccountHandler.revokeGrants(id, serviceUtility.toXML(taskParam));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrievePreference(java.lang.String, java.lang.String)
     */
    @Override
    public JAXBElement<PreferenceTypeTO> retrievePreference(final String id, final String name)
        throws UserAccountNotFoundException,
        PreferenceNotFoundException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException {
        return factoryProvider.getUserAccountPreferencesFactory().createPreference(
            serviceUtility.fromXML(PreferenceTypeTO.class, this.userAccountHandler.retrievePreference(id, name)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrievePreferences(java.lang.String)
     */
    @Override
    public JAXBElement<PreferencesTypeTO> retrievePreferences(final String id)
        throws UserAccountNotFoundException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getUserAccountPreferencesFactory().createPreferences(
            serviceUtility.fromXML(PreferencesTypeTO.class, this.userAccountHandler.retrievePreferences(id)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#createPreference(java.lang.String,
     * org.escidoc.core.domain.aa.UserAccountPreferenceTO)
     */
    @Override
    public JAXBElement<PreferenceTypeTO> createPreference(final String id, final PreferenceTypeTO preferenceTypeTO)
        throws AlreadyExistsException, UserAccountNotFoundException, XmlCorruptedException,
        XmlSchemaValidationException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException, PreferenceNotFoundException {
        return factoryProvider.getUserAccountPreferencesFactory().createPreference(serviceUtility
            .fromXML(PreferenceTypeTO.class,
                this.userAccountHandler.createPreference(id, serviceUtility.toXML(preferenceTypeTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#updatePreference(java.lang.String, java.lang.String,
     * org.escidoc.core.domain.aa.UserAccountPreferenceTO)
     */
    @Override
    public JAXBElement<PreferenceTypeTO> updatePreference(final String id, final String preferenceName,
                                                          final PreferenceTypeTO preferenceTypeTO)
        throws AlreadyExistsException, UserAccountNotFoundException, XmlCorruptedException,
        XmlSchemaValidationException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException, PreferenceNotFoundException, OptimisticLockingException, MissingAttributeValueException {
        return factoryProvider.getUserAccountPreferencesFactory().createPreference(serviceUtility
            .fromXML(PreferenceTypeTO.class,
                this.userAccountHandler.updatePreference(id, preferenceName, serviceUtility.toXML(preferenceTypeTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#deletePreference(java.lang.String, java.lang.String)
     */
    @Override
    public void deletePreference(final String id, final String preferenceName)
        throws UserAccountNotFoundException, PreferenceNotFoundException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        this.userAccountHandler.deletePreference(id, preferenceName);
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#createAttribute(java.lang.String,
     * org.escidoc.core.domain.aa.UserAccountAttributeTO)
     */
    @Override
    public JAXBElement<AttributeTypeTO> createAttribute(final String id, final AttributeTypeTO attributeTypeTO)
        throws AlreadyExistsException, UserAccountNotFoundException, XmlCorruptedException,
        XmlSchemaValidationException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
        SystemException {
        return factoryProvider.getUserAccountAttributesFactory().createAttribute(serviceUtility
            .fromXML(AttributeTypeTO.class,
                this.userAccountHandler.createAttribute(id, serviceUtility.toXML(attributeTypeTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveAttributes(java.lang.String)
     */
    @Override
    public JAXBElement<AttributesTypeTO> retrieveAttributes(final String id)
        throws UserAccountNotFoundException, MissingMethodParameterException, AuthenticationException,
        AuthorizationException, SystemException {
        return factoryProvider.getUserAccountAttributesFactory().createAttributes(
            serviceUtility.fromXML(AttributesTypeTO.class, this.userAccountHandler.retrieveAttributes(id)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveNamedAttributes(java.lang.String, java.lang.String)
     */
    @Override
    public JAXBElement<AttributesTypeTO> retrieveNamedAttributes(final String id, final String name)
        throws UserAccountNotFoundException, UserAttributeNotFoundException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getUserAccountAttributesFactory().createAttributes(
            serviceUtility.fromXML(AttributesTypeTO.class, this.userAccountHandler.retrieveNamedAttributes(id, name)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrieveAttribute(java.lang.String, java.lang.String)
     */
    @Override
    public JAXBElement<AttributeTypeTO> retrieveAttribute(final String id, final String attId)
        throws UserAccountNotFoundException, UserAttributeNotFoundException, MissingMethodParameterException,
        AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getUserAccountAttributesFactory().createAttribute(
            serviceUtility.fromXML(AttributeTypeTO.class, this.userAccountHandler.retrieveAttribute(id, attId)));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#updateAttribute(java.lang.String, java.lang.String,
     * org.escidoc.core.domain.aa.UserAccountAttributeTO)
     */
    @Override
    public JAXBElement<AttributeTypeTO> updateAttribute(final String id, final String attId,
                                                        final AttributeTypeTO attributeTypeTO)
        throws UserAccountNotFoundException, OptimisticLockingException, UserAttributeNotFoundException,
        ReadonlyElementViolationException, XmlCorruptedException, XmlSchemaValidationException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        return factoryProvider.getUserAccountAttributesFactory().createAttribute(serviceUtility
            .fromXML(AttributeTypeTO.class,
                this.userAccountHandler.updateAttribute(id, attId, serviceUtility.toXML(attributeTypeTO))));
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#deleteAttribute(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteAttribute(final String id, final String attId)
        throws UserAccountNotFoundException, UserAttributeNotFoundException, ReadonlyElementViolationException,
        MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
        this.userAccountHandler.deleteAttribute(id, attId);
    }

    /* (non-Javadoc)
     * @see de.escidoc.core.aa.UserAccountRestService#retrievePermissionFilterQuery(java.util.List, java.util.List,
     * java.util.List)
     */
    @Override
    public JAXBElement<ResultTypeTO> retrievePermissionFilterQuery(final List<String> index, final List<String> user,
                                                                   final List<String> role)
        throws SystemException,
        InvalidSearchQueryException, AuthenticationException, AuthorizationException {
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        if (index != null && index.size() > 0) {
            parameters.put("index", index.toArray(new String[index.size()]));
        }
        if (user != null && user.size() > 0) {
            parameters.put("user", user.toArray(new String[user.size()]));
        }
        if (role != null && role.size() > 0) {
            parameters.put("role", role.toArray(new String[role.size()]));
        }
        return factoryProvider.getResultFactory().createResult(serviceUtility
            .fromXML(ResultTypeTO.class, this.userAccountHandler.retrievePermissionFilterQuery(parameters)));
    }
}