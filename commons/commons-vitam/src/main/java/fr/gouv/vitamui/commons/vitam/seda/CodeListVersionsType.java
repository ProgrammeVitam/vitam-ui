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
 * <p>Classe Java pour CodeListVersionsType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="CodeListVersionsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}TransportCodeListsGroup"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}TechnicalCodeListsGroup"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementCodeListsGroup"/&gt;
 *         &lt;element name="AcquisitionInformationCodeListVersion" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CodeType" minOccurs="0"/&gt;
 *         &lt;element name="AuthorizationReasonCodeListVersion" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CodeType" minOccurs="0"/&gt;
 *         &lt;element name="RelationshipCodeListVersion" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CodeType" minOccurs="0"/&gt;
 *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "CodeListVersionsType", propOrder = {
    "replyCodeListVersion",
    "messageDigestAlgorithmCodeListVersion",
    "mimeTypeCodeListVersion",
    "encodingCodeListVersion",
    "fileFormatCodeListVersion",
    "compressionAlgorithmCodeListVersion",
    "dataObjectVersionCodeListVersion",
    "storageRuleCodeListVersion",
    "appraisalRuleCodeListVersion",
    "accessRuleCodeListVersion",
    "disseminationRuleCodeListVersion",
    "reuseRuleCodeListVersion",
    "classificationRuleCodeListVersion",
    "acquisitionInformationCodeListVersion",
    "authorizationReasonCodeListVersion",
    "relationshipCodeListVersion",
    "any"
})
public class CodeListVersionsType {

    @XmlElement(name = "ReplyCodeListVersion")
    protected CodeType replyCodeListVersion;
    @XmlElement(name = "MessageDigestAlgorithmCodeListVersion")
    protected CodeType messageDigestAlgorithmCodeListVersion;
    @XmlElement(name = "MimeTypeCodeListVersion")
    protected CodeType mimeTypeCodeListVersion;
    @XmlElement(name = "EncodingCodeListVersion")
    protected CodeType encodingCodeListVersion;
    @XmlElement(name = "FileFormatCodeListVersion")
    protected CodeType fileFormatCodeListVersion;
    @XmlElement(name = "CompressionAlgorithmCodeListVersion")
    protected CodeType compressionAlgorithmCodeListVersion;
    @XmlElement(name = "DataObjectVersionCodeListVersion")
    protected CodeType dataObjectVersionCodeListVersion;
    @XmlElement(name = "StorageRuleCodeListVersion")
    protected CodeType storageRuleCodeListVersion;
    @XmlElement(name = "AppraisalRuleCodeListVersion")
    protected CodeType appraisalRuleCodeListVersion;
    @XmlElement(name = "AccessRuleCodeListVersion")
    protected CodeType accessRuleCodeListVersion;
    @XmlElement(name = "DisseminationRuleCodeListVersion")
    protected CodeType disseminationRuleCodeListVersion;
    @XmlElement(name = "ReuseRuleCodeListVersion")
    protected CodeType reuseRuleCodeListVersion;
    @XmlElement(name = "ClassificationRuleCodeListVersion")
    protected CodeType classificationRuleCodeListVersion;
    @XmlElement(name = "AcquisitionInformationCodeListVersion")
    protected CodeType acquisitionInformationCodeListVersion;
    @XmlElement(name = "AuthorizationReasonCodeListVersion")
    protected CodeType authorizationReasonCodeListVersion;
    @XmlElement(name = "RelationshipCodeListVersion")
    protected CodeType relationshipCodeListVersion;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Obtient la valeur de la propriété replyCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getReplyCodeListVersion() {
        return replyCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété replyCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setReplyCodeListVersion(CodeType value) {
        this.replyCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété messageDigestAlgorithmCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getMessageDigestAlgorithmCodeListVersion() {
        return messageDigestAlgorithmCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété messageDigestAlgorithmCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setMessageDigestAlgorithmCodeListVersion(CodeType value) {
        this.messageDigestAlgorithmCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété mimeTypeCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getMimeTypeCodeListVersion() {
        return mimeTypeCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété mimeTypeCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setMimeTypeCodeListVersion(CodeType value) {
        this.mimeTypeCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété encodingCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getEncodingCodeListVersion() {
        return encodingCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété encodingCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setEncodingCodeListVersion(CodeType value) {
        this.encodingCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété fileFormatCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getFileFormatCodeListVersion() {
        return fileFormatCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété fileFormatCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setFileFormatCodeListVersion(CodeType value) {
        this.fileFormatCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété compressionAlgorithmCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getCompressionAlgorithmCodeListVersion() {
        return compressionAlgorithmCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété compressionAlgorithmCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setCompressionAlgorithmCodeListVersion(CodeType value) {
        this.compressionAlgorithmCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété dataObjectVersionCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getDataObjectVersionCodeListVersion() {
        return dataObjectVersionCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété dataObjectVersionCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setDataObjectVersionCodeListVersion(CodeType value) {
        this.dataObjectVersionCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété storageRuleCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getStorageRuleCodeListVersion() {
        return storageRuleCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété storageRuleCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setStorageRuleCodeListVersion(CodeType value) {
        this.storageRuleCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété appraisalRuleCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getAppraisalRuleCodeListVersion() {
        return appraisalRuleCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété appraisalRuleCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setAppraisalRuleCodeListVersion(CodeType value) {
        this.appraisalRuleCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété accessRuleCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getAccessRuleCodeListVersion() {
        return accessRuleCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété accessRuleCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setAccessRuleCodeListVersion(CodeType value) {
        this.accessRuleCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété disseminationRuleCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getDisseminationRuleCodeListVersion() {
        return disseminationRuleCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété disseminationRuleCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setDisseminationRuleCodeListVersion(CodeType value) {
        this.disseminationRuleCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété reuseRuleCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getReuseRuleCodeListVersion() {
        return reuseRuleCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété reuseRuleCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setReuseRuleCodeListVersion(CodeType value) {
        this.reuseRuleCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété classificationRuleCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getClassificationRuleCodeListVersion() {
        return classificationRuleCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété classificationRuleCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setClassificationRuleCodeListVersion(CodeType value) {
        this.classificationRuleCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété acquisitionInformationCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getAcquisitionInformationCodeListVersion() {
        return acquisitionInformationCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété acquisitionInformationCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setAcquisitionInformationCodeListVersion(CodeType value) {
        this.acquisitionInformationCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété authorizationReasonCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getAuthorizationReasonCodeListVersion() {
        return authorizationReasonCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété authorizationReasonCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setAuthorizationReasonCodeListVersion(CodeType value) {
        this.authorizationReasonCodeListVersion = value;
    }

    /**
     * Obtient la valeur de la propriété relationshipCodeListVersion.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getRelationshipCodeListVersion() {
        return relationshipCodeListVersion;
    }

    /**
     * Définit la valeur de la propriété relationshipCodeListVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setRelationshipCodeListVersion(CodeType value) {
        this.relationshipCodeListVersion = value;
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
