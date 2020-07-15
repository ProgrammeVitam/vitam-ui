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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Contient la référence à l'objet signé (et son empreinte jusqu'à la fin de la phase de versement dans le SAE).
 * 
 * <p>Classe Java pour ReferencedObjectType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ReferencedObjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SignedObjectId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefIdType"/&gt;
 *         &lt;element name="SignedObjectDigest" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MessageDigestBinaryObjectType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferencedObjectType", propOrder = {
    "signedObjectId",
    "signedObjectDigest"
})
public class ReferencedObjectType {

    @XmlElement(name = "SignedObjectId", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object signedObjectId;
    @XmlElement(name = "SignedObjectDigest", required = true)
    protected MessageDigestBinaryObjectType signedObjectDigest;

    /**
     * Obtient la valeur de la propriété signedObjectId.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getSignedObjectId() {
        return signedObjectId;
    }

    /**
     * Définit la valeur de la propriété signedObjectId.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setSignedObjectId(Object value) {
        this.signedObjectId = value;
    }

    /**
     * Obtient la valeur de la propriété signedObjectDigest.
     * 
     * @return
     *     possible object is
     *     {@link MessageDigestBinaryObjectType }
     *     
     */
    public MessageDigestBinaryObjectType getSignedObjectDigest() {
        return signedObjectDigest;
    }

    /**
     * Définit la valeur de la propriété signedObjectDigest.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageDigestBinaryObjectType }
     *     
     */
    public void setSignedObjectDigest(MessageDigestBinaryObjectType value) {
        this.signedObjectDigest = value;
    }

}
