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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour DataObjectGroupType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DataObjectGroupType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="BinaryDataObject" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}BinaryDataObjectType"/&gt;
 *           &lt;element name="PhysicalDataObject" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}PhysicalDataObjectType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="LogBook" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}LogBookOgType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}GroupIdType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataObjectGroupType", propOrder = {
    "binaryDataObjectOrPhysicalDataObject",
    "logBook"
})
public class DataObjectGroupType {

    @XmlElements({
        @XmlElement(name = "BinaryDataObject", type = BinaryDataObjectType.class),
        @XmlElement(name = "PhysicalDataObject", type = PhysicalDataObjectType.class)
    })
    protected List<MinimalDataObjectType> binaryDataObjectOrPhysicalDataObject;
    @XmlElement(name = "LogBook")
    protected LogBookOgType logBook;
    @XmlAttribute(name = "id", required = true)
    protected String id;

    /**
     * Gets the value of the binaryDataObjectOrPhysicalDataObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the binaryDataObjectOrPhysicalDataObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBinaryDataObjectOrPhysicalDataObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BinaryDataObjectType }
     * {@link PhysicalDataObjectType }
     * 
     * 
     */
    public List<MinimalDataObjectType> getBinaryDataObjectOrPhysicalDataObject() {
        if (binaryDataObjectOrPhysicalDataObject == null) {
            binaryDataObjectOrPhysicalDataObject = new ArrayList<MinimalDataObjectType>();
        }
        return this.binaryDataObjectOrPhysicalDataObject;
    }

    /**
     * Obtient la valeur de la propriété logBook.
     * 
     * @return
     *     possible object is
     *     {@link LogBookOgType }
     *     
     */
    public LogBookOgType getLogBook() {
        return logBook;
    }

    /**
     * Définit la valeur de la propriété logBook.
     * 
     * @param value
     *     allowed object is
     *     {@link LogBookOgType }
     *     
     */
    public void setLogBook(LogBookOgType value) {
        this.logBook = value;
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
