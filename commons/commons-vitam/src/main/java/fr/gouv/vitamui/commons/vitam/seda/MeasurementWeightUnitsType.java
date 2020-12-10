//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour MeasurementWeightUnitsType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="MeasurementWeightUnitsType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="microgram"/&gt;
 *     &lt;enumeration value="MC"/&gt;
 *     &lt;enumeration value="milligram"/&gt;
 *     &lt;enumeration value="MGM"/&gt;
 *     &lt;enumeration value="gram"/&gt;
 *     &lt;enumeration value="GRM"/&gt;
 *     &lt;enumeration value="kilogram"/&gt;
 *     &lt;enumeration value="KGM"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "MeasurementWeightUnitsType")
@XmlEnum
public enum MeasurementWeightUnitsType {

    @XmlEnumValue("microgram")
    MICROGRAM("microgram"),
    MC("MC"),
    @XmlEnumValue("milligram")
    MILLIGRAM("milligram"),
    MGM("MGM"),
    @XmlEnumValue("gram")
    GRAM("gram"),
    GRM("GRM"),
    @XmlEnumValue("kilogram")
    KILOGRAM("kilogram"),
    KGM("KGM");
    private final String value;

    MeasurementWeightUnitsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MeasurementWeightUnitsType fromValue(String v) {
        for (MeasurementWeightUnitsType c: MeasurementWeightUnitsType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
