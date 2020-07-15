//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * Métadonnées de base par type d'objet-données.
 * 
 * <p>Classe Java pour CoreMetadataType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="CoreMetadataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Text" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextTechnicalMetadataType"/&gt;
 *         &lt;element name="Document" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DocumentTechnicalMetadataType"/&gt;
 *         &lt;element name="Image" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}ImageTechnicalMetadataType"/&gt;
 *         &lt;element name="Audio" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}AudioTechnicalMetadataType"/&gt;
 *         &lt;element name="Video" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}VideoTechnicalMetadataType"/&gt;
 *         &lt;any processContents='lax' minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoreMetadataType", propOrder = {
    "text",
    "document",
    "image",
    "audio",
    "video",
    "any"
})
public class CoreMetadataType {

    @XmlElement(name = "Text")
    protected TextTechnicalMetadataType text;
    @XmlElement(name = "Document")
    protected DocumentTechnicalMetadataType document;
    @XmlElement(name = "Image")
    protected ImageTechnicalMetadataType image;
    @XmlElement(name = "Audio")
    protected AudioTechnicalMetadataType audio;
    @XmlElement(name = "Video")
    protected VideoTechnicalMetadataType video;
    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Obtient la valeur de la propriété text.
     * 
     * @return
     *     possible object is
     *     {@link TextTechnicalMetadataType }
     *     
     */
    public TextTechnicalMetadataType getText() {
        return text;
    }

    /**
     * Définit la valeur de la propriété text.
     * 
     * @param value
     *     allowed object is
     *     {@link TextTechnicalMetadataType }
     *     
     */
    public void setText(TextTechnicalMetadataType value) {
        this.text = value;
    }

    /**
     * Obtient la valeur de la propriété document.
     * 
     * @return
     *     possible object is
     *     {@link DocumentTechnicalMetadataType }
     *     
     */
    public DocumentTechnicalMetadataType getDocument() {
        return document;
    }

    /**
     * Définit la valeur de la propriété document.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentTechnicalMetadataType }
     *     
     */
    public void setDocument(DocumentTechnicalMetadataType value) {
        this.document = value;
    }

    /**
     * Obtient la valeur de la propriété image.
     * 
     * @return
     *     possible object is
     *     {@link ImageTechnicalMetadataType }
     *     
     */
    public ImageTechnicalMetadataType getImage() {
        return image;
    }

    /**
     * Définit la valeur de la propriété image.
     * 
     * @param value
     *     allowed object is
     *     {@link ImageTechnicalMetadataType }
     *     
     */
    public void setImage(ImageTechnicalMetadataType value) {
        this.image = value;
    }

    /**
     * Obtient la valeur de la propriété audio.
     * 
     * @return
     *     possible object is
     *     {@link AudioTechnicalMetadataType }
     *     
     */
    public AudioTechnicalMetadataType getAudio() {
        return audio;
    }

    /**
     * Définit la valeur de la propriété audio.
     * 
     * @param value
     *     allowed object is
     *     {@link AudioTechnicalMetadataType }
     *     
     */
    public void setAudio(AudioTechnicalMetadataType value) {
        this.audio = value;
    }

    /**
     * Obtient la valeur de la propriété video.
     * 
     * @return
     *     possible object is
     *     {@link VideoTechnicalMetadataType }
     *     
     */
    public VideoTechnicalMetadataType getVideo() {
        return video;
    }

    /**
     * Définit la valeur de la propriété video.
     * 
     * @param value
     *     allowed object is
     *     {@link VideoTechnicalMetadataType }
     *     
     */
    public void setVideo(VideoTechnicalMetadataType value) {
        this.video = value;
    }

    /**
     * Obtient la valeur de la propriété any.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Définit la valeur de la propriété any.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

}
