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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour MinimalDataObjectType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MinimalDataObjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DataObjectSystemId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="DataObjectGroupSystemId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="Relationship" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}RelationshipType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectVersionGroup" minOccurs="0"/&gt;
 *         &lt;element name="DataObjectVersion" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}VersionIdType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectIdType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MinimalDataObjectType", propOrder = {
    "dataObjectSystemId",
    "dataObjectGroupSystemId",
    "relationship",
    "dataObjectGroupReferenceId",
    "dataObjectGroupId",
    "dataObjectVersion"
})
@XmlSeeAlso({
    BinaryDataObjectType.class,
    PhysicalDataObjectType.class
})
public abstract class MinimalDataObjectType {

    @XmlElement(name = "DataObjectSystemId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String dataObjectSystemId;
    @XmlElement(name = "DataObjectGroupSystemId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String dataObjectGroupSystemId;
    @XmlElement(name = "Relationship")
    protected List<RelationshipType> relationship;
    @XmlElement(name = "DataObjectGroupReferenceId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object dataObjectGroupReferenceId;
    @XmlElement(name = "DataObjectGroupId")
    protected String dataObjectGroupId;
    @XmlElement(name = "DataObjectVersion")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String dataObjectVersion;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Obtient la valeur de la propriété dataObjectSystemId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataObjectSystemId() {
        return dataObjectSystemId;
    }

    /**
     * Définit la valeur de la propriété dataObjectSystemId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataObjectSystemId(String value) {
        this.dataObjectSystemId = value;
    }

    /**
     * Obtient la valeur de la propriété dataObjectGroupSystemId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataObjectGroupSystemId() {
        return dataObjectGroupSystemId;
    }

    /**
     * Définit la valeur de la propriété dataObjectGroupSystemId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataObjectGroupSystemId(String value) {
        this.dataObjectGroupSystemId = value;
    }

    /**
     * Gets the value of the relationship property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relationship property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelationship().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelationshipType }
     * 
     * 
     */
    public List<RelationshipType> getRelationship() {
        if (relationship == null) {
            relationship = new ArrayList<RelationshipType>();
        }
        return this.relationship;
    }

    /**
     * Obtient la valeur de la propriété dataObjectGroupReferenceId.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDataObjectGroupReferenceId() {
        return dataObjectGroupReferenceId;
    }

    /**
     * Définit la valeur de la propriété dataObjectGroupReferenceId.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDataObjectGroupReferenceId(Object value) {
        this.dataObjectGroupReferenceId = value;
    }

    /**
     * Obtient la valeur de la propriété dataObjectGroupId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataObjectGroupId() {
        return dataObjectGroupId;
    }

    /**
     * Définit la valeur de la propriété dataObjectGroupId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataObjectGroupId(String value) {
        this.dataObjectGroupId = value;
    }

    /**
     * Obtient la valeur de la propriété dataObjectVersion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataObjectVersion() {
        return dataObjectVersion;
    }

    /**
     * Définit la valeur de la propriété dataObjectVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataObjectVersion(String value) {
        this.dataObjectVersion = value;
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
