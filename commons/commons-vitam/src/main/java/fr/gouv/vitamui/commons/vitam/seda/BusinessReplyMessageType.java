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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour BusinessReplyMessageType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="BusinessReplyMessageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}BusinessMessageType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ReplyCode" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType" minOccurs="0"/&gt;
 *         &lt;element name="Operation" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OperationType" minOccurs="0"/&gt;
 *         &lt;element name="MessageRequestIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessReplyMessageType", propOrder = {
    "replyCode",
    "operation",
    "messageRequestIdentifier"
})
@XmlSeeAlso({
    ArchiveDeliveryRequestReplyType.class,
    ArchiveRestitutionRequestReplyType.class,
    ArchiveTransferReplyType.class,
    ArchiveTransferRequestReplyType.class,
    BusinessAuthorizationRequestReplyMessageType.class
})
public abstract class BusinessReplyMessageType
    extends BusinessMessageType
{

    @XmlElement(name = "ReplyCode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String replyCode;
    @XmlElement(name = "Operation")
    protected OperationType operation;
    @XmlElement(name = "MessageRequestIdentifier", required = true)
    protected IdentifierType messageRequestIdentifier;

    /**
     * Obtient la valeur de la propriété replyCode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyCode() {
        return replyCode;
    }

    /**
     * Définit la valeur de la propriété replyCode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyCode(String value) {
        this.replyCode = value;
    }

    /**
     * Obtient la valeur de la propriété operation.
     * 
     * @return
     *     possible object is
     *     {@link OperationType }
     *     
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * Définit la valeur de la propriété operation.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationType }
     *     
     */
    public void setOperation(OperationType value) {
        this.operation = value;
    }

    /**
     * Obtient la valeur de la propriété messageRequestIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getMessageRequestIdentifier() {
        return messageRequestIdentifier;
    }

    /**
     * Définit la valeur de la propriété messageRequestIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setMessageRequestIdentifier(IdentifierType value) {
        this.messageRequestIdentifier = value;
    }

}
