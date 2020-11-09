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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * La liste d'identifiants de règles à appliquer et à ignorer qui doit être appliquée à partir de cet ArchiveUnit.
 * 
 * <p>Classe Java pour AppraisalRuleType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AppraisalRuleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="Rule" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}RuleIdType"/&gt;
 *           &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;/sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}PreventInheritanceGroup"/&gt;
 *           &lt;element name="RefNonRuleId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}RuleIdType" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="FinalAction" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}FinalActionAppraisalCodeType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppraisalRuleType", propOrder = {
    "ruleAndStartDate",
    "preventInheritance",
    "refNonRuleId",
    "finalAction"
})
public class AppraisalRuleType {

    @XmlElements({
        @XmlElement(name = "Rule", type = RuleIdType.class),
        @XmlElement(name = "StartDate", type = XMLGregorianCalendar.class, nillable = true)
    })
    protected List<Object> ruleAndStartDate;
    @XmlElement(name = "PreventInheritance", defaultValue = "false")
    protected Boolean preventInheritance;
    @XmlElement(name = "RefNonRuleId")
    protected List<RuleIdType> refNonRuleId;
    @XmlElement(name = "FinalAction", required = true)
    @XmlSchemaType(name = "token")
    protected FinalActionAppraisalCodeType finalAction;

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
     * Obtient la valeur de la propriété finalAction.
     * 
     * @return
     *     possible object is
     *     {@link FinalActionAppraisalCodeType }
     *     
     */
    public FinalActionAppraisalCodeType getFinalAction() {
        return finalAction;
    }

    /**
     * Définit la valeur de la propriété finalAction.
     * 
     * @param value
     *     allowed object is
     *     {@link FinalActionAppraisalCodeType }
     *     
     */
    public void setFinalAction(FinalActionAppraisalCodeType value) {
        this.finalAction = value;
    }

}
