/**
 * 
 */
package de.escidoc.core.sm;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBElement;

import org.escidoc.core.domain.sm.ReportDefinitionListTO;
import org.escidoc.core.domain.sru.ResponseType;
import org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean;
import org.escidoc.core.utils.io.MimeTypes;

import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;

/**
 * @author Michael Hoppe
 * 
 */

@Path("/")
@Produces(MimeTypes.TEXT_XML)
@Consumes(MimeTypes.TEXT_XML)
public interface ReportDefinitionsRestService {

    @GET
    JAXBElement<? extends ResponseType> retrieveReportDefinitions(@QueryParam("") SruSearchRequestParametersBean parameters, @QueryParam("x-info5-roleId") String roleId,
        @QueryParam("x-info5-userId") String userId, @QueryParam("x-info5-omitHighlighting") String omitHighlighting) throws InvalidSearchQueryException,
    MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;

}
