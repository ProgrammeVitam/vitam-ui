package fr.gouv.vitamui.cucumber.common.parametertypes;

import fr.gouv.vitamui.cucumber.common.context.TestContext;

public class TenantParameterType {

    private static final String PARAM_MAIN = "principal";
    private static final String PARAM_SECOND = "secondaire";
    private static final String PARAM_MAIN_TENANT = "le tenant principal";
    private static final String PARAM_SECOND_TENANT = "le tenant secondaire";
    private static final String PARAM_OTHER_CUSTOMER = "de l'autre customer";

    private String paramTenant;

    public TenantParameterType(final String paramTenant) {
        this.paramTenant = paramTenant;
    }

    public Integer getTenant(final TestContext testContext) {
        Integer chosenTenant = null;
        switch (paramTenant) {
            case PARAM_MAIN_TENANT:
            case PARAM_MAIN:
                chosenTenant = testContext.mainTenant;
                break;
            case PARAM_SECOND_TENANT:
            case PARAM_SECOND:
            case PARAM_OTHER_CUSTOMER:
                chosenTenant = testContext.otherTenant;
                break;
            default:
                throw new IllegalArgumentException(
                        "Le paramètre " + paramTenant + " ne correspond pas à un tenant possible");
        }
        return chosenTenant;
    }

}

