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
 * <p>Classe Java pour FinalActionAppraisalCodeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="FinalActionAppraisalCodeType"&gt;
 *   &lt;restriction base="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"&gt;
 *     &lt;enumeration value="Keep"/&gt;
 *     &lt;enumeration value="Destroy"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "FinalActionAppraisalCodeType")
@XmlEnum
public enum FinalActionAppraisalCodeType {

    @XmlEnumValue("Keep")
    KEEP("Keep"),
    @XmlEnumValue("Destroy")
    DESTROY("Destroy");
    private final String value;

    FinalActionAppraisalCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FinalActionAppraisalCodeType fromValue(String v) {
        for (FinalActionAppraisalCodeType c: FinalActionAppraisalCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
