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
package de.escidoc.core.aa.security.aop;

import de.escidoc.core.aa.security.cache.SecurityInterceptorCache;
import de.escidoc.core.aa.service.interfaces.PolicyDecisionPointInterface;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.aop.AopUtil;
import de.escidoc.core.common.util.logger.AppLogger;
import de.escidoc.core.common.util.security.helper.InvocationParser;
import de.escidoc.core.common.util.security.persistence.MethodMapping;
import de.escidoc.core.common.util.security.persistence.MethodMappingList;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.string.StringUtility;
import de.escidoc.core.om.service.interfaces.ContainerHandlerInterface;
import de.escidoc.core.om.service.interfaces.ItemHandlerInterface;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Interceptor used for securing the escidoc framework.
 * <p/>
 * 
 * This Interceptor is invoked every time an EJB calls one of its service
 * classes, except
 * <ul>
 * <li>FedoraManagementDeviationaHandler (as this just delegates to other
 * resource handlers that are secured)</li>
 * <li>FedoraAccessDeviationaHandler (as this just delegates to the item handler
 * that is secured)</li>
 * <li>FedoraDescribeDeviationaHandler (as this handler does not provide any
 * information that has to be secured)</li>
 * </ul>
 * <br>
 * Together with the AA component, this class implements a Policy enforcement
 * point (PEP).
 * 
 * @spring.bean id="security.SecurityInterceptor" factory-method="aspectOf"
 *              lazy-init="false"
 * 
 * @author Roland Werner (Accenture)
 * @aa
 */
@Aspect
@DeclarePrecedence("de.escidoc.core.common.util.aop.StatisticInterceptor, de.escidoc.core.aa.security.aop.AuthenticationInterceptor, de.escidoc.core.common.util.aop.ParameterCheckInterceptor, de.escidoc.core.common.util.aop.XmlValidationInterceptor,*")
public class SecurityInterceptor implements Ordered {

    /**
     * The logger.
     */
    private static final AppLogger LOG =
        new AppLogger(SecurityInterceptor.class.getName());

    /**
     * Pattern used to check that a resource id does not contain the optional
     * version number.
     * 
     * @aa
     */
    private static final Pattern PATTERN_CHECK_MISSING_VERSION_NUMBER =
        Pattern.compile("^[^:]*:[^:]*$");

    private static final String ITEM_HANDLER_CLASS_NAME =
        ItemHandlerInterface.class.getName();

    private static final String CONTAINER_HANDLER_CLASS_NAME =
        ContainerHandlerInterface.class.getName();

    private static final String INTERNAL_UNEXPECTED_ERROR_DURING_AUTHORIZATION =
        "Internal unexpected error during authorization.";

    private InvocationParser invocationParser;

    // used components
    private PolicyDecisionPointInterface pdp;

    private SecurityInterceptorCache cache;

    private TripleStoreUtility tsu;

    /**
     * The error message that is used in the {@link ResourceNotFoundException}
     * to indicate a resource has not been released, yet.
     */
    private static final String ERR_MSG_LATEST_RELEASE_NOT_FOUND =
        "Latest release not found.";

    /**
     * Around advice to perform the authorization of the current request.
     * 
     * This method is called every time the Interceptor is intercepting a method
     * call.
     * 
     * It does the following steps:
     * <ul>
     * <li>Fetch the credentials (techUser, handle) of the current user from
     * class <code>UserContext</code>.</li>
     * <li>Checks the technical username. Has to be either
     * <ul>
     * <li><code>ShibbolethUser</code>, which means that the EJB has been
     * invoked from the <code>EJBProvider</code> class of Axis via a webservice,
     * </li>
     * <li><code>internal</code>, which means that the EJB has been called
     * internally from another component and <code>INTERNAL_INTERCEPTION</code>
     * is turned off, or</li>
     * <li><code>authorization</code>, which means that the EJB has been called
     * internally from the authorization component.</li>
     * </ul>
     * <li>In case the technical username is <code>internal</code>, no further
     * security checks are done, the intercepted method is invoked and its
     * return value is returned to the originally invoking method.</li>
     * <li>In case the technical username is <code>ShibbolethUser</code>, the
     * following steps are executed.</li>
     * <li>The private method <code>doAuthentication</code> is called, which
     * returns the "real" username for the handle fetched from
     * <code>UserContext</code>.</li>
     * <li>The private method <code>doAuthorisation</code> is called, which
     * calls the XACML engine with the current input parameters in order to
     * decide whether invoking the intercepted method is permitted or denied. In
     * case of denial, an exception is thrown.</li>
     * <li>The intercepted method is invoked, returning some return values.</li>
     * <li>If the return values are a list of objects, these have to filtered
     * before returned to the invoking EJB. For this the private method
     * <code>doFiltering</code> is called, which returns the (filtered) return
     * value of the intercepted method.</li>
     * <li>The (filtered) return value of the intercepted method is returned
     * back to the invoking EJB.</li>
     * </ul>
     * 
     * @param joinPoint
     *            The current {@link JoinPoint}.
     * @throws Throwable
     *             Thrown in case of an error.
     * @common
     */
//    @Around("call(public !static * de.escidoc.core.*.service.interfaces.*.*(..))"
//        + " && within(de.escidoc.core.*.ejb.*Bean)")
    @Around("call(public !static * de.escidoc.core.*.service.interfaces.*.*(..))")
    public Object authorize(final ProceedingJoinPoint joinPoint)
        throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug(StringUtility.format(
                "authorize", this));
        }
        final MethodSignature methodSignature =
            ((MethodSignature) joinPoint.getSignature());
        final Method calledMethod = methodSignature.getMethod();
        final String target = methodSignature.getDeclaringTypeName();
        String methodName = calledMethod.getName();
        final String handle = UserContext.getHandle();

        // -------------------
        // --- Preparation ---
        // -------------------

        if (LOG.isDebugEnabled()) {
            LOG.debug(StringUtility.concatenateWithColonToString("The callee",
                target));
            LOG.debug(StringUtility.concatenateWithColonToString("Method name",
                methodName));
            LOG.debug(StringUtility.concatenateWithColonToString(
                "The handle/password", handle));
        }

        final Object[] arguments = joinPoint.getArgs();
        if (LOG.isDebugEnabled()) {
            if (arguments.length > 0) {
                LOG.debug(StringUtility.concatenateWithColon("First Argument",
                        arguments[0]).toString());
            }
            else {
                LOG.debug("Method called without arguments.");
            }
        }

        // ---------------------
        // --- Authorization ---
        // ---------------------
        // authorization is not performed if the current request is executed as
        // an internal user. Only external users are authorized.

        if (!UserContext.isInternalUser()) {

            // Calls from the authorization component to other components run
            // with privileges of the internal authorization user (superuser).
            // They will not be further intercepted.
            UserContext.runAsInternalUser();
            doAuthorisation(target, methodName, arguments);

            // --------------------
            // --- Continuation ---
            // --------------------
            // if everything is fine, finally call the method.
            // This method runs with privileges of an internal user that will
            // not be
            // further intercepted, as the access to the resource has been
            // granted,
            // now.
        }

        try {
            return proceed(joinPoint);
        }
        catch (ResourceNotFoundException e) {
            // see issue 475, 500
            // this exception may be thrown if the user tries to access
            // a versionized resource without providing the version number.
            // If the access is denied for the latest version, the business
            // logic is asked to retrieve the latest release. If no release
            // exists, a Resource not found exception is thrown containing
            // an error message indicating the missing release.
            // As this is an authorization failure, this kind of
            // ResourceNotFoundException must be caught and a
            // AuthorizationException has to be thrown, instead
            if (UserContext.isRetrieveRestrictedToReleased()
                && ERR_MSG_LATEST_RELEASE_NOT_FOUND.equals(e.getMessage())) {
                throw createAuthorizationException(target, methodName);
            }
            else {
                throw e;
            }
        }
    }

    /**
     * Continue the invocation.
     * 
     * @param joinPoint
     *            The current {@link ProceedingJoinPoint}.
     * @return Returns the result of the continued invocation.
     * @throws Throwable
     *             Thrown in case of an error during proceeding the method call.
     */
    private static Object proceed(final ProceedingJoinPoint joinPoint)
        throws Throwable {

        return joinPoint.proceed();
    }

    // CHECKSTYLE:JAVADOC-OFF

    /**
     * See Interface for functional description.
     * 
     * @return
     * @see org.springframework.core.Ordered#getOrder()
     * @common
     */
    public final int getOrder() {

        return AopUtil.PRECEDENCE_SECURITY_INTERCEPTOR;
    }

    // CHECKSTYLE:JAVADOC-ON

    /**
     * Does the authorization part of the interception.
     * <p/>
     * 
     * In detail, the following steps are executed:
     * <ul>
     * <li>Calls <code>retrieveMethodMapping</code> of the
     * PolicyDecisionPointBean, providing the name of the intercepted method.
     * Receives one or two <code>MethodMapping</code> objects, of which the
     * first is used for this method, the second is returned back to the
     * <code>invoke</code> method. A <code>MethodMapping</code> contains the
     * information, which input parameter of the intercepted method are relevant
     * for the creation of the XACML request.</li>
     * <li>The helper class <code>InvocationParser</code> is used to create a
     * <code>RequestVo</code> object from the method input parameters, which
     * contains the data needed to build an XACML request.</li>
     * <li>Calls <code>checkUserPrivilege</code> of the PolicyDecisionPointBean,
     * providing the <code>RequestVo</code> in order to decide on the
     * authorization of the current method invocation.</li>
     * <li>If the invocation of the intercepted method is not authorized, an
     * exception is thrown.</li>
     * </ul>
     * 
     * @param className
     *            name of the called class.
     * @param methodName
     *            The called method name.
     * @param arguments
     *            The arguments of the current call.
     * @return The MethodMapping object for after-call, in order to be reused
     *         during filtering and don't have to be fetched twice from the
     *         database. If filtering is not needed, <code>null</code> is
     *         returned.
     * @throws AuthorizationException
     *             Thrown if authorization fails.
     * @throws WebserverSystemException
     *             Thrown in case of an internal error.
     * @throws ResourceNotFoundException
     *             Thrown if a resource that shall be accessed cannot be found.
     * @throws MissingMethodParameterException
     *             Thrown if an argument has not been provided but is needed for
     *             authorization.
     * @throws MissingAttributeValueException
     *             Thrown if an argument does not contain an attribute needed
     *             for authorization.
     * @throws MissingElementValueException
     *             Thrown if an argument does not contain an element needed for
     *             authorization.
     * @throws InvalidXmlException
     *             Thrown if an argument is expected to contain XML data but
     *             cannot be parsed.
     * @see InvocationParser
     * @see MethodMapping
     * @aa
     */
    private MethodMappingList doAuthorisation(
        final String className, final String methodName,
        final Object[] arguments) throws AuthorizationException,
        WebserverSystemException, ResourceNotFoundException,
        MissingMethodParameterException, MissingAttributeValueException,
        MissingElementValueException, InvalidXmlException {

        MethodMapping methodMapping = null;
        MethodMappingList methodMappings = null;
        try {
            methodMappings = cache.getMethodMappings(className, methodName);

            // collect all data needed for the creation of a before-request
            // from the method-call (name, arguments etc.)
            // TODO: maybe the interface of AA should be changed to support
            // this?
            // FIXME: one PDP call for all method mappings
            boolean methodMappingsExist = false;
            for (int i = 0; i < methodMappings.sizeBefore(); i++) {
                methodMappingsExist = true;
                methodMapping = methodMappings.getBefore(i);
                final List<Map<String, String>> requests =
                    getInvocationParser().buildRequestsList(arguments,
                        methodMapping);

                // try to authorize the user
                // throws an AuthorizationException if not authorized
                boolean[] accessAllowedArray =
                    getPdp().evaluateRequestList(requests);
                if (accessAllowedArray == null || accessAllowedArray.length == 0) {
                    throw createAuthorizationException(className,
                            methodName);
                }
                for (boolean anAccessAllowedArray : accessAllowedArray) {
                    if (!anAccessAllowedArray) {
                        throw createAuthorizationException(className,
                                methodName);
                    }
                }
            }
            if (!methodMappingsExist) {
                getPdp().touch();
            }

            return methodMappings;
        }
        catch (AuthorizationException e) {
            // in case of a retrieve request for a versionized object (currently
            // container and item), the failed check says it is not allowed for
            // the user to retrieve the specified version if it has been
            // specified. Otherwise it says it is not allowed for the user to
            // retrieve the latest version. In the latter case it is allowed to
            // him to retrieve the latest released version of the object, as
            // this
            // is a default privilege for any user.
            // This situation is marked in the UserContext and the retrieve
            // is allowed with the restriction to released versions.
            // Otherwise, the exception is thrown.

            // FIXME: see issue 500
            // this is NOT true for retrieve-content! And for retrieve-item it
            // is NOT true if the default-policies have to be changed!!!

            if (methodName.startsWith("retrieve")
                && PATTERN_CHECK_MISSING_VERSION_NUMBER.matcher(
                    (String) arguments[0]).find()
                && (className.equals(CONTAINER_HANDLER_CLASS_NAME) || className
                    .equals(ITEM_HANDLER_CLASS_NAME))) {

                try {
                    String latestReleaseVersionNumber =
                        getTsu().getPropertiesElements((String) arguments[0],
                            TripleStoreUtility.PROP_LATEST_RELEASE_NUMBER);
                    if (latestReleaseVersionNumber == null) {
                        throw e;
                    }
                    Object[] replacedArguments = arguments;
                    replacedArguments[0] =
                        arguments[0] + ":"
                            + latestReleaseVersionNumber;
                    doAuthorisation(className, methodName, replacedArguments);
                }
                catch (TripleStoreSystemException ex) {
                    throw e;
                }

                UserContext
                    .setRestrictedPermissions(UserContext.RESTRICTED_PERMISSION_RELEASES_ONLY);
                return methodMappings;
            }
            else {
                throw e;
            }
        }
        catch (MissingMethodParameterException e) {
            throw e;
        }
        catch (MissingAttributeValueException e) {
            throw e;
        }
        catch (MissingElementValueException e) {
            throw e;
        }
        catch (InvalidXmlException e) {
            throw e;
        }
        catch (ResourceNotFoundException e) {
            throw determineResourceNotFoundException(methodMapping, e);
        }
        catch (WebserverSystemException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.error(INTERNAL_UNEXPECTED_ERROR_DURING_AUTHORIZATION, e);
            throw new WebserverSystemException(
                INTERNAL_UNEXPECTED_ERROR_DURING_AUTHORIZATION, e);
        }
    }

    /**
     * Creates the {@link AuthorizationException} indicating a denied access.
     * 
     * @param className
     *            The name of the class.
     * @param methodName
     *            The name of the method.
     * @return Returns the {@link CreateDestMsg} {@link AuthorizationException}.
     * @throws WebserverSystemException
     *             Thrown in case of an internal error.
     */
    private static AuthorizationException createAuthorizationException(
            final String className, final String methodName)
        throws WebserverSystemException {

        return new AuthorizationException(StringUtility
                .format("Access denied", className, methodName,
                        UserContext.getHandle()));
    }

    /**
     * Determines the correct resource not found exception sub class.
     * 
     * @param methodMapping
     *            The currently checked methodMapping that raised the
     *            ResourceNotFoundException. This must not be <code>null</code>.
     * @param e
     *            The ResourceNotFoundException
     * @return Returns the determined sub class instance or the original
     *         exception if no sub class could be determined.
     * @throws WebserverSystemException
     *             Thrown in case of an identified error with a method mapping.
     * @common
     */
    private static ResourceNotFoundException determineResourceNotFoundException(
            final MethodMapping methodMapping, final ResourceNotFoundException e)
        throws WebserverSystemException {

        if (methodMapping == null) {
            throw new WebserverSystemException("No method mapping provided.");
        }

        if (!e.getClass().equals(ResourceNotFoundException.class)) {
            return e;
        }

        String exceptionName = methodMapping.getResourceNotFoundException();
        if (exceptionName == null) {
            String errorMsg =
                StringUtility.format(
                    "Error in method mapping, missing specified"
                        + " ResourceNotFoundException", methodMapping.getId());
            LOG.error(errorMsg);
            throw new WebserverSystemException(errorMsg);
        }
        Constructor<ResourceNotFoundException> constructor;
        try {
            constructor =
                (Constructor<ResourceNotFoundException>) Class.forName(
                    exceptionName).getConstructor(new Class[] { String.class });
            String msg = e.getMessage();
            ResourceNotFoundException resourceNotFoundException =
                (constructor.newInstance(msg));
            return resourceNotFoundException;
        }
        catch (Exception e1) {
            StringBuilder errorMsg =
                    new StringBuilder("Error in method mapping. Specified");
            errorMsg.append(" ResourceNotFoundException is unknown or cannot ");
            errorMsg.append(" be instantiated using the constructor ");
            errorMsg.append(exceptionName);
            errorMsg.append("(java.lang.String)");
            errorMsg.append(" [");
            errorMsg.append(exceptionName);
            errorMsg.append(", id=");
            errorMsg.append(methodMapping.getId());
            errorMsg.append("]. Error message of ResourceNotFoundException");
            errorMsg.append(" was: ");
            errorMsg.append(e.getMessage());
            LOG.error(errorMsg.toString(), e1);
            LOG.error("Original exception was:");
            LOG.error(e);
            throw new WebserverSystemException(errorMsg.toString(), e1);
        }
    }

    /**
     * Gets the invocation parser.
     * 
     * @return Returns the invocation parser.
     * @common
     */
    private InvocationParser getInvocationParser() {

        return invocationParser;
    }

    /**
     * Injects the {@link InvocationParser}.
     * 
     * @param invocationParser
     *            The {@link InvocationParser} to be injected.
     * @spring.property ref="eSciDoc.core.common.helper.InvocationParser"
     * @common
     */
    public void setInvocationParser(final InvocationParser invocationParser) {

        LOG.debug("setInvocationParser");

        this.invocationParser = invocationParser;
    }

    /**
     * Gets the policy decision point (PDP).
     * 
     * @return Returns the PDP.
     * @common
     */
    private PolicyDecisionPointInterface getPdp() {

        return pdp;
    }

    /**
     * Injects the policy decision point.
     * 
     * @param pdp
     *            The {@link PolicyDecisionPointInterface} implementation to be
     *            injected.
     * @spring.property ref="service.PolicyDecisionPoint"
     * @common
     */
    public void setPdp(final PolicyDecisionPointInterface pdp) {

        LOG.debug("setPdp");

        this.pdp = pdp;
    }

    /**
     * Injects the {@link SecurityInterceptorCache}.
     * 
     * @param cache
     *            The {@link SecurityInterceptorCache} to be injected.
     * @spring.property ref="eSciDoc.core.common.SecurityInterceptorCache"
     * @common
     */
    public void setCache(final SecurityInterceptorCache cache) {

        LOG.debug("setCache");

        this.cache = cache;
    }

    /**
     * Gets the triple store utility bean.
     * 
     * @return Returns the tsu.
     * @common
     */
    private TripleStoreUtility getTsu() {

        return tsu;
    }

    /**
     * Injects the triple store utility bean.
     * 
     * @param tsu
     *            The {@link TripleStoreUtility}.
     * @spring.property ref="business.TripleStoreUtility"
     * @aa
     */
    public void setTsu(final TripleStoreUtility tsu) {

        LOG.debug("setTsu");

        this.tsu = tsu;
    }

}
