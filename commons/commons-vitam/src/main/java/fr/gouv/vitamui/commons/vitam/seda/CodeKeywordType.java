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
 * <p>Classe Java pour CodeKeywordType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="CodeKeywordType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="corpname"/&gt;
 *     &lt;enumeration value="famname"/&gt;
 *     &lt;enumeration value="geogname"/&gt;
 *     &lt;enumeration value="name"/&gt;
 *     &lt;enumeration value="occupation"/&gt;
 *     &lt;enumeration value="persname"/&gt;
 *     &lt;enumeration value="subject"/&gt;
 *     &lt;enumeration value="genreform"/&gt;
 *     &lt;enumeration value="function"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CodeKeywordType")
@XmlEnum
public enum CodeKeywordType {


    /**
     * Références : ead.corpname
     * 
     */
    @XmlEnumValue("corpname")
    CORPNAME("corpname"),

    /**
     * Nom de famille.
     * 
     */
    @XmlEnumValue("famname")
    FAMNAME("famname"),

    /**
     * Nom géographique.
     * 
     */
    @XmlEnumValue("geogname")
    GEOGNAME("geogname"),

    /**
     * Nom.
     * 
     */
    @XmlEnumValue("name")
    NAME("name"),

    /**
     * Fonction.
     * 
     */
    @XmlEnumValue("occupation")
    OCCUPATION("occupation"),

    /**
     * Nom de personne.
     * 
     */
    @XmlEnumValue("persname")
    PERSNAME("persname"),

    /**
     * Mot-matière.
     * 
     */
    @XmlEnumValue("subject")
    SUBJECT("subject"),

    /**
     * Type de document.
     * 
     */
    @XmlEnumValue("genreform")
    GENREFORM("genreform"),

    /**
     * Références : ead.function
     * 
     */
    @XmlEnumValue("function")
    FUNCTION("function");
    private final String value;

    CodeKeywordType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CodeKeywordType fromValue(String v) {
        for (CodeKeywordType c: CodeKeywordType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
