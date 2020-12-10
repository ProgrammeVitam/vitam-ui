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
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Classe Java pour ManagementType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ManagementType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}ManagementGroup"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManagementType", propOrder = {
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
public class ManagementType {

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

}
