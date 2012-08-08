//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.25 at 10:57:01 AM CEST 
//


package org.escidoc.core.domain.properties.java;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.escidoc.core.domain.properties.java package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Comment_QNAME = new QName("", "comment");
    private final static QName _Entry_QNAME = new QName("", "entry");
    private final static QName _Properties_QNAME = new QName("", "properties");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.escidoc.core.domain.properties.java
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EntryTypeTO }
     * 
     */
    public EntryTypeTO createEntryTypeTO() {
        return new EntryTypeTO();
    }

    /**
     * Create an instance of {@link PropertiesTypeTO }
     * 
     */
    public PropertiesTypeTO createPropertiesTypeTO() {
        return new PropertiesTypeTO();
    }

    /**
     * Create an instance of {@link CommentTypeTO }
     * 
     */
    public CommentTypeTO createCommentTypeTO() {
        return new CommentTypeTO();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CommentTypeTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "comment")
    public JAXBElement<CommentTypeTO> createComment(CommentTypeTO value) {
        return new JAXBElement<CommentTypeTO>(_Comment_QNAME, CommentTypeTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EntryTypeTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "entry")
    public JAXBElement<EntryTypeTO> createEntry(EntryTypeTO value) {
        return new JAXBElement<EntryTypeTO>(_Entry_QNAME, EntryTypeTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PropertiesTypeTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "properties")
    public JAXBElement<PropertiesTypeTO> createProperties(PropertiesTypeTO value) {
        return new JAXBElement<PropertiesTypeTO>(_Properties_QNAME, PropertiesTypeTO.class, null, value);
    }

}