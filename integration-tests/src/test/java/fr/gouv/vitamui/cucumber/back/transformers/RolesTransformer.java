package fr.gouv.vitamui.cucumber.back.transformers;

import org.junit.Assert;
import org.junit.Test;

import cucumber.api.Transformer;
/**
 * Transformer for roles.
 *
 *
 */
public class RolesTransformer extends Transformer<String[]> {

    private final RoleTransformer roleTransformer = new RoleTransformer();

	@Override
	public String[] transform(String roles) {
	    final String[] parts = roles.split("\\+");
	    final int nb = parts.length;
	    final String[] arrayRoles = new String[nb];
	    for (int i = 0; i < nb; i++) {
            final String role = parts[i].trim();
            arrayRoles[i] = roleTransformer.transform(role);
        }
        return arrayRoles;
	}


	@Test
	public void test() {
	    String[] results = transform("ROLE_GET_USERS+ROLE_GET_GROUPS+ROLE_CREATE_PROFILES");
	    Assert.assertEquals("ROLE_GET_USERS", results[0]);
	    Assert.assertEquals("ROLE_GET_GROUPS", results[1]);
	    Assert.assertEquals("ROLE_CREATE_PROFILES", results[2]);
	}
}
