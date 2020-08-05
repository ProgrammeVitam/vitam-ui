package fr.gouv.vitamui.cucumber.common.parametertypes;

import fr.gouv.vitamui.cucumber.front.utils.ApplicationEnum;
import lombok.Getter;

@Getter
public class ApplicationParameterType {
    
    private ApplicationEnum data;

    public ApplicationParameterType(final String data) {
        this.data = ApplicationEnum.valueOf(data.toUpperCase());
    }

}
