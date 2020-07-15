//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Mots-clés.
 * 
 * <p>Classe Java pour KeywordsType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="KeywordsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="KeywordContent" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}TextType"/&gt;
 *         &lt;element name="KeywordReference" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType" minOccurs="0"/&gt;
 *         &lt;element name="KeywordType" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}KeyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeywordsType", propOrder = {
    "keywordContent",
    "keywordReference",
    "keywordType"
})
public class KeywordsType {

    @XmlElement(name = "KeywordContent", required = true)
    protected TextType keywordContent;
    @XmlElement(name = "KeywordReference")
    protected IdentifierType keywordReference;
    @XmlElement(name = "KeywordType")
    protected KeyType keywordType;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Obtient la valeur de la propriété keywordContent.
     * 
     * @return
     *     possible object is
     *     {@link TextType }
     *     
     */
    public TextType getKeywordContent() {
        return keywordContent;
    }

    /**
     * Définit la valeur de la propriété keywordContent.
     * 
     * @param value
     *     allowed object is
     *     {@link TextType }
     *     
     */
    public void setKeywordContent(TextType value) {
        this.keywordContent = value;
    }

    /**
     * Obtient la valeur de la propriété keywordReference.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getKeywordReference() {
        return keywordReference;
    }

    /**
     * Définit la valeur de la propriété keywordReference.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setKeywordReference(IdentifierType value) {
        this.keywordReference = value;
    }

    /**
     * Obtient la valeur de la propriété keywordType.
     * 
     * @return
     *     possible object is
     *     {@link KeyType }
     *     
     */
    public KeyType getKeywordType() {
        return keywordType;
    }

    /**
     * Définit la valeur de la propriété keywordType.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyType }
     *     
     */
    public void setKeywordType(KeyType value) {
        this.keywordType = value;
    }

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
