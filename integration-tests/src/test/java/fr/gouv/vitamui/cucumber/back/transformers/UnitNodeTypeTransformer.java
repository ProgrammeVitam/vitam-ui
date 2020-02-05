package fr.gouv.vitamui.cucumber.back.transformers;

import cucumber.api.Transformer;
import fr.gouv.vitamui.commons.api.enums.UnitNodeType;

public class UnitNodeTypeTransformer extends Transformer<UnitNodeType> {

    public static final String PARAM_NODE = "noeud";
    public final static String PARAM_FINAL = "final";
    public static final String PARAM_SERIAL = "serial";
    public static final String PARAM_SOLIDARY = "document solidaire";

    @Override
    public UnitNodeType transform(String type) {
        switch (type) {
            case PARAM_NODE:
                return UnitNodeType.NODE;
            case PARAM_FINAL:
                return UnitNodeType.FINAL;
            case PARAM_SERIAL:
                return UnitNodeType.SERIAL;
            case PARAM_SOLIDARY:
                return UnitNodeType.SOLIDARY;
            default:
                throw new IllegalArgumentException(
                        "Le paramètre " + type + " ne correspond pas à un UnitNodeType existant");
        }
    }

}
