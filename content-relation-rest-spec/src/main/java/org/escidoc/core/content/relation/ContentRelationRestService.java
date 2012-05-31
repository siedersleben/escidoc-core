/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
 * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
 * terms.
 */

package org.escidoc.core.content.relation;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.escidoc.core.domain.content.relation.ContentRelationPropertiesTO;
import org.escidoc.core.domain.content.relation.ContentRelationTO;
import org.escidoc.core.domain.content.relation.ContentRelationResourcesTO;
import org.escidoc.core.domain.metadatarecords.MdRecordTO;
import org.escidoc.core.domain.metadatarecords.MdRecordsTO;
import org.escidoc.core.domain.predicate.list.PredicatesTO;
import org.escidoc.core.domain.result.ResultTO;
import org.escidoc.core.domain.taskparam.assignpid.AssignPidTaskParamTO;
import org.escidoc.core.domain.taskparam.optimisticlocking.OptimisticLockingTaskParamTO;
import org.escidoc.core.domain.taskparam.status.StatusTaskParamTO;

import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.application.violated.LockingException;
import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
import de.escidoc.core.common.exceptions.system.SystemException;

@Path("/ir/content-relation")
public interface ContentRelationRestService {

    @PUT
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ContentRelationTO create(ContentRelationTO contentRelationTO) throws SystemException, InvalidContentException,
        MissingAttributeValueException, RelationPredicateNotFoundException, InvalidXmlException,
        ReferencedResourceNotFoundException, MissingMethodParameterException, AuthorizationException,
        AuthenticationException;

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    ContentRelationTO retrieve(@PathParam("id") String id) throws SystemException, ContentRelationNotFoundException,
        AuthorizationException, AuthenticationException;

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ContentRelationTO update(@PathParam("id") String id, ContentRelationTO contentRelationTO) throws SystemException,
        InvalidContentException, OptimisticLockingException, MissingAttributeValueException,
        RelationPredicateNotFoundException, InvalidStatusException, ContentRelationNotFoundException,
        InvalidXmlException, ReferencedResourceNotFoundException, LockingException, MissingMethodParameterException,
        AuthorizationException, AuthenticationException;

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") String id) throws SystemException, ContentRelationNotFoundException, LockingException,
        AuthorizationException, AuthenticationException;

    @GET
    @Path("{id}/properties")
    @Produces(MediaType.TEXT_XML)
    ContentRelationPropertiesTO retrieveProperties(@PathParam("id") String id) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, SystemException;

    @POST
    @Path("{id}/lock")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultTO lock(@PathParam("id") String id, OptimisticLockingTaskParamTO optimisticLockingTaskParamTO) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidContentException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidStatusException;

    @POST
    @Path("{id}/unlock")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultTO unlock(@PathParam("id") String id, OptimisticLockingTaskParamTO optimisticLockingTaskParamTO) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, MissingMethodParameterException,
        SystemException, OptimisticLockingException, InvalidXmlException, InvalidContentException,
        InvalidStatusException;

    @POST
    @Path("{id}/submit")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultTO submit(@PathParam("id") String id, StatusTaskParamTO statusTaskParamTO) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidContentException;

    @POST
    @Path("{id}/revise")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultTO revise(@PathParam("id") String id, StatusTaskParamTO statusTaskParamTO) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, XmlCorruptedException,
        InvalidContentException;

    @POST
    @Path("{id}/release")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultTO release(@PathParam("id") String id, StatusTaskParamTO statusTaskParamTO) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, LockingException, InvalidStatusException,
        MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
        InvalidContentException;

    @POST
    @Path("{id}/assign-object-pid")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultTO assignObjectPid(@PathParam("id") String id, AssignPidTaskParamTO assignPidTaskParamTO)
        throws AuthenticationException, AuthorizationException, ContentRelationNotFoundException, LockingException,
        MissingMethodParameterException, OptimisticLockingException, InvalidXmlException, SystemException,
        PidAlreadyAssignedException;

    @GET
    @Path("{id}/md-records")
    @Produces(MediaType.TEXT_XML)
    MdRecordsTO retrieveMdRecords(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
        ContentRelationNotFoundException, SystemException;

    @GET
    @Path("{id}/md-records/md-record/{name}")
    @Produces(MediaType.TEXT_XML)
    MdRecordTO retrieveMdRecord(@PathParam("id") String id, @PathParam("name") String name)
        throws AuthenticationException, AuthorizationException, ContentRelationNotFoundException,
        MdRecordNotFoundException, SystemException;

    @GET
    @Path("/content-relations/retrieve-registered-predicates")
    @Produces(MediaType.TEXT_XML)
    PredicatesTO retrieveRegisteredPredicates() throws InvalidContentException, InvalidXmlException, SystemException;

    @GET
    @Path("/{id}/resources")
    @Produces(MediaType.TEXT_XML)
    ContentRelationResourcesTO retrieveResources(@PathParam("id") String id) throws AuthenticationException,
        AuthorizationException, ContentRelationNotFoundException, MissingMethodParameterException, SystemException;

}