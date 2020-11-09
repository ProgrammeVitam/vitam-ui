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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour FormatIdentificationType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="FormatIdentificationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FormatLitteral" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MimeType" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MimeTypeType" minOccurs="0"/&gt;
 *         &lt;element name="FormatId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}FileFormatType" minOccurs="0"/&gt;
 *         &lt;element name="Encoding" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}EncodingType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FormatIdentificationType", propOrder = {
    "formatLitteral",
    "mimeType",
    "formatId",
    "encoding"
})
public class FormatIdentificationType {

    @XmlElement(name = "FormatLitteral")
    protected String formatLitteral;
    @XmlElement(name = "MimeType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String mimeType;
    @XmlElement(name = "FormatId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String formatId;
    @XmlElement(name = "Encoding")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String encoding;

    /**
     * Obtient la valeur de la propriété formatLitteral.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatLitteral() {
        return formatLitteral;
    }

    /**
     * Définit la valeur de la propriété formatLitteral.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatLitteral(String value) {
        this.formatLitteral = value;
    }

    /**
     * Obtient la valeur de la propriété mimeType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Définit la valeur de la propriété mimeType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * Obtient la valeur de la propriété formatId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatId() {
        return formatId;
    }

    /**
     * Définit la valeur de la propriété formatId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatId(String value) {
        this.formatId = value;
    }

    /**
     * Obtient la valeur de la propriété encoding.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Définit la valeur de la propriété encoding.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

}
