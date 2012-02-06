/**
 * 
 */
package de.escidoc.core.context.internal;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.escidoc.core.domain.service.ServiceUtility;
import org.escidoc.core.domain.sru.RequestType;
import org.escidoc.core.domain.sru.ResponseType;
import org.escidoc.core.domain.sru.parameters.SruRequestTypeFactory;
import org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.util.service.KeyValuePair;
import de.escidoc.core.context.ContextsRestService;
import de.escidoc.core.om.service.interfaces.ContextHandlerInterface;

/**
 * @author Marko VoÃŸ
 * 
 */
public class ContextsRestServiceImpl implements ContextsRestService {

    @Autowired
    @Qualifier("service.ContextHandler")
    private ContextHandlerInterface contextHandler;

    /**
     * 
     */
    protected ContextsRestServiceImpl() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.core.context.ContextsRestService#retrieveContexts(java.util.String, java.util.String, java.util.String,
     * java.util.String, java.util.String, java.util.String, java.util.String, java.util.String, java.util.String,
     * java.util.String, java.util.String, java.util.String, java.util.String, java.util.String, java.util.String, java.util.String, java.util.String)
     */
    @Override
    public JAXBElement<? extends ResponseType> retrieveContexts(
        final String operation,
        final String version,
        final String query,
        final String startRecord,
        final String maximumRecords,
        final String recordPacking,
        final String recordSchema,
        final String recordXPath,
        final String resultSetTTL,
        final String sortKeys,
        final String stylesheet,
        final String scanClause,
        final String responsePosition,
        final String maximumTerms, 
        final String roleId, 
        final String userId,
        final String omitHighlighting) throws MissingMethodParameterException, SystemException {

        SruSearchRequestParametersBean parameters =
            new SruSearchRequestParametersBean(operation, version, query, startRecord, maximumRecords, recordPacking,
                recordSchema, recordXPath, resultSetTTL, sortKeys, stylesheet, scanClause, responsePosition,
                maximumTerms);

        final List<KeyValuePair> additionalParams = new LinkedList<KeyValuePair>();
        if (roleId != null) {
            additionalParams.add(new KeyValuePair(Constants.SRU_PARAMETER_ROLE, roleId));
        }
        if (userId != null) {
            additionalParams.add(new KeyValuePair(Constants.SRU_PARAMETER_USER, userId));
        }
        if (omitHighlighting != null) {
            additionalParams.add(new KeyValuePair(Constants.SRU_PARAMETER_OMIT_HIGHLIGHTING, omitHighlighting));
        }

        final JAXBElement<? extends RequestType> requestTO =
            SruRequestTypeFactory.createRequestTO(parameters, additionalParams);

		return ((JAXBElement<? extends ResponseType>) ServiceUtility.fromXML(
				Constants.SRU_CONTEXT_PATH , this.contextHandler
						.retrieveContexts(ServiceUtility.toMap(requestTO))));
    }

}