//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour OrganizationType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="OrganizationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Identifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType"/&gt;
 *         &lt;element name="OrganizationDescriptiveMetadata" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationDescriptiveMetadataType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrganizationType", propOrder = {
    "identifier",
    "organizationDescriptiveMetadata"
})
@XmlSeeAlso({
    OrganizationWithIdType.class
})
public class OrganizationType {

    @XmlElement(name = "Identifier", required = true)
    protected IdentifierType identifier;
    @XmlElement(name = "OrganizationDescriptiveMetadata")
    protected OrganizationDescriptiveMetadataType organizationDescriptiveMetadata;

    /**
     * Obtient la valeur de la propriété identifier.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getIdentifier() {
        return identifier;
    }

    /**
     * Définit la valeur de la propriété identifier.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setIdentifier(IdentifierType value) {
        this.identifier = value;
    }

    /**
     * Obtient la valeur de la propriété organizationDescriptiveMetadata.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationDescriptiveMetadataType }
     *     
     */
    public OrganizationDescriptiveMetadataType getOrganizationDescriptiveMetadata() {
        return organizationDescriptiveMetadata;
    }

    /**
     * Définit la valeur de la propriété organizationDescriptiveMetadata.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationDescriptiveMetadataType }
     *     
     */
    public void setOrganizationDescriptiveMetadata(OrganizationDescriptiveMetadataType value) {
        this.organizationDescriptiveMetadata = value;
    }

}
