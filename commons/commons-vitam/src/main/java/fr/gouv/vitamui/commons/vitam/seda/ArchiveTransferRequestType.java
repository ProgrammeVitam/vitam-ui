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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour ArchiveTransferRequestType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArchiveTransferRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}BusinessRequestMessageType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RelatedTransferReference" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="TransferDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="ArchivalAgency" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationWithIdType"/&gt;
 *         &lt;element name="TransferringAgency" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationWithIdType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchiveTransferRequestType", propOrder = {
    "relatedTransferReference",
    "transferDate",
    "archivalAgency",
    "transferringAgency"
})
public class ArchiveTransferRequestType
    extends BusinessRequestMessageType
{

    @XmlElement(name = "RelatedTransferReference")
    protected List<IdentifierType> relatedTransferReference;
    @XmlElement(name = "TransferDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar transferDate;
    @XmlElement(name = "ArchivalAgency", required = true)
    protected OrganizationWithIdType archivalAgency;
    @XmlElement(name = "TransferringAgency", required = true)
    protected OrganizationWithIdType transferringAgency;

    /**
     * Gets the value of the relatedTransferReference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedTransferReference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedTransferReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IdentifierType }
     * 
     * 
     */
    public List<IdentifierType> getRelatedTransferReference() {
        if (relatedTransferReference == null) {
            relatedTransferReference = new ArrayList<IdentifierType>();
        }
        return this.relatedTransferReference;
    }

    /**
     * Obtient la valeur de la propriété transferDate.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTransferDate() {
        return transferDate;
    }

    /**
     * Définit la valeur de la propriété transferDate.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTransferDate(XMLGregorianCalendar value) {
        this.transferDate = value;
    }

    /**
     * Obtient la valeur de la propriété archivalAgency.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public OrganizationWithIdType getArchivalAgency() {
        return archivalAgency;
    }

    /**
     * Définit la valeur de la propriété archivalAgency.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public void setArchivalAgency(OrganizationWithIdType value) {
        this.archivalAgency = value;
    }

    /**
     * Obtient la valeur de la propriété transferringAgency.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public OrganizationWithIdType getTransferringAgency() {
        return transferringAgency;
    }

    /**
     * Définit la valeur de la propriété transferringAgency.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public void setTransferringAgency(OrganizationWithIdType value) {
        this.transferringAgency = value;
    }

}
