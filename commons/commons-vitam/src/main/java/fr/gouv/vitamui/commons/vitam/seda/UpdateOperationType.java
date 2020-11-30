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
 * <p>Classe Java pour UpdateOperationType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="UpdateOperationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="SystemId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *           &lt;element name="ArchiveUnitIdentifierKey" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ArchiveUnitIdentifierKeyType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="ToDelete" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ToDeleteType" minOccurs="0"/&gt;
 *         &lt;element name="FullUpdate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateOperationType", propOrder = {
    "systemId",
    "archiveUnitIdentifierKey",
    "toDelete",
    "fullUpdate"
})
public class UpdateOperationType {

    @XmlElement(name = "SystemId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String systemId;
    @XmlElement(name = "ArchiveUnitIdentifierKey")
    protected ArchiveUnitIdentifierKeyType archiveUnitIdentifierKey;
    @XmlElement(name = "ToDelete")
    protected ToDeleteType toDelete;
    @XmlElement(name = "FullUpdate", defaultValue = "false")
    protected Boolean fullUpdate;

    /**
     * Obtient la valeur de la propriété systemId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Définit la valeur de la propriété systemId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemId(String value) {
        this.systemId = value;
    }

    /**
     * Obtient la valeur de la propriété archiveUnitIdentifierKey.
     * 
     * @return
     *     possible object is
     *     {@link ArchiveUnitIdentifierKeyType }
     *     
     */
    public ArchiveUnitIdentifierKeyType getArchiveUnitIdentifierKey() {
        return archiveUnitIdentifierKey;
    }

    /**
     * Définit la valeur de la propriété archiveUnitIdentifierKey.
     * 
     * @param value
     *     allowed object is
     *     {@link ArchiveUnitIdentifierKeyType }
     *     
     */
    public void setArchiveUnitIdentifierKey(ArchiveUnitIdentifierKeyType value) {
        this.archiveUnitIdentifierKey = value;
    }

    /**
     * Obtient la valeur de la propriété toDelete.
     * 
     * @return
     *     possible object is
     *     {@link ToDeleteType }
     *     
     */
    public ToDeleteType getToDelete() {
        return toDelete;
    }

    /**
     * Définit la valeur de la propriété toDelete.
     * 
     * @param value
     *     allowed object is
     *     {@link ToDeleteType }
     *     
     */
    public void setToDelete(ToDeleteType value) {
        this.toDelete = value;
    }

    /**
     * Obtient la valeur de la propriété fullUpdate.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFullUpdate() {
        return fullUpdate;
    }

    /**
     * Définit la valeur de la propriété fullUpdate.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFullUpdate(Boolean value) {
        this.fullUpdate = value;
    }

}
