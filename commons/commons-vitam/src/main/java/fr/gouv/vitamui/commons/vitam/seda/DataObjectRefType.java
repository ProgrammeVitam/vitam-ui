//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Référence à un objet-données ou à un groupe d'objets-données.
 * 
 * <p>Classe Java pour DataObjectRefType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DataObjectRefType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="DataObjectReferenceId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefIdType"/&gt;
 *           &lt;element name="DataObjectGroupReferenceId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}GroupRefIdType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataObjectRefType", propOrder = {
    "dataObjectReferenceId",
    "dataObjectGroupReferenceId"
})
public class DataObjectRefType {

    @XmlElement(name = "DataObjectReferenceId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object dataObjectReferenceId;
    @XmlElement(name = "DataObjectGroupReferenceId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object dataObjectGroupReferenceId;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Obtient la valeur de la propriété dataObjectReferenceId.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDataObjectReferenceId() {
        return dataObjectReferenceId;
    }

    /**
     * Définit la valeur de la propriété dataObjectReferenceId.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDataObjectReferenceId(Object value) {
        this.dataObjectReferenceId = value;
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
