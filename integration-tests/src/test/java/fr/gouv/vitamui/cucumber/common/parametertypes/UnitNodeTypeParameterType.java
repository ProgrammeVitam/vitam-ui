package fr.gouv.vitamui.cucumber.common.parametertypes;

import fr.gouv.vitamui.commons.api.enums.UnitNodeType;
import lombok.Getter;

@Getter
public class UnitNodeTypeParameterType {

    public static final String PARAM_NODE = "noeud";
    public final static String PARAM_FINAL = "final";
    public static final String PARAM_SERIAL = "serial";
    public static final String PARAM_SOLIDARY = "document solidaire";
    
    private UnitNodeType data;

    public UnitNodeTypeParameterType(final String data) {
        switch (data) {
            case PARAM_NODE:
                this.data = UnitNodeType.NODE;
            case PARAM_FINAL:
                this.data = UnitNodeType.FINAL;
            case PARAM_SERIAL:
                this.data = UnitNodeType.SERIAL;
            case PARAM_SOLIDARY:
                this.data = UnitNodeType.SOLIDARY;
            default:
                throw new IllegalArgumentException(
                        "Le paramètre " + data + " ne correspond pas à un UnitNodeType existant");
        }
    }

}
