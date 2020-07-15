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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour BusinessAuthorizationRequestMessageType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="BusinessAuthorizationRequestMessageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}BusinessRequestMessageType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AuthorizationRequestContent" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}AuthorizationRequestContentType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessAuthorizationRequestMessageType", propOrder = {
    "authorizationRequestContent"
})
@XmlSeeAlso({
    AuthorizationControlAuthorityRequestType.class,
    AuthorizationOriginatingAgencyRequestType.class
})
public abstract class BusinessAuthorizationRequestMessageType
    extends BusinessRequestMessageType
{

    @XmlElement(name = "AuthorizationRequestContent", required = true)
    protected AuthorizationRequestContentType authorizationRequestContent;

    /**
     * Obtient la valeur de la propriété authorizationRequestContent.
     * 
     * @return
     *     possible object is
     *     {@link AuthorizationRequestContentType }
     *     
     */
    public AuthorizationRequestContentType getAuthorizationRequestContent() {
        return authorizationRequestContent;
    }

    /**
     * Définit la valeur de la propriété authorizationRequestContent.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorizationRequestContentType }
     *     
     */
    public void setAuthorizationRequestContent(AuthorizationRequestContentType value) {
        this.authorizationRequestContent = value;
    }

}
