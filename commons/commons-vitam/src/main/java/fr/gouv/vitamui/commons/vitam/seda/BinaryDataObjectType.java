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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Objet-données numérique.
 * 
 * Métadonnées techniques minimales : URI, Digest, Poids.
 * 
 * <p>Classe Java pour BinaryDataObjectType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="BinaryDataObjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}MinimalDataObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}MinimalBinaryDataObjectGroup" minOccurs="0"/&gt;
 *         &lt;element name="Size" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}SizeInBytesType" minOccurs="0"/&gt;
 *         &lt;element name="Compressed" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CompressedType" minOccurs="0"/&gt;
 *         &lt;group ref="{fr:gouv:culture:archivesdefrance:seda:v2.1}BinaryTechnicalDescriptionGroup"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryDataObjectType", propOrder = {
    "attachment",
    "uri",
    "messageDigest",
    "size",
    "compressed",
    "formatIdentification",
    "fileInfo",
    "metadata",
    "otherMetadata"
})
public class BinaryDataObjectType
    extends MinimalDataObjectType
{

    @XmlElement(name = "Attachment")
    protected BinaryObjectType attachment;
    @XmlElement(name = "Uri")
    @XmlSchemaType(name = "anyURI")
    protected String uri;
    @XmlElement(name = "MessageDigest")
    protected MessageDigestBinaryObjectType messageDigest;
    @XmlElement(name = "Size")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger size;
    @XmlElement(name = "Compressed")
    protected CompressedType compressed;
    @XmlElement(name = "FormatIdentification")
    protected FormatIdentificationType formatIdentification;
    @XmlElement(name = "FileInfo")
    protected FileInfoType fileInfo;
    @XmlElement(name = "Metadata")
    protected CoreMetadataType metadata;
    @XmlElement(name = "OtherMetadata")
    protected DescriptiveTechnicalMetadataType otherMetadata;

    /**
     * Obtient la valeur de la propriété attachment.
     * 
     * @return
     *     possible object is
     *     {@link BinaryObjectType }
     *     
     */
    public BinaryObjectType getAttachment() {
        return attachment;
    }

    /**
     * Définit la valeur de la propriété attachment.
     * 
     * @param value
     *     allowed object is
     *     {@link BinaryObjectType }
     *     
     */
    public void setAttachment(BinaryObjectType value) {
        this.attachment = value;
    }

    /**
     * Obtient la valeur de la propriété uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri() {
        return uri;
    }

    /**
     * Définit la valeur de la propriété uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value) {
        this.uri = value;
    }

    /**
     * Obtient la valeur de la propriété messageDigest.
     * 
     * @return
     *     possible object is
     *     {@link MessageDigestBinaryObjectType }
     *     
     */
    public MessageDigestBinaryObjectType getMessageDigest() {
        return messageDigest;
    }

    /**
     * Définit la valeur de la propriété messageDigest.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageDigestBinaryObjectType }
     *     
     */
    public void setMessageDigest(MessageDigestBinaryObjectType value) {
        this.messageDigest = value;
    }

    /**
     * Obtient la valeur de la propriété size.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSize() {
        return size;
    }

    /**
     * Définit la valeur de la propriété size.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSize(BigInteger value) {
        this.size = value;
    }

    /**
     * Obtient la valeur de la propriété compressed.
     * 
     * @return
     *     possible object is
     *     {@link CompressedType }
     *     
     */
    public CompressedType getCompressed() {
        return compressed;
    }

    /**
     * Définit la valeur de la propriété compressed.
     * 
     * @param value
     *     allowed object is
     *     {@link CompressedType }
     *     
     */
    public void setCompressed(CompressedType value) {
        this.compressed = value;
    }

    /**
     * Obtient la valeur de la propriété formatIdentification.
     * 
     * @return
     *     possible object is
     *     {@link FormatIdentificationType }
     *     
     */
    public FormatIdentificationType getFormatIdentification() {
        return formatIdentification;
    }

    /**
     * Définit la valeur de la propriété formatIdentification.
     * 
     * @param value
     *     allowed object is
     *     {@link FormatIdentificationType }
     *     
     */
    public void setFormatIdentification(FormatIdentificationType value) {
        this.formatIdentification = value;
    }

    /**
     * Obtient la valeur de la propriété fileInfo.
     * 
     * @return
     *     possible object is
     *     {@link FileInfoType }
     *     
     */
    public FileInfoType getFileInfo() {
        return fileInfo;
    }

    /**
     * Définit la valeur de la propriété fileInfo.
     * 
     * @param value
     *     allowed object is
     *     {@link FileInfoType }
     *     
     */
    public void setFileInfo(FileInfoType value) {
        this.fileInfo = value;
    }

    /**
     * Obtient la valeur de la propriété metadata.
     * 
     * @return
     *     possible object is
     *     {@link CoreMetadataType }
     *     
     */
    public CoreMetadataType getMetadata() {
        return metadata;
    }

    /**
     * Définit la valeur de la propriété metadata.
     * 
     * @param value
     *     allowed object is
     *     {@link CoreMetadataType }
     *     
     */
    public void setMetadata(CoreMetadataType value) {
        this.metadata = value;
    }

    /**
     * Obtient la valeur de la propriété otherMetadata.
     * 
     * @return
     *     possible object is
     *     {@link DescriptiveTechnicalMetadataType }
     *     
     */
    public DescriptiveTechnicalMetadataType getOtherMetadata() {
        return otherMetadata;
    }

    /**
     * Définit la valeur de la propriété otherMetadata.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptiveTechnicalMetadataType }
     *     
     */
    public void setOtherMetadata(DescriptiveTechnicalMetadataType value) {
        this.otherMetadata = value;
    }

}
