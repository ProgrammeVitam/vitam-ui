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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour SignerType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SignerType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}SignerGroup"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignerType", propOrder = {
    "firstName",
    "birthName",
    "fullName",
    "givenName",
    "gender",
    "birthDate",
    "birthPlace",
    "deathDate",
    "deathPlace",
    "nationality",
    "corpname",
    "identifier",
    "signingTime",
    "function",
    "activity",
    "position",
    "role",
    "mandate"
})
public class SignerType {

    @XmlElement(name = "FirstName")
    protected String firstName;
    @XmlElement(name = "BirthName")
    protected String birthName;
    @XmlElement(name = "FullName")
    protected String fullName;
    @XmlElement(name = "GivenName")
    protected String givenName;
    @XmlElement(name = "Gender")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String gender;
    @XmlElement(name = "BirthDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar birthDate;
    @XmlElement(name = "BirthPlace")
    protected BirthOrDeathPlaceType birthPlace;
    @XmlElement(name = "DeathDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar deathDate;
    @XmlElement(name = "DeathPlace")
    protected BirthOrDeathPlaceType deathPlace;
    @XmlElement(name = "Nationality")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> nationality;
    @XmlElement(name = "Corpname")
    protected String corpname;
    @XmlElement(name = "Identifier")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> identifier;
    @XmlElement(name = "SigningTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar signingTime;
    @XmlElement(name = "Function")
    protected List<TextType> function;
    @XmlElement(name = "Activity")
    protected List<TextType> activity;
    @XmlElement(name = "Position")
    protected List<TextType> position;
    @XmlElement(name = "Role")
    protected List<TextType> role;
    @XmlElement(name = "Mandate")
    protected List<TextType> mandate;

    /**
     * Obtient la valeur de la propriété firstName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Définit la valeur de la propriété firstName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Obtient la valeur de la propriété birthName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBirthName() {
        return birthName;
    }

    /**
     * Définit la valeur de la propriété birthName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBirthName(String value) {
        this.birthName = value;
    }

    /**
     * Obtient la valeur de la propriété fullName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Définit la valeur de la propriété fullName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullName(String value) {
        this.fullName = value;
    }

    /**
     * Obtient la valeur de la propriété givenName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Définit la valeur de la propriété givenName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGivenName(String value) {
        this.givenName = value;
    }

    /**
     * Obtient la valeur de la propriété gender.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGender() {
        return gender;
    }

    /**
     * Définit la valeur de la propriété gender.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGender(String value) {
        this.gender = value;
    }

    /**
     * Obtient la valeur de la propriété birthDate.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBirthDate() {
        return birthDate;
    }

    /**
     * Définit la valeur de la propriété birthDate.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBirthDate(XMLGregorianCalendar value) {
        this.birthDate = value;
    }

    /**
     * Obtient la valeur de la propriété birthPlace.
     * 
     * @return
     *     possible object is
     *     {@link BirthOrDeathPlaceType }
     *     
     */
    public BirthOrDeathPlaceType getBirthPlace() {
        return birthPlace;
    }

    /**
     * Définit la valeur de la propriété birthPlace.
     * 
     * @param value
     *     allowed object is
     *     {@link BirthOrDeathPlaceType }
     *     
     */
    public void setBirthPlace(BirthOrDeathPlaceType value) {
        this.birthPlace = value;
    }

    /**
     * Obtient la valeur de la propriété deathDate.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDeathDate() {
        return deathDate;
    }

    /**
     * Définit la valeur de la propriété deathDate.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDeathDate(XMLGregorianCalendar value) {
        this.deathDate = value;
    }

    /**
     * Obtient la valeur de la propriété deathPlace.
     * 
     * @return
     *     possible object is
     *     {@link BirthOrDeathPlaceType }
     *     
     */
    public BirthOrDeathPlaceType getDeathPlace() {
        return deathPlace;
    }

    /**
     * Définit la valeur de la propriété deathPlace.
     * 
     * @param value
     *     allowed object is
     *     {@link BirthOrDeathPlaceType }
     *     
     */
    public void setDeathPlace(BirthOrDeathPlaceType value) {
        this.deathPlace = value;
    }

    /**
     * Gets the value of the nationality property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nationality property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNationality().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNationality() {
        if (nationality == null) {
            nationality = new ArrayList<String>();
        }
        return this.nationality;
    }

    /**
     * Obtient la valeur de la propriété corpname.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorpname() {
        return corpname;
    }

    /**
     * Définit la valeur de la propriété corpname.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorpname(String value) {
        this.corpname = value;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<String>();
        }
        return this.identifier;
    }

    /**
     * Obtient la valeur de la propriété signingTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSigningTime() {
        return signingTime;
    }

    /**
     * Définit la valeur de la propriété signingTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSigningTime(XMLGregorianCalendar value) {
        this.signingTime = value;
    }

    /**
     * Gets the value of the function property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the function property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFunction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getFunction() {
        if (function == null) {
            function = new ArrayList<TextType>();
        }
        return this.function;
    }

    /**
     * Gets the value of the activity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getActivity() {
        if (activity == null) {
            activity = new ArrayList<TextType>();
        }
        return this.activity;
    }

    /**
     * Gets the value of the position property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the position property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPosition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getPosition() {
        if (position == null) {
            position = new ArrayList<TextType>();
        }
        return this.position;
    }

    /**
     * Gets the value of the role property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the role property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getRole() {
        if (role == null) {
            role = new ArrayList<TextType>();
        }
        return this.role;
    }

    /**
     * Gets the value of the mandate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TextType }
     * 
     * 
     */
    public List<TextType> getMandate() {
        if (mandate == null) {
            mandate = new ArrayList<TextType>();
        }
        return this.mandate;
    }

}
