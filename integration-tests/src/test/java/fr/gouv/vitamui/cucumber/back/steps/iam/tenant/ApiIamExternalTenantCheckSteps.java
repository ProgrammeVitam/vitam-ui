package fr.gouv.vitamui.cucumber.back.steps.iam.tenant;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Tenants dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamExternalTenantCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un tenant par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_tenant_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", TestConstants.SYSTEM_TENANT_ID, CriterionOperator.EQUALS);

            getTenantRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^cet utilisateur vérifie l'existence d'un tenant par son identifiant$")
    public void cet_utilisateur_vérifie_l_existence_d_un_tenant_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", TestConstants.SYSTEM_TENANT_ID, CriterionOperator.EQUALS);

            getTenantRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .checkExist(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

}
