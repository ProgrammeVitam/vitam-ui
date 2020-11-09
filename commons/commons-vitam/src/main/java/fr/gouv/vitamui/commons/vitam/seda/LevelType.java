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
 * <p>Classe Java pour LevelType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="LevelType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="Fonds"/&gt;
 *     &lt;enumeration value="Subfonds"/&gt;
 *     &lt;enumeration value="Class"/&gt;
 *     &lt;enumeration value="Collection"/&gt;
 *     &lt;enumeration value="Series"/&gt;
 *     &lt;enumeration value="Subseries"/&gt;
 *     &lt;enumeration value="RecordGrp"/&gt;
 *     &lt;enumeration value="SubGrp"/&gt;
 *     &lt;enumeration value="File"/&gt;
 *     &lt;enumeration value="Item"/&gt;
 *     &lt;enumeration value="OtherLevel"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "LevelType")
@XmlEnum
public enum LevelType {

    @XmlEnumValue("Fonds")
    FONDS("Fonds"),
    @XmlEnumValue("Subfonds")
    SUBFONDS("Subfonds"),
    @XmlEnumValue("Class")
    CLASS("Class"),
    @XmlEnumValue("Collection")
    COLLECTION("Collection"),
    @XmlEnumValue("Series")
    SERIES("Series"),
    @XmlEnumValue("Subseries")
    SUBSERIES("Subseries"),
    @XmlEnumValue("RecordGrp")
    RECORD_GRP("RecordGrp"),
    @XmlEnumValue("SubGrp")
    SUB_GRP("SubGrp"),
    @XmlEnumValue("File")
    FILE("File"),
    @XmlEnumValue("Item")
    ITEM("Item"),
    @XmlEnumValue("OtherLevel")
    OTHER_LEVEL("OtherLevel");
    private final String value;

    LevelType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LevelType fromValue(String v) {
        for (LevelType c: LevelType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
