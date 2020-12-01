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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Element;


/**
 * <p>Classe Java pour EventType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="EventType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EventIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="EventTypeCode" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="EventType" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="EventDateTime" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DateType"/&gt;
 *         &lt;element name="EventDetail" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextType" minOccurs="0"/&gt;
 *         &lt;element name="Outcome" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="OutcomeDetail" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="OutcomeDetailMessage" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="EventDetailData" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventType", propOrder = {
    "eventIdentifier",
    "eventTypeCode",
    "eventType",
    "eventDateTime",
    "eventDetail",
    "outcome",
    "outcomeDetail",
    "outcomeDetailMessage",
    "eventDetailData",
    "any"
})
@XmlSeeAlso({
    EventLogBookOgType.class
})
public class EventType {

    @XmlElement(name = "EventIdentifier")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String eventIdentifier;
    @XmlElement(name = "EventTypeCode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String eventTypeCode;
    @XmlElement(name = "EventType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String eventType;
    @XmlElement(name = "EventDateTime", required = true)
    protected String eventDateTime;
    @XmlElement(name = "EventDetail")
    protected TextType eventDetail;
    @XmlElement(name = "Outcome")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String outcome;
    @XmlElement(name = "OutcomeDetail")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String outcomeDetail;
    @XmlElement(name = "OutcomeDetailMessage")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String outcomeDetailMessage;
    @XmlElement(name = "EventDetailData")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String eventDetailData;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Obtient la valeur de la propriété eventIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventIdentifier() {
        return eventIdentifier;
    }

    /**
     * Définit la valeur de la propriété eventIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventIdentifier(String value) {
        this.eventIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété eventTypeCode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventTypeCode() {
        return eventTypeCode;
    }

    /**
     * Définit la valeur de la propriété eventTypeCode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventTypeCode(String value) {
        this.eventTypeCode = value;
    }

    /**
     * Obtient la valeur de la propriété eventType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Définit la valeur de la propriété eventType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventType(String value) {
        this.eventType = value;
    }

    /**
     * Obtient la valeur de la propriété eventDateTime.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventDateTime() {
        return eventDateTime;
    }

    /**
     * Définit la valeur de la propriété eventDateTime.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventDateTime(String value) {
        this.eventDateTime = value;
    }

    /**
     * Obtient la valeur de la propriété eventDetail.
     * 
     * @return
     *     possible object is
     *     {@link TextType }
     *     
     */
    public TextType getEventDetail() {
        return eventDetail;
    }

    /**
     * Définit la valeur de la propriété eventDetail.
     * 
     * @param value
     *     allowed object is
     *     {@link TextType }
     *     
     */
    public void setEventDetail(TextType value) {
        this.eventDetail = value;
    }

    /**
     * Obtient la valeur de la propriété outcome.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutcome() {
        return outcome;
    }

    /**
     * Définit la valeur de la propriété outcome.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutcome(String value) {
        this.outcome = value;
    }

    /**
     * Obtient la valeur de la propriété outcomeDetail.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutcomeDetail() {
        return outcomeDetail;
    }

    /**
     * Définit la valeur de la propriété outcomeDetail.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutcomeDetail(String value) {
        this.outcomeDetail = value;
    }

    /**
     * Obtient la valeur de la propriété outcomeDetailMessage.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutcomeDetailMessage() {
        return outcomeDetailMessage;
    }

    /**
     * Définit la valeur de la propriété outcomeDetailMessage.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutcomeDetailMessage(String value) {
        this.outcomeDetailMessage = value;
    }

    /**
     * Obtient la valeur de la propriété eventDetailData.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventDetailData() {
        return eventDetailData;
    }

    /**
     * Définit la valeur de la propriété eventDetailData.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventDetailData(String value) {
        this.eventDetailData = value;
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

}
