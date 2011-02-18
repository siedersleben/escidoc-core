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
package de.escidoc.core.aa.business.xacml.finder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.combine.OrderedPermitOverridesPolicyAlg;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;

import de.escidoc.core.aa.business.authorisation.Constants;
import de.escidoc.core.aa.business.authorisation.CustomPolicyBuilder;
import de.escidoc.core.aa.business.authorisation.CustomStatusBuilder;
import de.escidoc.core.aa.business.authorisation.FinderModuleHelper;
import de.escidoc.core.aa.business.cache.PoliciesCache;
import de.escidoc.core.aa.business.cache.PoliciesCacheProxy;
import de.escidoc.core.aa.business.interfaces.UserAccountHandlerInterface;
import de.escidoc.core.aa.business.interfaces.UserGroupHandlerInterface;
import de.escidoc.core.aa.business.persistence.EscidocRole;
import de.escidoc.core.aa.business.persistence.EscidocRoleDaoInterface;
import de.escidoc.core.aa.business.persistence.RoleGrant;
import de.escidoc.core.aa.business.xacml.XacmlPolicyReference;
import de.escidoc.core.aa.business.xacml.XacmlPolicySet;
import de.escidoc.core.common.exceptions.EscidocException;
import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.logger.AppLogger;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.string.StringUtility;

/**
 * Custom implementation of a PolicyFinderModule.
 * <p/>
 * 
 * @spring.bean id="eSciDoc.core.aa.DatabasePolicyFinderModule"
 * 
 * @author Roland Werner (Accenture)
 * @aa
 */
public class DatabasePolicyFinderModule extends PolicyFinderModule {

    /** The logger. */
    private static final AppLogger log = new AppLogger(
        DatabasePolicyFinderModule.class.getName());

    /**
     * The property which is used to specify the schema file to validate against
     * (if any).
     */
    public static final String POLICY_SCHEMA_PROPERTY =
        "com.sun.xacml.PolicySchema";

    public static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    public static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    public static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private UserAccountHandlerInterface userAccountHandler;

    private UserGroupHandlerInterface userGroupHandler;

    /** The data access object to retrieve escidoc roles from. */
    private EscidocRoleDaoInterface roleDao;

    private AbstractPolicy defaultPolicies;

    private PoliciesCacheProxy policiesCacheProxy = null;

    private PolicyFinder policyFinder;

    /**
     * Indicates whether this module supports finding policies based on a
     * request (target matching). Since this module does support finding
     * policies based on requests, it returns true.
     * 
     * @return true, since finding policies based on requests is supported
     * @aa
     */
    @Override
    public final boolean isRequestSupported() {
        return true;
    }

    /**
     * Indicates whether this module supports finding policies based on
     * referencing. Since this module does not support finding policies based on
     * referencing, it returns false.
     * 
     * @return false, since finding policies based on referencing is not
     *         supported
     * @aa
     */
    @Override
    public final boolean isIdReferenceSupported() {
        return true;
    }

    /**
     * Initializes the <code>DatabasePolicyFinderModule</code> by setting the
     * specified <code>PolicyFinder</code> to help in instantiating PolicySets.
     * 
     * @param finder
     *            a PolicyFinder used to help in instantiating PolicySets
     * @aa
     */
    @Override
    public final void init(final PolicyFinder finder) {

    }

    /**
     * See Interface for functional description.
     * 
     * @see com.sun.xacml.finder.PolicyFinderModule#invalidateCache()
     * 
     * @aa
     */
    @Override
    public final void invalidateCache() {

        PoliciesCache.clear();
    }

    // CHECKSTYLE:JAVADOC-OFF

    /**
     * See Interface for functional description.
     * 
     * @param idReference
     * @param type
     * @return
     * @throws IllegalArgumentException
     * @see com.sun.xacml.finder.PolicyFinderModule#findPolicy(java.net.URI,
     *      int)
     */
    @Override
    public final PolicyFinderResult findPolicy(final URI idReference, final int type) {

        if (type != PolicyReference.POLICY_REFERENCE
            && type != PolicyReference.POLICYSET_REFERENCE) {
            throw new IllegalArgumentException(StringUtility
                    .format("Illegal type", type));
        }
        if (type != PolicyReference.POLICYSET_REFERENCE) {
            return new PolicyFinderResult();
        }

        // The policyId is concatenated String
        // containing <roleName>/<user or group>/<userOrGroupId>
        String[] parts = idReference.toString().split("/");
        StringBuilder roleIdentifier = new StringBuilder("");
        if (parts.length > 2) {
            for (int i = 0; i < parts.length - 2; i++) {
                roleIdentifier.append(parts[i]);
            }
        }
        else {
            roleIdentifier.append(idReference);
        }
        URI roleIdentifierUri;
        try {
            roleIdentifierUri = new URI(roleIdentifier.toString());
        }
        catch (URISyntaxException e1) {
            return createProcessingError(
                "Error during resolving policy reference. ", e1);
        }

        XacmlPolicySet result =
            PoliciesCache.getRolePolicySet(roleIdentifierUri);

        if (result == null) {
            EscidocRole role = PoliciesCache.getRole(roleIdentifier.toString());
            if (role == null) {
                try {
                    role = roleDao.retrieveRole(roleIdentifier.toString());
                    role.isLimited();
                    PoliciesCache.putRole(role.getId(), role);
                }
                catch (SqlDatabaseSystemException e) {
                    role = null;
                }
            }
            if (role == null) {
                return new PolicyFinderResult();
            }

            try {
                result = (XacmlPolicySet) role.getXacmlPolicySet();
            }
            catch (WebserverSystemException e) {
                return createProcessingError(
                    "Error during resolving policy reference. ", e);
            }

            if (log.isDebugEnabled()) {
                try {
                    log.debug(role.getXacmlPolicySet().toString());
                }
                catch (WebserverSystemException e) {
                    log.debug(StringUtility.format(
                        "Fetching of role's policy set failed.",
                        role.toString()));
                }
            }

            PoliciesCache.putRolePolicySet(roleIdentifierUri, result);
        }
        try {
            result =
                CustomPolicyBuilder.regeneratePolicySet(result,
                    idReference.toString());
        }
        catch (Exception e) {
            return createProcessingError(
                "Error during resolving policy reference. ", e);
        }
        return new PolicyFinderResult(result);
    }

    // CHECKSTYLE:JAVADOC-ON

    /**
     * Finds a policy based on a request's context.
     * <p/>
     * 
     * The method executes the following steps in order to fetch suitable
     * policies:
     * <ul>
     * <li>Retrieve <code>subject-id</code> and <code>action-id</code> from the
     * provided context object, which is used to narrow down the search to only
     * policies that have this <code>subject-id</code> and
     * <code>action-id</code> in their target part. This is done via the method
     * <code>retrieveSingleAttribute</code>.</li>
     * 
     * <li>Check whether the policies for <code>subject-id</code> and
     * <code>action-id</code> are already contained in the
     * <code>PoliciesCache</code>. This is done via the method
     * <code>retrievePoliciesFromCache</code>.</li>
     * 
     * <li>Only if the policies are not in the cache, they will be fetched from
     * the database. For this method <code>retrieveFlatPolicies</code> is
     * called.</li>
     * 
     * <li>Any retrieved policies will now be stored in the
     * <code>PoliciesCache</code> so they will be available for the next request
     * </li>
     * 
     * <li>The retrieved policies are checked for matching against the current
     * <code>EvaluationCtx</code>.</li>
     * 
     * <li>If a matching policy is found, it is returned.</li>
     * </ul>
     * 
     * @param context
     *            the representation of the request data
     * 
     * @return the result of trying to find an applicable policy
     * @see FinderModuleHelper#retrieveSingleAttribute
     * @see #retrievePoliciesFromCache
     * @see PoliciesCache
     * @aa
     */
    @Override
    public final PolicyFinderResult findPolicy(final EvaluationCtx context) {
        try {
            List<AbstractPolicy> policies = new ArrayList<AbstractPolicy>();

            // first get the user id and action from the request
            String userId =
                FinderModuleHelper.retrieveSingleSubjectAttribute(context,
                    Constants.URI_SUBJECT_ID, true);

            // get policySet for policies attached to the user
            XacmlPolicySet userPolicySet = getUserPolicies(userId);
            policies.add(userPolicySet);

            // get policySet for policies attached via groups the user belongs
            // to
            if (!UserContext.isIdOfAnonymousUser(userId)) {
                XacmlPolicySet userGroupsPolicySet =
                    getUserGroupPolicies(userId);
                if (userGroupsPolicySet != null) {
                    policies.add(userGroupsPolicySet);
                }
            }

            XacmlPolicySet result =
                new XacmlPolicySet(
                    "UserGroupPolicies-" + userId,
                    XacmlPolicySet.URN_POLICY_COMBINING_ALGORITHM_ORDERED_PERMIT_OVERRIDES,
                    null, policies);

            return new PolicyFinderResult(result);

        }
        catch (Exception e) {
            return createProcessingError(
                "Exception happened while searching for policies: ", e);
        }
    }

    /**
     * Creates a <code>XacmlPolicySet</code> object with all policies directly
     * attached to the user.
     * 
     * @param userId
     *            The userId.
     * @return Returns the created <code>XacmlPolicySet</code> object for the
     *         user.
     * @throws Exception
     *             e
     * @aa
     */
    private XacmlPolicySet getUserPolicies(final String userId)
        throws Exception {

        // first check the cache
        XacmlPolicySet result = PoliciesCache.getUserPolicies(userId);

        // if no policies found in the cache, get them from the database
        if (result == null) {

            List<AbstractPolicy> policies = new ArrayList<AbstractPolicy>();

            // retrieve user's roles policies
            XacmlPolicySet rolesPolicySet = retrieveUserRolesPolicies(userId);
            if (rolesPolicySet != null) {
                policies.add(rolesPolicySet);
            }

            // add the default policies
            AbstractPolicy defPolicies = retrieveDefaultPolicies();
            if (defPolicies != null) {
                policies.add(defPolicies);
            }

            result =
                new XacmlPolicySet(
                    "Policies-" + userId,
                    XacmlPolicySet.URN_POLICY_COMBINING_ALGORITHM_ORDERED_PERMIT_OVERRIDES,
                    null, policies);

            if (log.isDebugEnabled()) {
                log.debug(result.toString());
            }

            PoliciesCache.putUserPolicies(userId, result);
        }

        return result;

    }

    /**
     * Creates a <code>XacmlPolicySet</code> object with all policies attached
     * to the user via the groups he belongs to.
     * 
     * @param userId
     *            The userId.
     * @return Returns the created <code>XacmlPolicySet</code> object for the
     *         user.
     * @throws Exception
     *             e
     * @aa
     */
    private XacmlPolicySet getUserGroupPolicies(final String userId)
        throws Exception {

        List<AbstractPolicy> policies = new ArrayList<AbstractPolicy>();
        // get groups the user belongs to
        Set<String> userGroups = policiesCacheProxy.getUserGroups(userId);
        XacmlPolicySet groupPolicySet;
        if (userGroups != null && !userGroups.isEmpty()) {
            List<String> nonCachedGroupPolicies = new ArrayList<String>();
            for (String groupId : userGroups) {
                groupPolicySet = PoliciesCache.getGroupPolicies(groupId);
                if (groupPolicySet == null) {
                    nonCachedGroupPolicies.add(groupId);
                }
                else {
                    if (groupPolicySet.getChildren() != null
                        && !groupPolicySet.getChildren().isEmpty()) {
                        policies.add(groupPolicySet);
                    }
                }
            }
            if (!nonCachedGroupPolicies.isEmpty()) {
                // retrieve group's roles policies
                Map<String, XacmlPolicySet> groupsPolicies =
                    retrieveGroupRolesPolicies(nonCachedGroupPolicies);
                for (String groupId : nonCachedGroupPolicies) {
                    XacmlPolicySet thisGroupPolicySet =
                        groupsPolicies.get(groupId);
                    if (thisGroupPolicySet == null) {
                        thisGroupPolicySet =
                            new XacmlPolicySet(
                                "roles-" + groupId,
                                XacmlPolicySet.URN_POLICY_COMBINING_ALGORITHM_ORDERED_PERMIT_OVERRIDES,
                                null, new ArrayList<AbstractPolicy>());
                    }

                    PoliciesCache.putGroupPolicies(groupId, thisGroupPolicySet);
                    if (thisGroupPolicySet.getChildren() != null
                        && !thisGroupPolicySet.getChildren().isEmpty()) {
                        policies.add(thisGroupPolicySet);
                    }
                }
            }
        }
        if (!policies.isEmpty()) {
            return new XacmlPolicySet(
                "GroupPolicies-" + userId,
                XacmlPolicySet.URN_POLICY_COMBINING_ALGORITHM_ORDERED_PERMIT_OVERRIDES,
                null, policies);
        }
        return null;

    }

    /**
     * Creates a <code>PolicyFinderResult</code> object holding a processing
     * error status and the provided exception.
     * 
     * @param msg
     *            The error message.
     * @param e
     *            The exception causing the error.
     * @return Returns the created <code>PolicyFinderResult</code> object.
     * @aa
     */
    private static PolicyFinderResult createProcessingError(
            final String msg, final Exception e) {

        log.error(msg, e);
        Exception ex;
        if (e instanceof EscidocException) {
            ex = e;
        }
        else {
            ex = new WebserverSystemException(e);
        }
        return new PolicyFinderResult(CustomStatusBuilder.createErrorStatus(
            Status.STATUS_PROCESSING_ERROR, ex));
    }

    /**
     * Retrieves the default policies that are granted to every user.<br>
     * The policies are fetched for the dummy role "Default". They are set in
     * the field <code>defaultPolicies</code>.
     * 
     * @return Returns an <code>XacmlPolicyReference</code> referencing the set
     *         of default policies.
     * @throws WebserverSystemException
     *             Thrown in case of an internal error.
     * @aa
     */
    private AbstractPolicy retrieveDefaultPolicies()
        throws WebserverSystemException {

        if (defaultPolicies == null) {
            try {
                defaultPolicies =
                    new XacmlPolicyReference(new URI(
                        EscidocRole.DEFAULT_USER_ROLE_ID),
                        PolicyReference.POLICYSET_REFERENCE, policyFinder);
            }
            catch (Exception e) {
                throw new WebserverSystemException(e);
            }
        }
        return defaultPolicies;
    }

    /**
     * Retrieve all policies given to the user by his/her (restricted) roles <br>
     * The policies are returned in a <code>XacmlPolicySet</code> with the
     * policy combining algorithm set to ordered-permit-overrides.
     * 
     * @param userId
     *            The internal id of the user, used to identify the user
     *            account.
     * @return Returns a <code>PolicySet</code> with the policy combining
     *         algorithm set to ordered-permit-overrides or <code>null</code>.
     *         The policy set is built up by policy references to the role
     *         policy sets. If the provided user id matches the anonymous user,
     *         <code>null</code> is returned.
     * @throws WebserverSystemException
     *             In case of an internal error.
     * 
     * @aa
     */
    private XacmlPolicySet retrieveUserRolesPolicies(final String userId)
        throws WebserverSystemException {

        if (UserContext.isIdOfAnonymousUser(userId)) {
            return null;
        }

        try {
            Map<String, Map<String, List<RoleGrant>>> roleGrants;
            roleGrants = userAccountHandler.retrieveCurrentGrantsAsMap(userId);
            // cache grants for later retrieval during policy evaluation
            PoliciesCache.putUserGrants(userId, roleGrants);

            if (roleGrants == null || roleGrants.isEmpty()) {
                return null;
            }

            return retrieveRolesPolicies(roleGrants, userId, true);
        }
        catch (Exception e) {
            throw new WebserverSystemException(e);
        }
    }

    /**
     * Retrieve all policies given to the groups by their (restricted) roles <br>
     * The policies are returned in a <code>XacmlPolicySet</code> with the
     * policy combining algorithm set to ordered-permit-overrides.
     * 
     * @param groupIds
     *            The internal ids of the groups, used to identify the user
     *            groups.
     * @return Returns a <code>PolicySet</code> with the policy combining
     *         algorithm set to ordered-permit-overrides or <code>null</code>.
     *         The policy set is built up by policy references to the role
     *         policy sets. If the provided user id matches the anonymous user,
     *         <code>null</code> is returned.
     * @throws WebserverSystemException
     *             In case of an internal error.
     * 
     * @aa
     */
    private Map<String, XacmlPolicySet> retrieveGroupRolesPolicies(
        final List<String> groupIds) throws WebserverSystemException {

        HashMap<String, XacmlPolicySet> ret =
            new HashMap<String, XacmlPolicySet>();
        try {
            Map<String, Map<String, Map<String, List<RoleGrant>>>> roleGrants;
            roleGrants =
                userGroupHandler.retrieveManyCurrentGrantsAsMap(groupIds);
            // cache grants for later retrieval during policy evaluation
            if (roleGrants != null) {
                for (Entry<String, Map<String, 
                        Map<String, List<RoleGrant>>>> entry 
                                    : roleGrants.entrySet()) {
                    PoliciesCache.putGroupGrants(entry.getKey(),
                        entry.getValue());
                    if (entry.getValue() != null
                        && !entry.getValue().isEmpty()) {
                        XacmlPolicySet policies =
                            retrieveRolesPolicies(entry.getValue(),
                                entry.getKey(), false);
                        if (policies != null) {
                            ret.put(entry.getKey(), policies);
                        }
                    }
                }
            }
            return ret;
        }
        catch (Exception e) {
            throw new WebserverSystemException(e);
        }
    }

    /**
     * Retrieve all policies given to the user/group by his/her (restricted)
     * roles <br>
     * The policies are returned in a <code>XacmlPolicySet</code> with the
     * policy combining algorithm set to ordered-permit-overrides.
     * 
     * @param roleGrants
     *            map with current grants of the user/group.
     * @param userOrGroupId
     *            The internal id of the user/group, used to identify the user
     *            account/user group.
     * @param isUser
     *            boolean if user-roles are requested
     * @return Returns a <code>PolicySet</code> with the policy combining
     *         algorithm set to ordered-permit-overrides or <code>null</code>.
     *         The policy set is built up by policy references to the role
     *         policy sets. If the provided user id matches the anonymous user,
     *         <code>null</code> is returned.
     * @throws WebserverSystemException
     *             In case of an internal error.
     * 
     * @aa
     */
    private XacmlPolicySet retrieveRolesPolicies(
        final Map roleGrants, final String userOrGroupId, final boolean isUser)
        throws WebserverSystemException {

        String userOrGroupIdentifier = "user";
        if (!isUser) {
            userOrGroupIdentifier = "group";
        }
        List<AbstractPolicy> rolesPolicies = new ArrayList<AbstractPolicy>();
        try {
            for (Object o : roleGrants.keySet()) {
                String roleId = (String) o;
                EscidocRole role = PoliciesCache.getRole(roleId);
                if (role == null) {
                    role = roleDao.retrieveRole(roleId);
                    role.isLimited();
                    PoliciesCache.putRole(roleId, role);
                }
                // The policyId is concatenated String
                // containing <roleName>/<user or group>/<userOrGroupId>
                URI policySetId =
                        new URI(role.getPolicySetId().toString() + '/'
                                + userOrGroupIdentifier + '/' + userOrGroupId);
                rolesPolicies.add(new XacmlPolicyReference(policySetId,
                        PolicyReference.POLICYSET_REFERENCE, policyFinder));
            }

            if (!rolesPolicies.isEmpty()) {
                return new XacmlPolicySet("roles-" + userOrGroupId,
                    OrderedPermitOverridesPolicyAlg.algId, null, rolesPolicies);
            }

        }
        catch (Exception e) {
            throw new WebserverSystemException(e);
        }
        return null;
    }

    /**
     * Injects the policies cache proxy.
     * 
     * @spring.property ref="resource.PoliciesCacheProxy"
     * @param policiesCacheProxy
     *            the {@link PoliciesCacheProxy} to inject.
     */
    public void setPoliciesCacheProxy(
        final PoliciesCacheProxy policiesCacheProxy) {
        this.policiesCacheProxy = policiesCacheProxy;
    }

    /**
     * Sets the policy finder.
     * 
     * @param policyFinder
     *            The <code>PolicyFinder</code> object to set.
     */
    public final void setPolicyFinder(final PolicyFinder policyFinder) {

        if (policyFinder == null) {
            throw new IllegalArgumentException(
                "Policy finder must be provided.");
        }
        this.policyFinder = policyFinder;
    }

    /**
     * Injects the role data access object.
     * 
     * @param roleDao
     *            The {@link EscidocRoleDaoInterface} implementation.
     * @spring.property ref="persistence.EscidocRoleDao"
     */
    public void setRoleDao(final EscidocRoleDaoInterface roleDao) {

        this.roleDao = roleDao;
    }

    /**
     * Injects the user account handler.
     * 
     * @param userAccountHandler
     *            The {@link UserAccountHandlerInterface} implementation.
     * @spring.property ref="business.UserAccountHandler"
     */
    public void setUserAccountHandler(
        final UserAccountHandlerInterface userAccountHandler) {

        this.userAccountHandler = userAccountHandler;
    }

    /**
     * Injects the user group handler.
     * 
     * @param userGroupHandler
     *            The {@link UserGroupHandlerInterface} implementation.
     * @spring.property ref="business.UserGroupHandler"
     */
    public void setUserGroupHandler(
        final UserGroupHandlerInterface userGroupHandler) {

        this.userGroupHandler = userGroupHandler;
    }

}
