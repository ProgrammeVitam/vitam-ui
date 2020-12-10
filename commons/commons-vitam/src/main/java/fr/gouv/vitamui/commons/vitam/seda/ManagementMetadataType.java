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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Element;


/**
 * <p>Classe Java pour ManagementMetadataType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ManagementMetadataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ArchivalProfile" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;element name="ServiceLevel" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;element name="AcquisitionInformation" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="LegalStatus" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}LegalStatusType" minOccurs="0"/&gt;
 *         &lt;element name="OriginatingAgencyIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;element name="SubmissionAgencyIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementGroup" minOccurs="0"/&gt;
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
@XmlType(name = "ManagementMetadataType", propOrder = {
    "archivalProfile",
    "serviceLevel",
    "acquisitionInformation",
    "legalStatus",
    "originatingAgencyIdentifier",
    "submissionAgencyIdentifier",
    "storageRule",
    "appraisalRule",
    "accessRule",
    "disseminationRule",
    "reuseRule",
    "classificationRule",
    "logBook",
    "needAuthorization",
    "updateOperation",
    "any"
})
public class ManagementMetadataType {

    @XmlElement(name = "ArchivalProfile")
    protected IdentifierType archivalProfile;
    @XmlElement(name = "ServiceLevel")
    protected IdentifierType serviceLevel;
    @XmlElement(name = "AcquisitionInformation")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String acquisitionInformation;
    @XmlElement(name = "LegalStatus")
    @XmlSchemaType(name = "token")
    protected LegalStatusType legalStatus;
    @XmlElement(name = "OriginatingAgencyIdentifier")
    protected IdentifierType originatingAgencyIdentifier;
    @XmlElement(name = "SubmissionAgencyIdentifier")
    protected IdentifierType submissionAgencyIdentifier;
    @XmlElement(name = "StorageRule")
    protected StorageRuleType storageRule;
    @XmlElement(name = "AppraisalRule")
    protected AppraisalRuleType appraisalRule;
    @XmlElement(name = "AccessRule")
    protected AccessRuleType accessRule;
    @XmlElement(name = "DisseminationRule")
    protected DisseminationRuleType disseminationRule;
    @XmlElement(name = "ReuseRule")
    protected ReuseRuleType reuseRule;
    @XmlElement(name = "ClassificationRule")
    protected ClassificationRuleType classificationRule;
    @XmlElement(name = "LogBook")
    protected LogBookType logBook;
    @XmlElement(name = "NeedAuthorization")
    protected Boolean needAuthorization;
    @XmlElement(name = "UpdateOperation")
    protected UpdateOperationType updateOperation;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Obtient la valeur de la propriété archivalProfile.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getArchivalProfile() {
        return archivalProfile;
    }

    /**
     * Définit la valeur de la propriété archivalProfile.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setArchivalProfile(IdentifierType value) {
        this.archivalProfile = value;
    }

    /**
     * Obtient la valeur de la propriété serviceLevel.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getServiceLevel() {
        return serviceLevel;
    }

    /**
     * Définit la valeur de la propriété serviceLevel.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setServiceLevel(IdentifierType value) {
        this.serviceLevel = value;
    }

    /**
     * Obtient la valeur de la propriété acquisitionInformation.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcquisitionInformation() {
        return acquisitionInformation;
    }

    /**
     * Définit la valeur de la propriété acquisitionInformation.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcquisitionInformation(String value) {
        this.acquisitionInformation = value;
    }

    /**
     * Obtient la valeur de la propriété legalStatus.
     * 
     * @return
     *     possible object is
     *     {@link LegalStatusType }
     *     
     */
    public LegalStatusType getLegalStatus() {
        return legalStatus;
    }

    /**
     * Définit la valeur de la propriété legalStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link LegalStatusType }
     *     
     */
    public void setLegalStatus(LegalStatusType value) {
        this.legalStatus = value;
    }

    /**
     * Obtient la valeur de la propriété originatingAgencyIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getOriginatingAgencyIdentifier() {
        return originatingAgencyIdentifier;
    }

    /**
     * Définit la valeur de la propriété originatingAgencyIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setOriginatingAgencyIdentifier(IdentifierType value) {
        this.originatingAgencyIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété submissionAgencyIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getSubmissionAgencyIdentifier() {
        return submissionAgencyIdentifier;
    }

    /**
     * Définit la valeur de la propriété submissionAgencyIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setSubmissionAgencyIdentifier(IdentifierType value) {
        this.submissionAgencyIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété storageRule.
     * 
     * @return
     *     possible object is
     *     {@link StorageRuleType }
     *     
     */
    public StorageRuleType getStorageRule() {
        return storageRule;
    }

    /**
     * Définit la valeur de la propriété storageRule.
     * 
     * @param value
     *     allowed object is
     *     {@link StorageRuleType }
     *     
     */
    public void setStorageRule(StorageRuleType value) {
        this.storageRule = value;
    }

    /**
     * Obtient la valeur de la propriété appraisalRule.
     * 
     * @return
     *     possible object is
     *     {@link AppraisalRuleType }
     *     
     */
    public AppraisalRuleType getAppraisalRule() {
        return appraisalRule;
    }

    /**
     * Définit la valeur de la propriété appraisalRule.
     * 
     * @param value
     *     allowed object is
     *     {@link AppraisalRuleType }
     *     
     */
    public void setAppraisalRule(AppraisalRuleType value) {
        this.appraisalRule = value;
    }

    /**
     * Obtient la valeur de la propriété accessRule.
     * 
     * @return
     *     possible object is
     *     {@link AccessRuleType }
     *     
     */
    public AccessRuleType getAccessRule() {
        return accessRule;
    }

    /**
     * Définit la valeur de la propriété accessRule.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessRuleType }
     *     
     */
    public void setAccessRule(AccessRuleType value) {
        this.accessRule = value;
    }

    /**
     * Obtient la valeur de la propriété disseminationRule.
     * 
     * @return
     *     possible object is
     *     {@link DisseminationRuleType }
     *     
     */
    public DisseminationRuleType getDisseminationRule() {
        return disseminationRule;
    }

    /**
     * Définit la valeur de la propriété disseminationRule.
     * 
     * @param value
     *     allowed object is
     *     {@link DisseminationRuleType }
     *     
     */
    public void setDisseminationRule(DisseminationRuleType value) {
        this.disseminationRule = value;
    }

    /**
     * Obtient la valeur de la propriété reuseRule.
     * 
     * @return
     *     possible object is
     *     {@link ReuseRuleType }
     *     
     */
    public ReuseRuleType getReuseRule() {
        return reuseRule;
    }

    /**
     * Définit la valeur de la propriété reuseRule.
     * 
     * @param value
     *     allowed object is
     *     {@link ReuseRuleType }
     *     
     */
    public void setReuseRule(ReuseRuleType value) {
        this.reuseRule = value;
    }

    /**
     * Obtient la valeur de la propriété classificationRule.
     * 
     * @return
     *     possible object is
     *     {@link ClassificationRuleType }
     *     
     */
    public ClassificationRuleType getClassificationRule() {
        return classificationRule;
    }

    /**
     * Définit la valeur de la propriété classificationRule.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassificationRuleType }
     *     
     */
    public void setClassificationRule(ClassificationRuleType value) {
        this.classificationRule = value;
    }

    /**
     * Obtient la valeur de la propriété logBook.
     * 
     * @return
     *     possible object is
     *     {@link LogBookType }
     *     
     */
    public LogBookType getLogBook() {
        return logBook;
    }

    /**
     * Définit la valeur de la propriété logBook.
     * 
     * @param value
     *     allowed object is
     *     {@link LogBookType }
     *     
     */
    public void setLogBook(LogBookType value) {
        this.logBook = value;
    }

    /**
     * Obtient la valeur de la propriété needAuthorization.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNeedAuthorization() {
        return needAuthorization;
    }

    /**
     * Définit la valeur de la propriété needAuthorization.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNeedAuthorization(Boolean value) {
        this.needAuthorization = value;
    }

    /**
     * Obtient la valeur de la propriété updateOperation.
     * 
     * @return
     *     possible object is
     *     {@link UpdateOperationType }
     *     
     */
    public UpdateOperationType getUpdateOperation() {
        return updateOperation;
    }

    /**
     * Définit la valeur de la propriété updateOperation.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateOperationType }
     *     
     */
    public void setUpdateOperation(UpdateOperationType value) {
        this.updateOperation = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
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
