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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour AcknowledgementType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AcknowledgementType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}MessageType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MessageReceivedIdentifier" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}IdentifierType"/&gt;
 *         &lt;element name="Sender" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationWithIdType"/&gt;
 *         &lt;element name="Receiver" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}OrganizationWithIdType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AcknowledgementType", propOrder = {
    "messageReceivedIdentifier",
    "sender",
    "receiver"
})
public class AcknowledgementType
    extends MessageType
{

    @XmlElement(name = "MessageReceivedIdentifier", required = true)
    protected IdentifierType messageReceivedIdentifier;
    @XmlElement(name = "Sender", required = true)
    protected OrganizationWithIdType sender;
    @XmlElement(name = "Receiver", required = true)
    protected OrganizationWithIdType receiver;

    /**
     * Obtient la valeur de la propriété messageReceivedIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierType }
     *     
     */
    public IdentifierType getMessageReceivedIdentifier() {
        return messageReceivedIdentifier;
    }

    /**
     * Définit la valeur de la propriété messageReceivedIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierType }
     *     
     */
    public void setMessageReceivedIdentifier(IdentifierType value) {
        this.messageReceivedIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété sender.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public OrganizationWithIdType getSender() {
        return sender;
    }

    /**
     * Définit la valeur de la propriété sender.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public void setSender(OrganizationWithIdType value) {
        this.sender = value;
    }

    /**
     * Obtient la valeur de la propriété receiver.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public OrganizationWithIdType getReceiver() {
        return receiver;
    }

    /**
     * Définit la valeur de la propriété receiver.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationWithIdType }
     *     
     */
    public void setReceiver(OrganizationWithIdType value) {
        this.receiver = value;
    }

}
