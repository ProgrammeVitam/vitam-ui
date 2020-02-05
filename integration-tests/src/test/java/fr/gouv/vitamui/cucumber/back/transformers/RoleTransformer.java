package fr.gouv.vitamui.cucumber.back.transformers;

import java.util.Arrays;

import cucumber.api.Transformer;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;

public class RoleTransformer extends Transformer<String> {

	@Override
	public String transform(String role) {
		if (ServicesData.checkIfRoleExists(Arrays.asList(new Role(role)))) {
		    return role;
		} else {
			throw new IllegalArgumentException("Le paramètre " + role + " ne correspond pas à un Rôle existant");
		}
	}
}
