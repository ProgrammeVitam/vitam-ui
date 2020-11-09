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
 * <p>Classe Java pour BusinessMessageType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="BusinessMessageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}MessageType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ArchivalAgreement" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;element name="CodeListVersions" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CodeListVersionsType"/&gt;
 *         &lt;element name="DataObjectPackage" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectPackageType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessMessageType", propOrder = {
    "archivalAgreement",
    "codeListVersions",
    "dataObjectPackage"
})
@XmlSeeAlso({
    BusinessRequestMessageType.class,
    BusinessReplyMessageType.class,
    BusinessNotificationMessageType.class
})
public abstract class BusinessMessageType
    extends MessageType
{

    @XmlElement(name = "ArchivalAgreement")
    protected IdentifierType archivalAgreement;
    @XmlElement(name = "CodeListVersions", required = true)
    protected CodeListVersionsType codeListVersions;
    @XmlElement(name = "DataObjectPackage")
    protected DataObjectPackageType dataObjectPackage;

    /**
     * Obtient la valeur de la propriété archivalAgreement.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getArchivalAgreement() {
        return archivalAgreement;
    }

    /**
     * Définit la valeur de la propriété archivalAgreement.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setArchivalAgreement(IdentifierType value) {
        this.archivalAgreement = value;
    }

    /**
     * Obtient la valeur de la propriété codeListVersions.
     * 
     * @return
     *     possible object is
     *     {@link CodeListVersionsType }
     *     
     */
    public CodeListVersionsType getCodeListVersions() {
        return codeListVersions;
    }

    /**
     * Définit la valeur de la propriété codeListVersions.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeListVersionsType }
     *     
     */
    public void setCodeListVersions(CodeListVersionsType value) {
        this.codeListVersions = value;
    }

    /**
     * Obtient la valeur de la propriété dataObjectPackage.
     * 
     * @return
     *     possible object is
     *     {@link DataObjectPackageType }
     *     
     */
    public DataObjectPackageType getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Définit la valeur de la propriété dataObjectPackage.
     * 
     * @param value
     *     allowed object is
     *     {@link DataObjectPackageType }
     *     
     */
    public void setDataObjectPackage(DataObjectPackageType value) {
        this.dataObjectPackage = value;
    }

}
