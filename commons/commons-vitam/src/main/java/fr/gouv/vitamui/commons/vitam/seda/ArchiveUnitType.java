//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Element;


/**
 * Unité de base des métadonnées de description contenant la gestion de l'arborescence.
 * 
 * <p>Classe Java pour ArchiveUnitType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArchiveUnitType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="ArchiveUnitRefId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ArchiveUnitRefIdType"/&gt;
 *         &lt;sequence&gt;
 *           &lt;element name="ArchiveUnitProfile" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *           &lt;element name="Management" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementType" minOccurs="0"/&gt;
 *           &lt;element name="Content" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DescriptiveMetadataContentType"/&gt;
 *           &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *             &lt;element name="ArchiveUnit" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ArchiveUnitType"/&gt;
 *             &lt;element name="DataObjectReference" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefType"/&gt;
 *             &lt;element name="DataObjectGroup" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ObjectGroupRefType"/&gt;
 *             &lt;any processContents='lax' minOccurs="0"/&gt;
 *           &lt;/choice&gt;
 *         &lt;/sequence&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="id" use="required" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ArchiveUnitIdType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchiveUnitType", propOrder = {
    "archiveUnitRefId",
    "archiveUnitProfile",
    "management",
    "content",
    "archiveUnitOrDataObjectReferenceOrDataObjectGroup"
})
public class ArchiveUnitType {

    @XmlElement(name = "ArchiveUnitRefId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object archiveUnitRefId;
    @XmlElement(name = "ArchiveUnitProfile")
    protected IdentifierType archiveUnitProfile;
    @XmlElement(name = "Management")
    protected ManagementType management;
    @XmlElement(name = "Content")
    protected DescriptiveMetadataContentType content;
    @XmlElementRefs({
        @XmlElementRef(name = "ArchiveUnit", namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "DataObjectReference", namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "DataObjectGroup", namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", type = JAXBElement.class, required = false)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> archiveUnitOrDataObjectReferenceOrDataObjectGroup;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Obtient la valeur de la propriété archiveUnitRefId.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getArchiveUnitRefId() {
        return archiveUnitRefId;
    }

    /**
     * Définit la valeur de la propriété archiveUnitRefId.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setArchiveUnitRefId(Object value) {
        this.archiveUnitRefId = value;
    }

    /**
     * Obtient la valeur de la propriété archiveUnitProfile.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getArchiveUnitProfile() {
        return archiveUnitProfile;
    }

    /**
     * Définit la valeur de la propriété archiveUnitProfile.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setArchiveUnitProfile(IdentifierType value) {
        this.archiveUnitProfile = value;
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

    /**
     * Obtient la valeur de la propriété content.
     * 
     * @return
     *     possible object is
     *     {@link DescriptiveMetadataContentType }
     *     
     */
    public DescriptiveMetadataContentType getContent() {
        return content;
    }

    /**
     * Définit la valeur de la propriété content.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptiveMetadataContentType }
     *     
     */
    public void setContent(DescriptiveMetadataContentType value) {
        this.content = value;
    }

    /**
     * Gets the value of the archiveUnitOrDataObjectReferenceOrDataObjectGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the archiveUnitOrDataObjectReferenceOrDataObjectGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArchiveUnitOrDataObjectReferenceOrDataObjectGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ArchiveUnitType }{@code >}
     * {@link JAXBElement }{@code <}{@link DataObjectRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObjectGroupRefType }{@code >}
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getArchiveUnitOrDataObjectReferenceOrDataObjectGroup() {
        if (archiveUnitOrDataObjectReferenceOrDataObjectGroup == null) {
            archiveUnitOrDataObjectReferenceOrDataObjectGroup = new ArrayList<Object>();
        }
        return this.archiveUnitOrDataObjectReferenceOrDataObjectGroup;
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
