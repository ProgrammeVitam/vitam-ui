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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * La liste d'identifiants de règles à appliquer et à ignorer qui doit être appliquée à partir de cet ArchiveUnit.
 * 
 * <p>Classe Java pour ClassificationRuleType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ClassificationRuleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="Rule" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}RuleIdType"/&gt;
 *           &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;/sequence&gt;
 *         &lt;element name="ClassificationAudience" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}PreventInheritanceGroup"/&gt;
 *           &lt;element name="RefNonRuleId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}RuleIdType" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="ClassificationLevel" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *         &lt;element name="ClassificationOwner" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"/&gt;
 *         &lt;element name="ClassificationReassessingDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="NeedReassessingAuthorization" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassificationRuleType", propOrder = {
    "ruleAndStartDate",
    "classificationAudience",
    "preventInheritance",
    "refNonRuleId",
    "classificationLevel",
    "classificationOwner",
    "classificationReassessingDate",
    "needReassessingAuthorization"
})
public class ClassificationRuleType {

    @XmlElements({
        @XmlElement(name = "Rule", type = RuleIdType.class),
        @XmlElement(name = "StartDate", type = XMLGregorianCalendar.class, nillable = true)
    })
    protected List<Object> ruleAndStartDate;
    @XmlElement(name = "ClassificationAudience")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String classificationAudience;
    @XmlElement(name = "PreventInheritance", defaultValue = "false")
    protected Boolean preventInheritance;
    @XmlElement(name = "RefNonRuleId")
    protected List<RuleIdType> refNonRuleId;
    @XmlElement(name = "ClassificationLevel", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String classificationLevel;
    @XmlElement(name = "ClassificationOwner", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String classificationOwner;
    @XmlElement(name = "ClassificationReassessingDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar classificationReassessingDate;
    @XmlElement(name = "NeedReassessingAuthorization")
    protected Boolean needReassessingAuthorization;

    /**
     * Gets the value of the ruleAndStartDate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ruleAndStartDate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRuleAndStartDate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RuleIdType }
     * {@link XMLGregorianCalendar }
     * 
     * 
     */
    public List<Object> getRuleAndStartDate() {
        if (ruleAndStartDate == null) {
            ruleAndStartDate = new ArrayList<Object>();
        }
        return this.ruleAndStartDate;
    }

    /**
     * Obtient la valeur de la propriété classificationAudience.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassificationAudience() {
        return classificationAudience;
    }

    /**
     * Définit la valeur de la propriété classificationAudience.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassificationAudience(String value) {
        this.classificationAudience = value;
    }

    /**
     * Obtient la valeur de la propriété preventInheritance.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPreventInheritance() {
        return preventInheritance;
    }

    /**
     * Définit la valeur de la propriété preventInheritance.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreventInheritance(Boolean value) {
        this.preventInheritance = value;
    }

    /**
     * Gets the value of the refNonRuleId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the refNonRuleId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRefNonRuleId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RuleIdType }
     * 
     * 
     */
    public List<RuleIdType> getRefNonRuleId() {
        if (refNonRuleId == null) {
            refNonRuleId = new ArrayList<RuleIdType>();
        }
        return this.refNonRuleId;
    }

    /**
     * Obtient la valeur de la propriété classificationLevel.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassificationLevel() {
        return classificationLevel;
    }

    /**
     * Définit la valeur de la propriété classificationLevel.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassificationLevel(String value) {
        this.classificationLevel = value;
    }

    /**
     * Obtient la valeur de la propriété classificationOwner.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassificationOwner() {
        return classificationOwner;
    }

    /**
     * Définit la valeur de la propriété classificationOwner.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassificationOwner(String value) {
        this.classificationOwner = value;
    }

    /**
     * Obtient la valeur de la propriété classificationReassessingDate.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getClassificationReassessingDate() {
        return classificationReassessingDate;
    }

    /**
     * Définit la valeur de la propriété classificationReassessingDate.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setClassificationReassessingDate(XMLGregorianCalendar value) {
        this.classificationReassessingDate = value;
    }

    /**
     * Obtient la valeur de la propriété needReassessingAuthorization.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNeedReassessingAuthorization() {
        return needReassessingAuthorization;
    }

    /**
     * Définit la valeur de la propriété needReassessingAuthorization.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNeedReassessingAuthorization(Boolean value) {
        this.needReassessingAuthorization = value;
    }

}
