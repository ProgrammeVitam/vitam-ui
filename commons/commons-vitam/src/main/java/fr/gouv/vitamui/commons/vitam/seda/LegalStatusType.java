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
 * <p>Classe Java pour LegalStatusType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="LegalStatusType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="Public Archive"/&gt;
 *     &lt;enumeration value="Private Archive"/&gt;
 *     &lt;enumeration value="Public and Private Archive"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "LegalStatusType")
@XmlEnum
public enum LegalStatusType {

    @XmlEnumValue("Public Archive")
    PUBLIC_ARCHIVE("Public Archive"),
    @XmlEnumValue("Private Archive")
    PRIVATE_ARCHIVE("Private Archive"),
    @XmlEnumValue("Public and Private Archive")
    PUBLIC_AND_PRIVATE_ARCHIVE("Public and Private Archive");
    private final String value;

    LegalStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LegalStatusType fromValue(String v) {
        for (LegalStatusType c: LegalStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
