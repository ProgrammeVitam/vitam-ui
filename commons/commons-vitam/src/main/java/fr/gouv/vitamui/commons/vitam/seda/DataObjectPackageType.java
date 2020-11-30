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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour DataObjectPackageType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DataObjectPackageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="DataObjectGroup" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectGroupType"/&gt;
 *           &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *             &lt;element name="BinaryDataObject" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}BinaryDataObjectType"/&gt;
 *             &lt;element name="PhysicalDataObject" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}PhysicalDataObjectType"/&gt;
 *           &lt;/choice&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="DescriptiveMetadata" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DescriptiveMetadataType"/&gt;
 *         &lt;element name="ManagementMetadata" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementMetadataType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataObjectPackageType", propOrder = {
    "dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject",
    "descriptiveMetadata",
    "managementMetadata"
})
public class DataObjectPackageType {

    @XmlElements({
        @XmlElement(name = "DataObjectGroup", type = DataObjectGroupType.class),
        @XmlElement(name = "BinaryDataObject", type = BinaryDataObjectType.class),
        @XmlElement(name = "PhysicalDataObject", type = PhysicalDataObjectType.class)
    })
    protected List<Object> dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject;
    @XmlElement(name = "DescriptiveMetadata", required = true)
    protected DescriptiveMetadataType descriptiveMetadata;
    @XmlElement(name = "ManagementMetadata", required = true)
    protected ManagementMetadataType managementMetadata;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataObjectGroupOrBinaryDataObjectOrPhysicalDataObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObjectGroupType }
     * {@link BinaryDataObjectType }
     * {@link PhysicalDataObjectType }
     * 
     * 
     */
    public List<Object> getDataObjectGroupOrBinaryDataObjectOrPhysicalDataObject() {
        if (dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject == null) {
            dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject = new ArrayList<Object>();
        }
        return this.dataObjectGroupOrBinaryDataObjectOrPhysicalDataObject;
    }

    /**
     * Obtient la valeur de la propriété descriptiveMetadata.
     * 
     * @return
     *     possible object is
     *     {@link DescriptiveMetadataType }
     *     
     */
    public DescriptiveMetadataType getDescriptiveMetadata() {
        return descriptiveMetadata;
    }

    /**
     * Définit la valeur de la propriété descriptiveMetadata.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptiveMetadataType }
     *     
     */
    public void setDescriptiveMetadata(DescriptiveMetadataType value) {
        this.descriptiveMetadata = value;
    }

    /**
     * Obtient la valeur de la propriété managementMetadata.
     * 
     * @return
     *     possible object is
     *     {@link ManagementMetadataType }
     *     
     */
    public ManagementMetadataType getManagementMetadata() {
        return managementMetadata;
    }

    /**
     * Définit la valeur de la propriété managementMetadata.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagementMetadataType }
     *     
     */
    public void setManagementMetadata(ManagementMetadataType value) {
        this.managementMetadata = value;
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
