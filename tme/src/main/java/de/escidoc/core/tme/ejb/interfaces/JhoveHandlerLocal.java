/*
 * Generated by XDoclet - Do not edit!
 */
package de.escidoc.core.tme.ejb.interfaces;

import de.escidoc.core.common.exceptions.application.invalid.TmeException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBLocalObject;

/**
 * Local interface for JhoveHandler.
 */
public interface JhoveHandlerLocal extends EJBLocalObject {

    String extract(String requests, SecurityContext securityContext)
            throws AuthenticationException,
            AuthorizationException,
            XmlCorruptedException,
            XmlSchemaValidationException,
            MissingMethodParameterException,
            SystemException,
            TmeException;

    String extract(String requests, String authHandle, Boolean restAccess)
            throws AuthenticationException,
            AuthorizationException,
            XmlCorruptedException,
            XmlSchemaValidationException,
            MissingMethodParameterException,
            SystemException,
            TmeException;

}
