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
 * Référence à un objet-données ou à un groupe d'objets-données existant.
 * 
 * <p>Classe Java pour ObjectGroupRefType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ObjectGroupRefType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DataObjectReference" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefType" minOccurs="0"/&gt;
 *         &lt;element name="DataObjectGroupExistingReferenceId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectGroupRefType", propOrder = {
    "dataObjectReference",
    "dataObjectGroupExistingReferenceId"
})
public class ObjectGroupRefType {

    @XmlElement(name = "DataObjectReference")
    protected DataObjectRefType dataObjectReference;
    @XmlElement(name = "DataObjectGroupExistingReferenceId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String dataObjectGroupExistingReferenceId;

    /**
     * Obtient la valeur de la propriété dataObjectReference.
     * 
     * @return
     *     possible object is
     *     {@link DataObjectRefType }
     *     
     */
    public DataObjectRefType getDataObjectReference() {
        return dataObjectReference;
    }

    /**
     * Définit la valeur de la propriété dataObjectReference.
     * 
     * @param value
     *     allowed object is
     *     {@link DataObjectRefType }
     *     
     */
    public void setDataObjectReference(DataObjectRefType value) {
        this.dataObjectReference = value;
    }

    /**
     * Obtient la valeur de la propriété dataObjectGroupExistingReferenceId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataObjectGroupExistingReferenceId() {
        return dataObjectGroupExistingReferenceId;
    }

    /**
     * Définit la valeur de la propriété dataObjectGroupExistingReferenceId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataObjectGroupExistingReferenceId(String value) {
        this.dataObjectGroupExistingReferenceId = value;
    }

}
