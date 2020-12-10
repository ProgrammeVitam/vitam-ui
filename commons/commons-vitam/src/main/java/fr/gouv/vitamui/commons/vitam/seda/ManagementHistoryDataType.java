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
import javax.xml.bind.annotation.XmlType;


/**
 * Référence à un objet-données ou à un groupe d'objets-données existant.
 *             
 * 
 * <p>Classe Java pour ManagementHistoryDataType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ManagementHistoryDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Management" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManagementHistoryDataType", propOrder = {
    "version",
    "management"
})
public class ManagementHistoryDataType {

    @XmlElement(name = "Version", required = true)
    protected String version;
    @XmlElement(name = "Management")
    protected ManagementType management;

    /**
     * Obtient la valeur de la propriété version.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Définit la valeur de la propriété version.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Obtient la valeur de la propriété management.
     * 
     * @return
     *     possible object is
     *     {@link ManagementType }
     *     
     */
    public ManagementType getManagement() {
        return management;
    }

    /**
     * Définit la valeur de la propriété management.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagementType }
     *     
     */
    public void setManagement(ManagementType value) {
        this.management = value;
    }

}
