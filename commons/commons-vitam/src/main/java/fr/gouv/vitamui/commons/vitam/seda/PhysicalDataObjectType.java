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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Classe Java pour PhysicalDataObjectType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PhysicalDataObjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}MinimalDataObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PhysicalId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}PhysicalTechnicalDescriptionGroup"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhysicalDataObjectType", propOrder = {
    "physicalId",
    "physicalDimensions",
    "any"
})
public class PhysicalDataObjectType
    extends MinimalDataObjectType
{

    @XmlElement(name = "PhysicalId")
    protected IdentifierType physicalId;
    @XmlElement(name = "PhysicalDimensions")
    protected DimensionsType physicalDimensions;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Obtient la valeur de la propriété physicalId.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getPhysicalId() {
        return physicalId;
    }

    /**
     * Définit la valeur de la propriété physicalId.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setPhysicalId(IdentifierType value) {
        this.physicalId = value;
    }

    /**
     * Obtient la valeur de la propriété physicalDimensions.
     * 
     * @return
     *     possible object is
     *     {@link DimensionsType }
     *     
     */
    public DimensionsType getPhysicalDimensions() {
        return physicalDimensions;
    }

    /**
     * Définit la valeur de la propriété physicalDimensions.
     * 
     * @param value
     *     allowed object is
     *     {@link DimensionsType }
     *     
     */
    public void setPhysicalDimensions(DimensionsType value) {
        this.physicalDimensions = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}
