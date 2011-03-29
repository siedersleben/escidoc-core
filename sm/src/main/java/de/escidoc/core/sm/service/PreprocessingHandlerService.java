package de.escidoc.core.sm.service;

import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import java.lang.Boolean;
import java.lang.String;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Service endpoint interface for PreprocessingHandler.
 */
public interface PreprocessingHandlerService extends Remote {

    void preprocess(String aggregationDefinitionId, String xmlData,
                    SecurityContext securityContext)
            throws AuthenticationException,
            AuthorizationException,
            XmlSchemaValidationException,
            XmlCorruptedException,
            MissingMethodParameterException,
            SystemException, RemoteException;

    void preprocess(String aggregationDefinitionId, String xmlData, String authHandle,
                    Boolean restAccess)
            throws AuthenticationException,
            AuthorizationException,
            XmlSchemaValidationException,
            XmlCorruptedException,
            MissingMethodParameterException,
            SystemException, RemoteException;

}
