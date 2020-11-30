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
 * <p>Classe Java pour FinalActionStorageCodeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="FinalActionStorageCodeType"&gt;
 *   &lt;restriction base="{fr:gouv:culture:archivesdefrance:seda:v2.1}NonEmptyTokenType"&gt;
 *     &lt;enumeration value="RestrictAccess"/&gt;
 *     &lt;enumeration value="Transfer"/&gt;
 *     &lt;enumeration value="Copy"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "FinalActionStorageCodeType")
@XmlEnum
public enum FinalActionStorageCodeType {

    @XmlEnumValue("RestrictAccess")
    RESTRICT_ACCESS("RestrictAccess"),
    @XmlEnumValue("Transfer")
    TRANSFER("Transfer"),
    @XmlEnumValue("Copy")
    COPY("Copy");
    private final String value;

    FinalActionStorageCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FinalActionStorageCodeType fromValue(String v) {
        for (FinalActionStorageCodeType c: FinalActionStorageCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
