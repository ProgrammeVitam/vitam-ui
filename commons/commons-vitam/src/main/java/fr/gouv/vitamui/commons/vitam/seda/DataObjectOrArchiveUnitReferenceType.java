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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour DataObjectOrArchiveUnitReferenceType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DataObjectOrArchiveUnitReferenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="ArchiveUnitRefId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ArchiveUnitRefIdType"/&gt;
 *         &lt;element name="DataObjectReference" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefType"/&gt;
 *         &lt;element name="RepositoryArchiveUnitPID" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *         &lt;element name="RepositoryObjectPID" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *         &lt;element name="ExternalReference" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataObjectOrArchiveUnitReferenceType", propOrder = {
    "archiveUnitRefId",
    "dataObjectReference",
    "repositoryArchiveUnitPID",
    "repositoryObjectPID",
    "externalReference"
})
public class DataObjectOrArchiveUnitReferenceType {

    @XmlElement(name = "ArchiveUnitRefId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object archiveUnitRefId;
    @XmlElement(name = "DataObjectReference")
    protected DataObjectRefType dataObjectReference;
    @XmlElement(name = "RepositoryArchiveUnitPID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String repositoryArchiveUnitPID;
    @XmlElement(name = "RepositoryObjectPID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String repositoryObjectPID;
    @XmlElement(name = "ExternalReference")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String externalReference;

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
     * Obtient la valeur de la propriété repositoryArchiveUnitPID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryArchiveUnitPID() {
        return repositoryArchiveUnitPID;
    }

    /**
     * Définit la valeur de la propriété repositoryArchiveUnitPID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryArchiveUnitPID(String value) {
        this.repositoryArchiveUnitPID = value;
    }

    /**
     * Obtient la valeur de la propriété repositoryObjectPID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryObjectPID() {
        return repositoryObjectPID;
    }

    /**
     * Définit la valeur de la propriété repositoryObjectPID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryObjectPID(String value) {
        this.repositoryObjectPID = value;
    }

    /**
     * Obtient la valeur de la propriété externalReference.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalReference() {
        return externalReference;
    }

    /**
     * Définit la valeur de la propriété externalReference.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalReference(String value) {
        this.externalReference = value;
    }

}
