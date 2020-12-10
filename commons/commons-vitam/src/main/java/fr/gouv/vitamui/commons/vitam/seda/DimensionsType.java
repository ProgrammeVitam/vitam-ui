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
 * Permet d'exprimer les mesures de dimensions basiques.
 * 
 * <p>Classe Java pour DimensionsType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DimensionsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{fr:gouv:culture:archivesdefrance:seda:v2.1}BaseDimensionsType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Width" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementType" minOccurs="0"/&gt;
 *         &lt;element name="Height" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementType" minOccurs="0"/&gt;
 *         &lt;element name="Depth" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementType" minOccurs="0"/&gt;
 *         &lt;element name="Shape" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Diameter" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementType" minOccurs="0"/&gt;
 *         &lt;element name="Length" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementType" minOccurs="0"/&gt;
 *         &lt;element name="Thickness" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementType" minOccurs="0"/&gt;
 *         &lt;element name="Weight" type="{fr:gouv:culture:archivesdefrance:seda:v2.1}MeasurementWeightType" minOccurs="0"/&gt;
 *         &lt;element name="NumberOfPage" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DimensionsType", propOrder = {
    "width",
    "height",
    "depth",
    "shape",
    "diameter",
    "length",
    "thickness",
    "weight",
    "numberOfPage"
})
public class DimensionsType
    extends BaseDimensionsType
{

    @XmlElement(name = "Width")
    protected MeasurementType width;
    @XmlElement(name = "Height")
    protected MeasurementType height;
    @XmlElement(name = "Depth")
    protected MeasurementType depth;
    @XmlElement(name = "Shape")
    protected String shape;
    @XmlElement(name = "Diameter")
    protected MeasurementType diameter;
    @XmlElement(name = "Length")
    protected MeasurementType length;
    @XmlElement(name = "Thickness")
    protected MeasurementType thickness;
    @XmlElement(name = "Weight")
    protected MeasurementWeightType weight;
    @XmlElement(name = "NumberOfPage")
    protected Integer numberOfPage;

    /**
     * Obtient la valeur de la propriété width.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementType }
     *     
     */
    public MeasurementType getWidth() {
        return width;
    }

    /**
     * Définit la valeur de la propriété width.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementType }
     *     
     */
    public void setWidth(MeasurementType value) {
        this.width = value;
    }

    /**
     * Obtient la valeur de la propriété height.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementType }
     *     
     */
    public MeasurementType getHeight() {
        return height;
    }

    /**
     * Définit la valeur de la propriété height.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementType }
     *     
     */
    public void setHeight(MeasurementType value) {
        this.height = value;
    }

    /**
     * Obtient la valeur de la propriété depth.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementType }
     *     
     */
    public MeasurementType getDepth() {
        return depth;
    }

    /**
     * Définit la valeur de la propriété depth.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementType }
     *     
     */
    public void setDepth(MeasurementType value) {
        this.depth = value;
    }

    /**
     * Obtient la valeur de la propriété shape.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShape() {
        return shape;
    }

    /**
     * Définit la valeur de la propriété shape.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShape(String value) {
        this.shape = value;
    }

    /**
     * Obtient la valeur de la propriété diameter.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementType }
     *     
     */
    public MeasurementType getDiameter() {
        return diameter;
    }

    /**
     * Définit la valeur de la propriété diameter.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementType }
     *     
     */
    public void setDiameter(MeasurementType value) {
        this.diameter = value;
    }

    /**
     * Obtient la valeur de la propriété length.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementType }
     *     
     */
    public MeasurementType getLength() {
        return length;
    }

    /**
     * Définit la valeur de la propriété length.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementType }
     *     
     */
    public void setLength(MeasurementType value) {
        this.length = value;
    }

    /**
     * Obtient la valeur de la propriété thickness.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementType }
     *     
     */
    public MeasurementType getThickness() {
        return thickness;
    }

    /**
     * Définit la valeur de la propriété thickness.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementType }
     *     
     */
    public void setThickness(MeasurementType value) {
        this.thickness = value;
    }

    /**
     * Obtient la valeur de la propriété weight.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementWeightType }
     *     
     */
    public MeasurementWeightType getWeight() {
        return weight;
    }

    /**
     * Définit la valeur de la propriété weight.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementWeightType }
     *     
     */
    public void setWeight(MeasurementWeightType value) {
        this.weight = value;
    }

    /**
     * Obtient la valeur de la propriété numberOfPage.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfPage() {
        return numberOfPage;
    }

    /**
     * Définit la valeur de la propriété numberOfPage.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfPage(Integer value) {
        this.numberOfPage = value;
    }

}
