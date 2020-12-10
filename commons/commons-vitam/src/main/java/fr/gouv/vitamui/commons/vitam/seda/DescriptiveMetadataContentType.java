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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Element;


/**
 * Permet de définir les métadonnées de description. Peut être étendu.
 * 
 * <p>Classe Java pour DescriptiveMetadataContentType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DescriptiveMetadataContentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}ObjectGroup"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementHistoryGroup" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptiveMetadataContentType", propOrder = {
    "descriptionLevel",
    "title",
    "filePlanPosition",
    "systemId",
    "originatingSystemId",
    "archivalAgencyArchiveUnitIdentifier",
    "originatingAgencyArchiveUnitIdentifier",
    "transferringAgencyArchiveUnitIdentifier",
    "description",
    "custodialHistory",
    "type",
    "documentType",
    "language",
    "descriptionLanguage",
    "status",
    "version",
    "tag",
    "keyword",
    "coverage",
    "originatingAgency",
    "submissionAgency",
    "agentAbstract",
    "authorizedAgent",
    "writer",
    "addressee",
    "recipient",
    "transmitter",
    "sender",
    "source",
    "relatedObjectReference",
    "createdDate",
    "transactedDate",
    "acquiredDate",
    "sentDate",
    "receivedDate",
    "registeredDate",
    "startDate",
    "endDate",
    "event",
    "signature",
    "gps",
    "any",
    "history"
})
public class DescriptiveMetadataContentType {

    @XmlElement(name = "DescriptionLevel")
    @XmlSchemaType(name = "token")
    protected LevelType descriptionLevel;
    @XmlElement(name = "Title")
    protected List<TextType> title;
    @XmlElement(name = "FilePlanPosition")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> filePlanPosition;
    @XmlElement(name = "SystemId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> systemId;
    @XmlElement(name = "OriginatingSystemId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> originatingSystemId;
    @XmlElement(name = "ArchivalAgencyArchiveUnitIdentifier")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> archivalAgencyArchiveUnitIdentifier;
    @XmlElement(name = "OriginatingAgencyArchiveUnitIdentifier")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> originatingAgencyArchiveUnitIdentifier;
    @XmlElement(name = "TransferringAgencyArchiveUnitIdentifier")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> transferringAgencyArchiveUnitIdentifier;
    @XmlElement(name = "Description")
    protected List<TextType> description;
    @XmlElement(name = "CustodialHistory")
    protected CustodialHistoryType custodialHistory;
    @XmlElement(name = "Type")
    protected TextType type;
    @XmlElement(name = "DocumentType")
    protected TextType documentType;
    @XmlElement(name = "Language")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected List<String> language;
    @XmlElement(name = "DescriptionLanguage")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected String descriptionLanguage;
    @XmlElement(name = "Status")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String status;
    @XmlElement(name = "Version")
    protected String version;
    @XmlElement(name = "Tag")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> tag;
    @XmlElement(name = "Keyword")
    protected List<KeywordsType> keyword;
    @XmlElement(name = "Coverage")
    protected CoverageType coverage;
    @XmlElement(name = "OriginatingAgency")
    protected OrganizationType originatingAgency;
    @XmlElement(name = "SubmissionAgency")
    protected OrganizationType submissionAgency;
    @XmlElement(name = "AgentAbstract")
    protected List<AgentType> agentAbstract;
    @XmlElement(name = "AuthorizedAgent")
    protected List<AgentType> authorizedAgent;
    @XmlElement(name = "Writer")
    protected List<AgentType> writer;
    @XmlElement(name = "Addressee")
    protected List<AgentType> addressee;
    @XmlElement(name = "Recipient")
    protected List<AgentType> recipient;
    @XmlElement(name = "Transmitter")
    protected List<AgentType> transmitter;
    @XmlElement(name = "Sender")
    protected List<AgentType> sender;
    @XmlElement(name = "Source")
    protected String source;
    @XmlElement(name = "RelatedObjectReference")
    protected RelatedObjectReferenceType relatedObjectReference;
    @XmlElement(name = "CreatedDate")
    protected String createdDate;
    @XmlElement(name = "TransactedDate")
    protected String transactedDate;
    @XmlElement(name = "AcquiredDate")
    protected String acquiredDate;
    @XmlElement(name = "SentDate")
    protected String sentDate;
    @XmlElement(name = "ReceivedDate")
    protected String receivedDate;
    @XmlElement(name = "RegisteredDate")
    protected String registeredDate;
    @XmlElement(name = "StartDate")
    protected String startDate;
    @XmlElement(name = "EndDate")
    protected String endDate;
    @XmlElement(name = "Event")
    protected List<EventType> event;
    @XmlElement(name = "Signature")
    protected List<SignatureType> signature;
    @XmlElement(name = "Gps")
    protected GpsType gps;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlElement(name = "History")
    protected List<ManagementHistoryType> history;

    /**
     * Obtient la valeur de la propriété descriptionLevel.
     * 
     * @return
     *     possible object is
     *     {@link LevelType }
     *     
     */
    public LevelType getDescriptionLevel() {
        return descriptionLevel;
    }

    /**
     * Définit la valeur de la propriété descriptionLevel.
     * 
     * @param value
     *     allowed object is
     *     {@link LevelType }
     *     
     */
    public void setDescriptionLevel(LevelType value) {
        this.descriptionLevel = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the title property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTitle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getTitle() {
        if (title == null) {
            title = new ArrayList<TextType>();
        }
        return this.title;
    }

    /**
     * Gets the value of the filePlanPosition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filePlanPosition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilePlanPosition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFilePlanPosition() {
        if (filePlanPosition == null) {
            filePlanPosition = new ArrayList<String>();
        }
        return this.filePlanPosition;
    }

    /**
     * Gets the value of the systemId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the systemId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSystemId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSystemId() {
        if (systemId == null) {
            systemId = new ArrayList<String>();
        }
        return this.systemId;
    }

    /**
     * Gets the value of the originatingSystemId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the originatingSystemId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOriginatingSystemId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOriginatingSystemId() {
        if (originatingSystemId == null) {
            originatingSystemId = new ArrayList<String>();
        }
        return this.originatingSystemId;
    }

    /**
     * Gets the value of the archivalAgencyArchiveUnitIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the archivalAgencyArchiveUnitIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArchivalAgencyArchiveUnitIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getArchivalAgencyArchiveUnitIdentifier() {
        if (archivalAgencyArchiveUnitIdentifier == null) {
            archivalAgencyArchiveUnitIdentifier = new ArrayList<String>();
        }
        return this.archivalAgencyArchiveUnitIdentifier;
    }

    /**
     * Gets the value of the originatingAgencyArchiveUnitIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the originatingAgencyArchiveUnitIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOriginatingAgencyArchiveUnitIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOriginatingAgencyArchiveUnitIdentifier() {
        if (originatingAgencyArchiveUnitIdentifier == null) {
            originatingAgencyArchiveUnitIdentifier = new ArrayList<String>();
        }
        return this.originatingAgencyArchiveUnitIdentifier;
    }

    /**
     * Gets the value of the transferringAgencyArchiveUnitIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transferringAgencyArchiveUnitIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransferringAgencyArchiveUnitIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTransferringAgencyArchiveUnitIdentifier() {
        if (transferringAgencyArchiveUnitIdentifier == null) {
            transferringAgencyArchiveUnitIdentifier = new ArrayList<String>();
        }
        return this.transferringAgencyArchiveUnitIdentifier;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getDescription() {
        if (description == null) {
            description = new ArrayList<TextType>();
        }
        return this.description;
    }

    /**
     * Obtient la valeur de la propriété custodialHistory.
     * 
     * @return
     *     possible object is
     *     {@link CustodialHistoryType }
     *     
     */
    public CustodialHistoryType getCustodialHistory() {
        return custodialHistory;
    }

    /**
     * Définit la valeur de la propriété custodialHistory.
     * 
     * @param value
     *     allowed object is
     *     {@link CustodialHistoryType }
     *     
     */
    public void setCustodialHistory(CustodialHistoryType value) {
        this.custodialHistory = value;
    }

    /**
     * Obtient la valeur de la propriété type.
     * 
     * @return
     *     possible object is
     *     {@link TextType }
     *     
     */
    public TextType getType() {
        return type;
    }

    /**
     * Définit la valeur de la propriété type.
     * 
     * @param value
     *     allowed object is
     *     {@link TextType }
     *     
     */
    public void setType(TextType value) {
        this.type = value;
    }

    /**
     * Obtient la valeur de la propriété documentType.
     * 
     * @return
     *     possible object is
     *     {@link TextType }
     *     
     */
    public TextType getDocumentType() {
        return documentType;
    }

    /**
     * Définit la valeur de la propriété documentType.
     * 
     * @param value
     *     allowed object is
     *     {@link TextType }
     *     
     */
    public void setDocumentType(TextType value) {
        this.documentType = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the language property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLanguage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLanguage() {
        if (language == null) {
            language = new ArrayList<String>();
        }
        return this.language;
    }

    /**
     * Obtient la valeur de la propriété descriptionLanguage.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptionLanguage() {
        return descriptionLanguage;
    }

    /**
     * Définit la valeur de la propriété descriptionLanguage.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptionLanguage(String value) {
        this.descriptionLanguage = value;
    }

    /**
     * Obtient la valeur de la propriété status.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété version.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Définit la valeur de la propriété version.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the tag property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tag property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTag().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTag() {
        if (tag == null) {
            tag = new ArrayList<String>();
        }
        return this.tag;
    }

    /**
     * Gets the value of the keyword property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keyword property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeyword().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KeywordsType }
     * 
     * 
     */
    public List<KeywordsType> getKeyword() {
        if (keyword == null) {
            keyword = new ArrayList<KeywordsType>();
        }
        return this.keyword;
    }

    /**
     * Obtient la valeur de la propriété coverage.
     * 
     * @return
     *     possible object is
     *     {@link CoverageType }
     *     
     */
    public CoverageType getCoverage() {
        return coverage;
    }

    /**
     * Définit la valeur de la propriété coverage.
     * 
     * @param value
     *     allowed object is
     *     {@link CoverageType }
     *     
     */
    public void setCoverage(CoverageType value) {
        this.coverage = value;
    }

    /**
     * Obtient la valeur de la propriété originatingAgency.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationType }
     *     
     */
    public OrganizationType getOriginatingAgency() {
        return originatingAgency;
    }

    /**
     * Définit la valeur de la propriété originatingAgency.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationType }
     *     
     */
    public void setOriginatingAgency(OrganizationType value) {
        this.originatingAgency = value;
    }

    /**
     * Obtient la valeur de la propriété submissionAgency.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationType }
     *     
     */
    public OrganizationType getSubmissionAgency() {
        return submissionAgency;
    }

    /**
     * Définit la valeur de la propriété submissionAgency.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationType }
     *     
     */
    public void setSubmissionAgency(OrganizationType value) {
        this.submissionAgency = value;
    }

    /**
     * Gets the value of the agentAbstract property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the agentAbstract property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAgentAbstract().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getAgentAbstract() {
        if (agentAbstract == null) {
            agentAbstract = new ArrayList<AgentType>();
        }
        return this.agentAbstract;
    }

    /**
     * Gets the value of the authorizedAgent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the authorizedAgent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuthorizedAgent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getAuthorizedAgent() {
        if (authorizedAgent == null) {
            authorizedAgent = new ArrayList<AgentType>();
        }
        return this.authorizedAgent;
    }

    /**
     * Gets the value of the writer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the writer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWriter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getWriter() {
        if (writer == null) {
            writer = new ArrayList<AgentType>();
        }
        return this.writer;
    }

    /**
     * Gets the value of the addressee property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addressee property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddressee().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getAddressee() {
        if (addressee == null) {
            addressee = new ArrayList<AgentType>();
        }
        return this.addressee;
    }

    /**
     * Gets the value of the recipient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recipient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecipient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getRecipient() {
        if (recipient == null) {
            recipient = new ArrayList<AgentType>();
        }
        return this.recipient;
    }

    /**
     * Gets the value of the transmitter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transmitter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransmitter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getTransmitter() {
        if (transmitter == null) {
            transmitter = new ArrayList<AgentType>();
        }
        return this.transmitter;
    }

    /**
     * Gets the value of the sender property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sender property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSender().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AgentType }
     * 
     * 
     */
    public List<AgentType> getSender() {
        if (sender == null) {
            sender = new ArrayList<AgentType>();
        }
        return this.sender;
    }

    /**
     * Obtient la valeur de la propriété source.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Définit la valeur de la propriété source.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Obtient la valeur de la propriété relatedObjectReference.
     * 
     * @return
     *     possible object is
     *     {@link RelatedObjectReferenceType }
     *     
     */
    public RelatedObjectReferenceType getRelatedObjectReference() {
        return relatedObjectReference;
    }

    /**
     * Définit la valeur de la propriété relatedObjectReference.
     * 
     * @param value
     *     allowed object is
     *     {@link RelatedObjectReferenceType }
     *     
     */
    public void setRelatedObjectReference(RelatedObjectReferenceType value) {
        this.relatedObjectReference = value;
    }

    /**
     * Obtient la valeur de la propriété createdDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Définit la valeur de la propriété createdDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedDate(String value) {
        this.createdDate = value;
    }

    /**
     * Obtient la valeur de la propriété transactedDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactedDate() {
        return transactedDate;
    }

    /**
     * Définit la valeur de la propriété transactedDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactedDate(String value) {
        this.transactedDate = value;
    }

    /**
     * Obtient la valeur de la propriété acquiredDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcquiredDate() {
        return acquiredDate;
    }

    /**
     * Définit la valeur de la propriété acquiredDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcquiredDate(String value) {
        this.acquiredDate = value;
    }

    /**
     * Obtient la valeur de la propriété sentDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSentDate() {
        return sentDate;
    }

    /**
     * Définit la valeur de la propriété sentDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSentDate(String value) {
        this.sentDate = value;
    }

    /**
     * Obtient la valeur de la propriété receivedDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivedDate() {
        return receivedDate;
    }

    /**
     * Définit la valeur de la propriété receivedDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivedDate(String value) {
        this.receivedDate = value;
    }

    /**
     * Obtient la valeur de la propriété registeredDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegisteredDate() {
        return registeredDate;
    }

    /**
     * Définit la valeur de la propriété registeredDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegisteredDate(String value) {
        this.registeredDate = value;
    }

    /**
     * Obtient la valeur de la propriété startDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Définit la valeur de la propriété startDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDate(String value) {
        this.startDate = value;
    }

    /**
     * Obtient la valeur de la propriété endDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Définit la valeur de la propriété endDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDate(String value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the event property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the event property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventType }
     * 
     * 
     */
    public List<EventType> getEvent() {
        if (event == null) {
            event = new ArrayList<EventType>();
        }
        return this.event;
    }

    /**
     * Gets the value of the signature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSignature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SignatureType }
     * 
     * 
     */
    public List<SignatureType> getSignature() {
        if (signature == null) {
            signature = new ArrayList<SignatureType>();
        }
        return this.signature;
    }

    /**
     * Obtient la valeur de la propriété gps.
     * 
     * @return
     *     possible object is
     *     {@link GpsType }
     *     
     */
    public GpsType getGps() {
        return gps;
    }

    /**
     * Définit la valeur de la propriété gps.
     * 
     * @param value
     *     allowed object is
     *     {@link GpsType }
     *     
     */
    public void setGps(GpsType value) {
        this.gps = value;
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
     * Gets the value of the history property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the history property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHistory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ManagementHistoryType }
     * 
     * 
     */
    public List<ManagementHistoryType> getHistory() {
        if (history == null) {
            history = new ArrayList<ManagementHistoryType>();
        }
        return this.history;
    }

}
