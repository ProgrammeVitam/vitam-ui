package fr.gouv.vitamui.cucumber.back.transformers;

import cucumber.api.Transformer;

public class LevelTransformer extends Transformer<String> {

    private static final String PARAM_VIDE = "vide";
    private static final String REGEX_LEVEL = "^([A-Z]\\.*)+$";
    public static final String REGEX_LEVEL_OR_VOID = "(vide|(?:(?:[A-Z]|\\.)+))";

    @Override
    public String transform(String level) {
        if (PARAM_VIDE.equals(level)) {
            return "";
        } else if (level.matches(REGEX_LEVEL)) {
            return level;
        }
        throw new IllegalArgumentException("Le paramètre " + level + " ne correspond pas à un niveau possible");
    }

}
