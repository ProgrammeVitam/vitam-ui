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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SignatureType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SignatureType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Signer" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}SignerType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Validator" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ValidatorType"/&gt;
 *         &lt;element name="Masterdata" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CodeType" minOccurs="0"/&gt;
 *         &lt;element name="ReferencedObject" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ReferencedObjectType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignatureType", propOrder = {
    "signer",
    "validator",
    "masterdata",
    "referencedObject"
})
public class SignatureType {

    @XmlElement(name = "Signer", required = true)
    protected List<SignerType> signer;
    @XmlElement(name = "Validator", required = true)
    protected ValidatorType validator;
    @XmlElement(name = "Masterdata")
    protected CodeType masterdata;
    @XmlElement(name = "ReferencedObject", required = true)
    protected ReferencedObjectType referencedObject;

    /**
     * Gets the value of the signer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSigner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SignerType }
     * 
     * 
     */
    public List<SignerType> getSigner() {
        if (signer == null) {
            signer = new ArrayList<SignerType>();
        }
        return this.signer;
    }

    /**
     * Obtient la valeur de la propriété validator.
     * 
     * @return
     *     possible object is
     *     {@link ValidatorType }
     *     
     */
    public ValidatorType getValidator() {
        return validator;
    }

    /**
     * Définit la valeur de la propriété validator.
     * 
     * @param value
     *     allowed object is
     *     {@link ValidatorType }
     *     
     */
    public void setValidator(ValidatorType value) {
        this.validator = value;
    }

    /**
     * Obtient la valeur de la propriété masterdata.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getMasterdata() {
        return masterdata;
    }

    /**
     * Définit la valeur de la propriété masterdata.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setMasterdata(CodeType value) {
        this.masterdata = value;
    }

    /**
     * Obtient la valeur de la propriété referencedObject.
     * 
     * @return
     *     possible object is
     *     {@link ReferencedObjectType }
     *     
     */
    public ReferencedObjectType getReferencedObject() {
        return referencedObject;
    }

    /**
     * Définit la valeur de la propriété referencedObject.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferencedObjectType }
     *     
     */
    public void setReferencedObject(ReferencedObjectType value) {
        this.referencedObject = value;
    }

}
