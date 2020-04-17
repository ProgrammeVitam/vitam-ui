package fr.gouv.vitamui.cucumber.common.parametertypes;

import lombok.Getter;

@Getter
public class LevelParameterType {

    private static final String PARAM_VIDE = "vide";

    private static final String REGEX_LEVEL = "^([A-Z]\\.*)+$";

    private String data;

    public LevelParameterType(final String data) {
        if (PARAM_VIDE.equals(data)) {
            this.data = "";
        }
        else if (data.matches(REGEX_LEVEL)) {
            this.data = data;
        } else {
            throw new IllegalArgumentException("Le paramètre " + data + " ne correspond pas à un niveau possible");
        }
    }

}
