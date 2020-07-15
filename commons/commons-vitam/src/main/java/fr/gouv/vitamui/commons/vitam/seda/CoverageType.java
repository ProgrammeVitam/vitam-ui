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
 * <p>Classe Java pour CoverageType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="CoverageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Spatial" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Temporal" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Juridictional" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoverageType", propOrder = {
    "spatial",
    "temporal",
    "juridictional"
})
public class CoverageType {

    @XmlElement(name = "Spatial")
    protected List<TextType> spatial;
    @XmlElement(name = "Temporal")
    protected List<TextType> temporal;
    @XmlElement(name = "Juridictional")
    protected List<TextType> juridictional;

    /**
     * Gets the value of the spatial property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spatial property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpatial().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getSpatial() {
        if (spatial == null) {
            spatial = new ArrayList<TextType>();
        }
        return this.spatial;
    }

    /**
     * Gets the value of the temporal property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the temporal property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTemporal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getTemporal() {
        if (temporal == null) {
            temporal = new ArrayList<TextType>();
        }
        return this.temporal;
    }

    /**
     * Gets the value of the juridictional property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the juridictional property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJuridictional().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getJuridictional() {
        if (juridictional == null) {
            juridictional = new ArrayList<TextType>();
        }
        return this.juridictional;
    }

}
