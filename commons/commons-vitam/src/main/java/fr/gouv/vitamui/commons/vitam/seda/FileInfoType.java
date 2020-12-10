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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Informations sur le fichier lui-même (d'un point de vue technique).
 * 
 * <p>Classe Java pour FileInfoType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="FileInfoType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Filename" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="CreatingApplicationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreatingApplicationVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="DateCreatedByApplication" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="CreatingOs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreatingOsVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="LastModified" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileInfoType", propOrder = {
    "filename",
    "creatingApplicationName",
    "creatingApplicationVersion",
    "dateCreatedByApplication",
    "creatingOs",
    "creatingOsVersion",
    "lastModified"
})
public class FileInfoType {

    @XmlElement(name = "Filename", required = true)
    protected String filename;
    @XmlElement(name = "CreatingApplicationName")
    protected String creatingApplicationName;
    @XmlElement(name = "CreatingApplicationVersion")
    protected String creatingApplicationVersion;
    @XmlElement(name = "DateCreatedByApplication")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateCreatedByApplication;
    @XmlElement(name = "CreatingOs")
    protected String creatingOs;
    @XmlElement(name = "CreatingOsVersion")
    protected String creatingOsVersion;
    @XmlElement(name = "LastModified")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastModified;

    /**
     * Obtient la valeur de la propriété filename.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Définit la valeur de la propriété filename.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Obtient la valeur de la propriété creatingApplicationName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatingApplicationName() {
        return creatingApplicationName;
    }

    /**
     * Définit la valeur de la propriété creatingApplicationName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatingApplicationName(String value) {
        this.creatingApplicationName = value;
    }

    /**
     * Obtient la valeur de la propriété creatingApplicationVersion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatingApplicationVersion() {
        return creatingApplicationVersion;
    }

    /**
     * Définit la valeur de la propriété creatingApplicationVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatingApplicationVersion(String value) {
        this.creatingApplicationVersion = value;
    }

    /**
     * Obtient la valeur de la propriété dateCreatedByApplication.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateCreatedByApplication() {
        return dateCreatedByApplication;
    }

    /**
     * Définit la valeur de la propriété dateCreatedByApplication.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateCreatedByApplication(XMLGregorianCalendar value) {
        this.dateCreatedByApplication = value;
    }

    /**
     * Obtient la valeur de la propriété creatingOs.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatingOs() {
        return creatingOs;
    }

    /**
     * Définit la valeur de la propriété creatingOs.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatingOs(String value) {
        this.creatingOs = value;
    }

    /**
     * Obtient la valeur de la propriété creatingOsVersion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatingOsVersion() {
        return creatingOsVersion;
    }

    /**
     * Définit la valeur de la propriété creatingOsVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatingOsVersion(String value) {
        this.creatingOsVersion = value;
    }

    /**
     * Obtient la valeur de la propriété lastModified.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastModified() {
        return lastModified;
    }

    /**
     * Définit la valeur de la propriété lastModified.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastModified(XMLGregorianCalendar value) {
        this.lastModified = value;
    }

}
