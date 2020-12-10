//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour AuthorizationRequestContentType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AuthorizationRequestContentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AuthorizationReason" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Comment" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="RequestDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="UnitIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Requester" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationType"/&gt;
 *         &lt;element name="AuthorizationRequestReply" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}BusinessAuthorizationRequestReplyMessageType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthorizationRequestContentType", propOrder = {
    "authorizationReason",
    "comment",
    "requestDate",
    "unitIdentifier",
    "requester",
    "authorizationRequestReply"
})
public class AuthorizationRequestContentType {

    @XmlElement(name = "AuthorizationReason", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String authorizationReason;
    @XmlElement(name = "Comment")
    protected List<TextType> comment;
    @XmlElement(name = "RequestDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar requestDate;
    @XmlElement(name = "UnitIdentifier", required = true)
    protected List<IdentifierType> unitIdentifier;
    @XmlElement(name = "Requester", required = true)
    protected OrganizationType requester;
    @XmlElement(name = "AuthorizationRequestReply")
    protected List<BusinessAuthorizationRequestReplyMessageType> authorizationRequestReply;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Obtient la valeur de la propriété authorizationReason.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorizationReason() {
        return authorizationReason;
    }

    /**
     * Définit la valeur de la propriété authorizationReason.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorizationReason(String value) {
        this.authorizationReason = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getComment() {
        if (comment == null) {
            comment = new ArrayList<TextType>();
        }
        return this.comment;
    }

    /**
     * Obtient la valeur de la propriété requestDate.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRequestDate() {
        return requestDate;
    }

    /**
     * Définit la valeur de la propriété requestDate.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRequestDate(XMLGregorianCalendar value) {
        this.requestDate = value;
    }

    /**
     * Gets the value of the unitIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unitIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnitIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IdentifierType }
     * 
     * 
     */
    public List<IdentifierType> getUnitIdentifier() {
        if (unitIdentifier == null) {
            unitIdentifier = new ArrayList<IdentifierType>();
        }
        return this.unitIdentifier;
    }

    /**
     * Obtient la valeur de la propriété requester.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationType }
     *     
     */
    public OrganizationType getRequester() {
        return requester;
    }

    /**
     * Définit la valeur de la propriété requester.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationType }
     *     
     */
    public void setRequester(OrganizationType value) {
        this.requester = value;
    }

    /**
     * Gets the value of the authorizationRequestReply property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the authorizationRequestReply property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuthorizationRequestReply().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BusinessAuthorizationRequestReplyMessageType }
     * 
     * 
     */
    public List<BusinessAuthorizationRequestReplyMessageType> getAuthorizationRequestReply() {
        if (authorizationRequestReply == null) {
            authorizationRequestReply = new ArrayList<BusinessAuthorizationRequestReplyMessageType>();
        }
        return this.authorizationRequestReply;
    }

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
