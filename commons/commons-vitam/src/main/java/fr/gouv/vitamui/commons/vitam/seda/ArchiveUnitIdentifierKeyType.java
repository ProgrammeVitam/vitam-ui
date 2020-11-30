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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour ArchiveUnitIdentifierKeyType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArchiveUnitIdentifierKeyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MetadataName" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *         &lt;element name="MetadataValue" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchiveUnitIdentifierKeyType", propOrder = {
    "metadataName",
    "metadataValue"
})
public class ArchiveUnitIdentifierKeyType {

    @XmlElement(name = "MetadataName", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String metadataName;
    @XmlElement(name = "MetadataValue", required = true)
    protected String metadataValue;

    /**
     * Obtient la valeur de la propriété metadataName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetadataName() {
        return metadataName;
    }

    /**
     * Définit la valeur de la propriété metadataName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetadataName(String value) {
        this.metadataName = value;
    }

    /**
     * Obtient la valeur de la propriété metadataValue.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetadataValue() {
        return metadataValue;
    }

    /**
     * Définit la valeur de la propriété metadataValue.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetadataValue(String value) {
        this.metadataValue = value;
    }

}
