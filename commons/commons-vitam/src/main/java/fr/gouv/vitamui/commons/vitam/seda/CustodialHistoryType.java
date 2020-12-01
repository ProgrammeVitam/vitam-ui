//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour CustodialHistoryType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="CustodialHistoryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CustodialHistoryItem" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}CustodialHistoryItemType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="CustodialHistoryFile" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}DataObjectRefType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustodialHistoryType", propOrder = {
    "custodialHistoryItem",
    "custodialHistoryFile"
})
public class CustodialHistoryType {

    @XmlElement(name = "CustodialHistoryItem", required = true)
    protected List<CustodialHistoryItemType> custodialHistoryItem;
    @XmlElement(name = "CustodialHistoryFile")
    protected DataObjectRefType custodialHistoryFile;

    /**
     * Gets the value of the custodialHistoryItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the custodialHistoryItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustodialHistoryItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustodialHistoryItemType }
     * 
     * 
     */
    public List<CustodialHistoryItemType> getCustodialHistoryItem() {
        if (custodialHistoryItem == null) {
            custodialHistoryItem = new ArrayList<CustodialHistoryItemType>();
        }
        return this.custodialHistoryItem;
    }

    /**
     * Obtient la valeur de la propriété custodialHistoryFile.
     * 
     * @return
     *     possible object is
     *     {@link DataObjectRefType }
     *     
     */
    public DataObjectRefType getCustodialHistoryFile() {
        return custodialHistoryFile;
    }

    /**
     * Définit la valeur de la propriété custodialHistoryFile.
     * 
     * @param value
     *     allowed object is
     *     {@link DataObjectRefType }
     *     
     */
    public void setCustodialHistoryFile(DataObjectRefType value) {
        this.custodialHistoryFile = value;
    }

}
