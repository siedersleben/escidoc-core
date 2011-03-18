/*
 * Generated by XDoclet - Do not edit!
 */
package de.escidoc.core.sm.ejb.interfaces;

import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.AggregationDefinitionNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ScopeNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;
import java.util.Map;

/**
 * Local interface for AggregationDefinitionHandler.
 */
public interface AggregationDefinitionHandlerLocal extends EJBLocalObject {

    String create(String xmlData, SecurityContext securityContext)
            throws AuthenticationException,
            AuthorizationException,
            XmlSchemaValidationException,
            XmlCorruptedException,
            MissingMethodParameterException,
            ScopeNotFoundException,
            SystemException;

    String create(String xmlData, String authHandle, Boolean restAccess)
            throws AuthenticationException,
            AuthorizationException,
            XmlSchemaValidationException,
            XmlCorruptedException,
            MissingMethodParameterException,
            ScopeNotFoundException,
            SystemException;

    void delete(String id, SecurityContext securityContext)
            throws AuthenticationException,
            AuthorizationException,
            AggregationDefinitionNotFoundException,
            MissingMethodParameterException,
            SystemException;

    void delete(String id, String authHandle, Boolean restAccess)
            throws AuthenticationException,
            AuthorizationException,
            AggregationDefinitionNotFoundException,
            MissingMethodParameterException,
            SystemException;

    String retrieve(String id, SecurityContext securityContext)
            throws AuthenticationException,
            AuthorizationException,
            AggregationDefinitionNotFoundException,
            MissingMethodParameterException,
            SystemException;

    String retrieve(String id, String authHandle, Boolean restAccess)
            throws AuthenticationException,
            AuthorizationException,
            AggregationDefinitionNotFoundException,
            MissingMethodParameterException,
            SystemException;

    String retrieveAggregationDefinitions(Map parameters, SecurityContext securityContext)
            throws InvalidSearchQueryException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException;

    String retrieveAggregationDefinitions(Map parameters, String authHandle, Boolean restAccess)
            throws InvalidSearchQueryException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException;

}
