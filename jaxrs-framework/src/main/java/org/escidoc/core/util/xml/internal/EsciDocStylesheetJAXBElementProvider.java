package org.escidoc.core.util.xml.internal;

import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;

import de.escidoc.core.common.util.configuration.EscidocConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * eSciDoc specific implementation of {@link JAXBElementProvider}.
 * Generates stylesheet-header with given stylesheet-path
 *
 * @author Michael Hoppe
 */
@Produces({"application/xml", "application/*+xml", "text/xml" })
@Consumes({"application/xml", "application/*+xml", "text/xml" })
@Provider
public class EsciDocStylesheetJAXBElementProvider extends JAXBElementProvider {
    
    private final String XML_HEADERS_PATH = "com.sun.xml.bind.xmlHeaders";

    private JAXBContextProvider jaxbContextProvider;

    @Override
    public void setMarshallerProperties(Map<String, Object> marshallProperties) {
        overrideXmlHeadersProperty(marshallProperties);
        super.setMarshallerProperties(marshallProperties);
    }

    public void setJaxbContextProvider(final JAXBContextProvider jaxbContextProvider) {
        this.jaxbContextProvider = jaxbContextProvider;
    }

    protected JAXBContext getJAXBContext(Class<?> type, Type genericType) throws JAXBException {
        if (this.jaxbContextProvider != null) {
            return this.jaxbContextProvider.getJAXBContext();
        } else {
            return super.getJAXBContext(type, genericType);
        }
    }
    
    /**
     * eSciDoc maintains a configurable property escidoc-core.xslt.std
     * that holds the name of an xml-header stylesheet.
     * This name is set as marshallProperty "com.sun.xml.bind.xmlHeaders" for 
     * the cxf JAXBElementProvider in applicationContext-cxf.xml. 
     * But property has to get surrounded with <?xml-stylesheet type=... or set to empty
     * if escidoc-core.xslt.std is not defined.
     * 
     * @param marshallProperties marshallProperties
     */
    private void overrideXmlHeadersProperty(Map<String, Object> marshallProperties) {
        String xmlHeaders = (String)marshallProperties.get(XML_HEADERS_PATH);
        if (xmlHeaders != null && !xmlHeaders.isEmpty()) {
            if (!(xmlHeaders.startsWith("http://") || xmlHeaders.startsWith("https://"))) {
                String baseurl = EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_BASEURL);
                if (!baseurl.endsWith("/")) {
                    baseurl += "/";
                }

                if (xmlHeaders.startsWith("./")) {
                    xmlHeaders = baseurl + xmlHeaders.substring(2, xmlHeaders.length());
                }
                else if (xmlHeaders.startsWith("/")) {
                    xmlHeaders = baseurl + xmlHeaders.substring(1, xmlHeaders.length());
                }
            }
            marshallProperties.put(XML_HEADERS_PATH, "<?xml-stylesheet type=\"text/xsl\" href=\"" + xmlHeaders + "\"?>\n");
        }
    }
    
}
