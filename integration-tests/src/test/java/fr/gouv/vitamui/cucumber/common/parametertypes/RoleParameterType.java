package fr.gouv.vitamui.cucumber.common.parametertypes;

import java.util.Arrays;

import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import lombok.Getter;

@Getter
public class RoleParameterType {
    
    private String data;

    public RoleParameterType(final String data) {
        if (ServicesData.checkIfRoleExists(Arrays.asList(new Role(data)))) {
            this.data = data;
        } else {
            throw new IllegalArgumentException("Le paramètre " + data + " ne correspond pas à un Rôle existant");
        }
    }

}
