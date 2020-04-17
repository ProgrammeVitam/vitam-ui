package fr.gouv.vitamui.cucumber.common.parametertypes;

import fr.gouv.vitamui.cucumber.front.utils.UserEnum;
import lombok.Getter;

@Getter
public class UserParameterType {
    
    private UserEnum data;

    public UserParameterType(final String data) {
        this.data = UserEnum.valueOf(data.toUpperCase());
    }

}
