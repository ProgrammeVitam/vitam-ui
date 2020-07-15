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
 * <p>Classe Java pour RelatedObjectReferenceType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="RelatedObjectReferenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="IsVersionOf" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectOrArchiveUnitReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Replaces" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectOrArchiveUnitReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Requires" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectOrArchiveUnitReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="IsPartOf" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectOrArchiveUnitReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="References" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectOrArchiveUnitReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelatedObjectReferenceType", propOrder = {
    "isVersionOf",
    "replaces",
    "requires",
    "isPartOf",
    "references"
})
public class RelatedObjectReferenceType {

    @XmlElement(name = "IsVersionOf")
    protected List<DataObjectOrArchiveUnitReferenceType> isVersionOf;
    @XmlElement(name = "Replaces")
    protected List<DataObjectOrArchiveUnitReferenceType> replaces;
    @XmlElement(name = "Requires")
    protected List<DataObjectOrArchiveUnitReferenceType> requires;
    @XmlElement(name = "IsPartOf")
    protected List<DataObjectOrArchiveUnitReferenceType> isPartOf;
    @XmlElement(name = "References")
    protected List<DataObjectOrArchiveUnitReferenceType> references;

    /**
     * Gets the value of the isVersionOf property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the isVersionOf property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIsVersionOf().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObjectOrArchiveUnitReferenceType }
     * 
     * 
     */
    public List<DataObjectOrArchiveUnitReferenceType> getIsVersionOf() {
        if (isVersionOf == null) {
            isVersionOf = new ArrayList<DataObjectOrArchiveUnitReferenceType>();
        }
        return this.isVersionOf;
    }

    /**
     * Gets the value of the replaces property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replaces property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReplaces().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObjectOrArchiveUnitReferenceType }
     * 
     * 
     */
    public List<DataObjectOrArchiveUnitReferenceType> getReplaces() {
        if (replaces == null) {
            replaces = new ArrayList<DataObjectOrArchiveUnitReferenceType>();
        }
        return this.replaces;
    }

    /**
     * Gets the value of the requires property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requires property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequires().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObjectOrArchiveUnitReferenceType }
     * 
     * 
     */
    public List<DataObjectOrArchiveUnitReferenceType> getRequires() {
        if (requires == null) {
            requires = new ArrayList<DataObjectOrArchiveUnitReferenceType>();
        }
        return this.requires;
    }

    /**
     * Gets the value of the isPartOf property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the isPartOf property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIsPartOf().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObjectOrArchiveUnitReferenceType }
     * 
     * 
     */
    public List<DataObjectOrArchiveUnitReferenceType> getIsPartOf() {
        if (isPartOf == null) {
            isPartOf = new ArrayList<DataObjectOrArchiveUnitReferenceType>();
        }
        return this.isPartOf;
    }

    /**
     * Gets the value of the references property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the references property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObjectOrArchiveUnitReferenceType }
     * 
     * 
     */
    public List<DataObjectOrArchiveUnitReferenceType> getReferences() {
        if (references == null) {
            references = new ArrayList<DataObjectOrArchiveUnitReferenceType>();
        }
        return this.references;
    }

}
