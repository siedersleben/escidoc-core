package de.escidoc.core.om.ejb.interfaces;

import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
import de.escidoc.core.common.exceptions.application.invalid.ContextNotEmptyException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.AdminDescriptorNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.ContextNameNotUniqueException;
import de.escidoc.core.common.exceptions.application.violated.LockingException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
import de.escidoc.core.common.exceptions.system.SystemException;
import org.springframework.security.context.SecurityContext;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Remote interface for ContextHandler.
 */
public interface ContextHandlerRemote extends EJBObject {

    String create(String xmlData, SecurityContext securityContext)
            throws MissingMethodParameterException,
            ContextNameNotUniqueException,
            AuthenticationException,
            AuthorizationException,
            SystemException,
            ContentModelNotFoundException,
            ReadonlyElementViolationException,
            MissingAttributeValueException,
            MissingElementValueException,
            ReadonlyAttributeViolationException,
            InvalidContentException,
            OrganizationalUnitNotFoundException,
            InvalidStatusException,
            XmlCorruptedException,
            XmlSchemaValidationException,
            RemoteException;

    String create(String xmlData, String authHandle, Boolean restAccess)
            throws MissingMethodParameterException,
            ContextNameNotUniqueException,
            AuthenticationException,
            AuthorizationException,
            SystemException,
            ContentModelNotFoundException,
            ReadonlyElementViolationException,
            MissingAttributeValueException,
            MissingElementValueException,
            ReadonlyAttributeViolationException,
            InvalidContentException,
            OrganizationalUnitNotFoundException,
            InvalidStatusException,
            XmlCorruptedException,
            XmlSchemaValidationException,
            RemoteException;

    void delete(String id, SecurityContext securityContext)
            throws ContextNotFoundException,
            ContextNotEmptyException,
            MissingMethodParameterException,
            InvalidStatusException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    void delete(String id, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            ContextNotEmptyException,
            MissingMethodParameterException,
            InvalidStatusException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String retrieve(String id, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String retrieve(String id, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String retrieveProperties(String id, SecurityContext securityContext)
            throws ContextNotFoundException,
            SystemException, RemoteException;

    String retrieveProperties(String id, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            SystemException, RemoteException;

    String update(String id, String xmlData, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            InvalidContentException,
            InvalidStatusException,
            AuthenticationException,
            AuthorizationException,
            ReadonlyElementViolationException,
            ReadonlyAttributeViolationException,
            OptimisticLockingException,
            ContextNameNotUniqueException,
            InvalidXmlException,
            MissingElementValueException,
            SystemException, RemoteException;

    String update(String id, String xmlData, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            InvalidContentException,
            InvalidStatusException,
            AuthenticationException,
            AuthorizationException,
            ReadonlyElementViolationException,
            ReadonlyAttributeViolationException,
            OptimisticLockingException,
            ContextNameNotUniqueException,
            InvalidXmlException,
            MissingElementValueException,
            SystemException, RemoteException;

    EscidocBinaryContent retrieveResource(String id, String resourceName, Map parameters,
                                          SecurityContext securityContext)
            throws OperationNotFoundException,
            ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    EscidocBinaryContent retrieveResource(String id, String resourceName, Map parameters, String authHandle,
                                          Boolean restAccess)
            throws OperationNotFoundException,
            ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String retrieveResources(String id, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String retrieveResources(String id, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String open(String id, String taskParam, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            InvalidStatusException,
            AuthenticationException,
            AuthorizationException,
            OptimisticLockingException,
            InvalidXmlException,
            SystemException,
            LockingException,
            StreamNotFoundException, RemoteException;

    String open(String id, String taskParam, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            InvalidStatusException,
            AuthenticationException,
            AuthorizationException,
            OptimisticLockingException,
            InvalidXmlException,
            SystemException,
            LockingException,
            StreamNotFoundException, RemoteException;

    String close(String id, String taskParam, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException,
            OptimisticLockingException,
            InvalidXmlException,
            InvalidStatusException,
            LockingException,
            StreamNotFoundException, RemoteException;

    String close(String id, String taskParam, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException,
            OptimisticLockingException,
            InvalidXmlException,
            InvalidStatusException,
            LockingException,
            StreamNotFoundException, RemoteException;

    String retrieveContexts(Map filter, SecurityContext securityContext)
            throws MissingMethodParameterException,
            SystemException, RemoteException;

    String retrieveContexts(Map filter, String authHandle, Boolean restAccess)
            throws MissingMethodParameterException,
            SystemException, RemoteException;

    String retrieveMembers(String id, Map filter, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            SystemException, RemoteException;

    String retrieveMembers(String id, Map filter, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            SystemException, RemoteException;

    String retrieveAdminDescriptor(String id, String name, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException,
            AdminDescriptorNotFoundException,
            RemoteException;

    String retrieveAdminDescriptor(String id, String name, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException,
            AdminDescriptorNotFoundException,
            RemoteException;

    String retrieveAdminDescriptors(String id, SecurityContext securityContext)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

    String retrieveAdminDescriptors(String id, String authHandle, Boolean restAccess)
            throws ContextNotFoundException,
            MissingMethodParameterException,
            AuthenticationException,
            AuthorizationException,
            SystemException, RemoteException;

}
