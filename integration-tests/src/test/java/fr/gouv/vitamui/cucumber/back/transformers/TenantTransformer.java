package fr.gouv.vitamui.cucumber.back.transformers;

import cucumber.api.Transformer;

public class TenantTransformer extends Transformer<Integer> {

    private static final String PARAM_MAIN = "principal";
    private static final String PARAM_SECOND = "secondaire";
    private static final String PARAM_MAIN_TENANT = "le tenant principal";
    private static final String PARAM_SECOND_TENANT = "le tenant secondaire";
    private static final String PARAM_OTHER_CUSTOMER = "de l'autre customer";

    public static Integer mainTenant;
    public static Integer secondTenant;

    @Override
    public Integer transform(String paramTenant) {
        switch (paramTenant) {
            case PARAM_MAIN_TENANT:
            case PARAM_MAIN:
                return mainTenant;
            case PARAM_SECOND_TENANT:
            case PARAM_SECOND:
            case PARAM_OTHER_CUSTOMER:
                return secondTenant;
            default:
                throw new IllegalArgumentException(
                        "Le paramètre " + paramTenant + " ne correspond pas à un tenant possible");
        }
    }

}
