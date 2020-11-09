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
 * <p>Classe Java pour EventLogBookOgType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="EventLogBookOgType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}EventType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DataObjectReferenceId" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefIdType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventLogBookOgType", propOrder = {
    "dataObjectReferenceId"
})
public class EventLogBookOgType
    extends EventType
{

    @XmlElement(name = "DataObjectReferenceId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object dataObjectReferenceId;

    /**
     * Obtient la valeur de la propriété dataObjectReferenceId.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDataObjectReferenceId() {
        return dataObjectReferenceId;
    }

    /**
     * Définit la valeur de la propriété dataObjectReferenceId.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDataObjectReferenceId(Object value) {
        this.dataObjectReferenceId = value;
    }

}
