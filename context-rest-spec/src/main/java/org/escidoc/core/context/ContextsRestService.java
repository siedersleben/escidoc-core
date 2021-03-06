/**
 *
 */
package org.escidoc.core.context;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import net.sf.oval.constraint.NotNull;
import org.escidoc.core.domain.sru.ResponseTypeTO;
import org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean;

import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.system.SystemException;

/**
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 */
@Path("/ir/contexts")
public interface ContextsRestService {

    /**
     * The list of all contexts matching the given filter criteria will be created.
     * <p/>
     * <br/> See chapter "Filters" for detailed information about filter definitions.
     *
     * @param parameters       The Standard SRU Get-Parameters as Object
     * @param userId           The custom SRU Get Parameter x-info5-userId
     * @param roleId           The custom SRU Get Parameter x-info5-roleId
     * @param omitHighlighting The custom SRU Get Parameter x-info5-omitHighlighting
     * @return The XML representation of the the filtered list of contexts corresponding to SRW schema as JAXBElement.
     * @throws MissingMethodParameterException
     *                         If the parameter filter is not given.
     * @throws SystemException Thrown if a framework internal error occurs.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    JAXBElement<? extends ResponseTypeTO> retrieveContexts(
        @NotNull @QueryParam("") SruSearchRequestParametersBean parameters,
        @QueryParam("x-info5-roleId") String roleId, @QueryParam("x-info5-userId") String userId,
        @QueryParam("x-info5-omitHighlighting") String omitHighlighting)
        throws MissingMethodParameterException, SystemException;
}