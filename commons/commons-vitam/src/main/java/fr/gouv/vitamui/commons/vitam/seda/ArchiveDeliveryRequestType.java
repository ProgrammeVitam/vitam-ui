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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArchiveDeliveryRequestType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArchiveDeliveryRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}BusinessRequestMessageType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Derogation" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="UnitIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="ArchivalAgency" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationWithIdType"/&gt;
 *         &lt;element name="Requester" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationWithIdType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchiveDeliveryRequestType", propOrder = {
    "derogation",
    "unitIdentifier",
    "archivalAgency",
    "requester"
})
public class ArchiveDeliveryRequestType
    extends BusinessRequestMessageType
{

    @XmlElement(name = "Derogation")
    protected boolean derogation;
    @XmlElement(name = "UnitIdentifier", required = true)
    protected List<IdentifierType> unitIdentifier;
    @XmlElement(name = "ArchivalAgency", required = true)
    protected OrganizationWithIdType archivalAgency;
    @XmlElement(name = "Requester", required = true)
    protected OrganizationWithIdType requester;

    /**
     * Obtient la valeur de la propriété derogation.
     * 
     */
    public boolean isDerogation() {
        return derogation;
    }

    /**
     * Définit la valeur de la propriété derogation.
     * 
     */
    public void setDerogation(boolean value) {
        this.derogation = value;
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
     * Obtient la valeur de la propriété requester.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public OrganizationWithIdType getRequester() {
        return requester;
    }

    /**
     * Définit la valeur de la propriété requester.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public void setRequester(OrganizationWithIdType value) {
        this.requester = value;
    }

}
