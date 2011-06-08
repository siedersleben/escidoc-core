package org.escidoc.core.services.fedora;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.escidoc.core.services.fedora.access.ObjectDatastreamsTO;
import org.escidoc.core.services.fedora.access.ObjectProfileTO;
import org.escidoc.core.services.fedora.management.DatastreamHistoryTO;
import org.escidoc.core.services.fedora.management.DatastreamProfileTO;
import org.esidoc.core.utils.io.MimeTypes;
import org.esidoc.core.utils.io.Stream;

/**
 * Service to access the Fedora repository.
 * 
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
public interface FedoraServiceRESTEndpoint {

    @POST
    @Path("/objects/nextPID")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    PidListTO getNextPID(@NotNull @PathParam("") NextPIDPathParam path, @NotNull @QueryParam("") NextPIDQueryParam query);

    @POST
    @Path("/objects/{pid}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    void createObject(
        @NotNull @PathParam("") CreateObjectPathParam path, @NotNull @QueryParam("") CreateObjectQueryParam query);

    @GET
    @Path("/objects/{pid}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    ObjectProfileTO getObjectProfile(
        @NotNull @PathParam("") GetObjectProfilePathParam path,
        @NotNull @QueryParam("") GetObjectProfileQueryParam query);

    @PUT
    @Path("/objects/{pid}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    void updateObject(
        @NotNull @PathParam("") UpdateObjectPathParam path, @NotNull @QueryParam("") UpdateObjectQueryParam query);

    @DELETE
    @Path("/objects/{pid}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    void deleteObject(
        @NotNull @PathParam("") DeleteObjectPathParam path, @NotNull @QueryParam("") DeleteObjectQueryParam query);

    @GET
    @Path("/objects/{pid}/objectXML")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    DigitalObjectTO getObjectXML(
        @NotNull @PathParam("") GetObjectXMLPathParam path, @NotNull @QueryParam("") GetObjectXMLQueryParam query);

    @POST
    @Path("/objects/{pid}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    String ingest(
        @NotNull @PathParam("") IngestPathParam path, @NotNull @QueryParam("") IngestQueryParam query,
        @NotNull DigitalObjectTypeTOExtension digitalObjectTO);

    @POST
    @Path("/objects/{pid}/datastreams/{dsID}")
    @Produces(MimeTypes.ALL)
    @Consumes(MimeTypes.TEXT_XML)
    DatastreamProfileTO addDatastream(
        @NotNull @PathParam("") AddDatastreamPathParam path, @NotNull @QueryParam("") AddDatastreamQueryParam query,
        Stream inputStream);

    @GET
    @Path("/objects/{pid}/datastreams/{dsID}/content")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.ALL)
    Stream getDatastream(
        @NotNull @PathParam("") GetDatastreamPathParam path, @NotNull @QueryParam("") GetDatastreamQueryParam query);

    @GET
    @Path("/objects/{pid}/datastreams/{dsID}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    DatastreamProfileTO getDatastreamProfile(
        @NotNull @PathParam("") GetDatastreamProfilePathParam path,
        @NotNull @QueryParam("") GetDatastreamProfileQueryParam query);

    @PUT
    @Path("/objects/{pid}/datastreams/{dsID}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.ALL)
    DatastreamProfileTO modifyDatastream(
        @NotNull @PathParam("") ModifiyDatastreamPathParam path,
        @NotNull @QueryParam("") ModifyDatastreamQueryParam query, @NotNull Stream stream);

    @DELETE
    @Path("/objects/{pid}/datastreams/{dsID}")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.APPLICATION_JSON)
    void deleteDatastream(
        @NotNull @PathParam("") DeleteDatastreamPathParam path,
        @NotNull @QueryParam("") DeleteDatastreamQueryParam query);

    @GET
    @Path("/objects/{pid}/datastreams")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    ObjectDatastreamsTO listDatastreams(
        @NotNull @PathParam("") ListDatastreamsPathParam path, @NotNull @QueryParam("") ListDatastreamsQueryParam query);

    @GET
    @Path("/objects/{pid}/datastreams/{dsID}/history")
    @Produces(MimeTypes.TEXT_XML)
    @Consumes(MimeTypes.TEXT_XML)
    DatastreamHistoryTO getDatastreamHistory(
        @NotNull @PathParam("") GetDatastreamHistoryPathParam path,
        @NotNull @QueryParam("") GetDatastreamHistoryQueryParam query);

    @GET
    @Path("/risearch")
    @Produces(MimeTypes.ALL)
    @Consumes(MimeTypes.ALL)
    void sync(@NotNull @PathParam("") SyncPathParam path,
              @NotNull @QueryParam("") SyncQueryParam query);

}