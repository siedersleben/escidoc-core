/*
 * Generated by XDoclet - Do not edit!
 */
package de.escidoc.core.sm.service;

/**
 * Service endpoint interface for ReportHandler.
 */
public interface ReportHandlerService extends java.rmi.Remote {

    java.lang.String retrieve(java.lang.String xml,
                              org.springframework.security.context.SecurityContext securityContext)
            throws de.escidoc.core.common.exceptions.application.security.AuthenticationException,
            de.escidoc.core.common.exceptions.application.security.AuthorizationException,
            de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException,
            de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException,
            de.escidoc.core.common.exceptions.application.notfound.ReportDefinitionNotFoundException,
            de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException,
            de.escidoc.core.common.exceptions.application.invalid.InvalidSqlException,
            de.escidoc.core.common.exceptions.system.SystemException, java.rmi.RemoteException;

    java.lang.String retrieve(java.lang.String xml, java.lang.String authHandle, java.lang.Boolean restAccess)
            throws de.escidoc.core.common.exceptions.application.security.AuthenticationException,
            de.escidoc.core.common.exceptions.application.security.AuthorizationException,
            de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException,
            de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException,
            de.escidoc.core.common.exceptions.application.notfound.ReportDefinitionNotFoundException,
            de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException,
            de.escidoc.core.common.exceptions.application.invalid.InvalidSqlException,
            de.escidoc.core.common.exceptions.system.SystemException, java.rmi.RemoteException;

}
