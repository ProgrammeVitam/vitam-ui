package fr.gouv.vitamui.cucumber.common.parametertypes;

import org.junit.Assert;
import org.junit.Test;

import lombok.Getter;

/**
 * Transformer for roles.
 *
 */
@Getter
public class RolesParameterType {
    
    private String[] data;

    public RolesParameterType(final String data) {
        final String[] parts = data.split("\\+");
        final int nb = parts.length;
        final String[] arrayRoles = new String[nb];
        for (int i = 0; i < nb; i++) {
            final String role = parts[i].trim();
            final RoleParameterType roleType = new RoleParameterType(role);
            arrayRoles[i] = roleType.getData();
        }
        this.data = arrayRoles;
    }


    @Test
    public void test() {
        final RolesParameterType roles = new RolesParameterType("ROLE_GET_USERS+ROLE_GET_GROUPS+ROLE_CREATE_PROFILES");
        final String[] results = roles.getData();
        Assert.assertEquals("ROLE_GET_USERS", results[0]);
        Assert.assertEquals("ROLE_GET_GROUPS", results[1]);
        Assert.assertEquals("ROLE_CREATE_PROFILES", results[2]);
    }

}
