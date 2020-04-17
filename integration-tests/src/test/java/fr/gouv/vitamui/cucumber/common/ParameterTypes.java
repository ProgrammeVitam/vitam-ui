package fr.gouv.vitamui.cucumber.common;

import fr.gouv.vitamui.cucumber.common.parametertypes.ApplicationParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.RoleParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.RolesParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.TenantParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.UnitNodeTypeParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.UserParameterType;
import io.cucumber.java.ParameterType;

public class ParameterTypes {

    public static final String REGEX_LEVEL_OR_VOID = "(vide|(?:(?:[A-Z]|\\\\.)+))";

    @ParameterType("[a-z ]+")
    public UnitNodeTypeParameterType unitNodeType(final String data) {
        return new UnitNodeTypeParameterType(data);
    }

    @ParameterType(value = ".*")
    public ApplicationParameterType application(final String data) {
        return new ApplicationParameterType(data);
    }

    @ParameterType(value = "\\w+")
    public UserParameterType user(final String data) {
        return new UserParameterType(data);
    }

    @ParameterType("principal|secondaire")
    public TenantParameterType tenant(final String data) {
        return new TenantParameterType(data);
    }

    @ParameterType(value = "ROLE_.*")
    public RoleParameterType role(final String data) {
        return new RoleParameterType(data);
    }

    @ParameterType(value = "ROLE_.*")
    public RolesParameterType roles(final String data) {
        return new RolesParameterType(data);
    }

    @ParameterType(value = ".*")
    public LevelParameterType level(final String data) {
        return new LevelParameterType(data);
    }

}
