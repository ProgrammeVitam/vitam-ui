//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour GpsType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="GpsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GpsVersionID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="GpsAltitude" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="GpsAltitudeRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="GpsLatitude" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="GpsLatitudeRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="GpsLongitude" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="GpsLongitudeRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="GpsDateStamp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GpsType", propOrder = {
    "gpsVersionID",
    "gpsAltitude",
    "gpsAltitudeRef",
    "gpsLatitude",
    "gpsLatitudeRef",
    "gpsLongitude",
    "gpsLongitudeRef",
    "gpsDateStamp"
})
public class GpsType {

    @XmlElement(name = "GpsVersionID")
    protected String gpsVersionID;
    @XmlElement(name = "GpsAltitude")
    protected BigInteger gpsAltitude;
    @XmlElement(name = "GpsAltitudeRef")
    protected String gpsAltitudeRef;
    @XmlElement(name = "GpsLatitude")
    protected String gpsLatitude;
    @XmlElement(name = "GpsLatitudeRef")
    protected String gpsLatitudeRef;
    @XmlElement(name = "GpsLongitude")
    protected String gpsLongitude;
    @XmlElement(name = "GpsLongitudeRef")
    protected String gpsLongitudeRef;
    @XmlElement(name = "GpsDateStamp")
    protected String gpsDateStamp;

    /**
     * Obtient la valeur de la propriété gpsVersionID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsVersionID() {
        return gpsVersionID;
    }

    /**
     * Définit la valeur de la propriété gpsVersionID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsVersionID(String value) {
        this.gpsVersionID = value;
    }

    /**
     * Obtient la valeur de la propriété gpsAltitude.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGpsAltitude() {
        return gpsAltitude;
    }

    /**
     * Définit la valeur de la propriété gpsAltitude.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGpsAltitude(BigInteger value) {
        this.gpsAltitude = value;
    }

    /**
     * Obtient la valeur de la propriété gpsAltitudeRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsAltitudeRef() {
        return gpsAltitudeRef;
    }

    /**
     * Définit la valeur de la propriété gpsAltitudeRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsAltitudeRef(String value) {
        this.gpsAltitudeRef = value;
    }

    /**
     * Obtient la valeur de la propriété gpsLatitude.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsLatitude() {
        return gpsLatitude;
    }

    /**
     * Définit la valeur de la propriété gpsLatitude.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsLatitude(String value) {
        this.gpsLatitude = value;
    }

    /**
     * Obtient la valeur de la propriété gpsLatitudeRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsLatitudeRef() {
        return gpsLatitudeRef;
    }

    /**
     * Définit la valeur de la propriété gpsLatitudeRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsLatitudeRef(String value) {
        this.gpsLatitudeRef = value;
    }

    /**
     * Obtient la valeur de la propriété gpsLongitude.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsLongitude() {
        return gpsLongitude;
    }

    /**
     * Définit la valeur de la propriété gpsLongitude.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsLongitude(String value) {
        this.gpsLongitude = value;
    }

    /**
     * Obtient la valeur de la propriété gpsLongitudeRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsLongitudeRef() {
        return gpsLongitudeRef;
    }

    /**
     * Définit la valeur de la propriété gpsLongitudeRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsLongitudeRef(String value) {
        this.gpsLongitudeRef = value;
    }

    /**
     * Obtient la valeur de la propriété gpsDateStamp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpsDateStamp() {
        return gpsDateStamp;
    }

    /**
     * Définit la valeur de la propriété gpsDateStamp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpsDateStamp(String value) {
        this.gpsDateStamp = value;
    }

}
