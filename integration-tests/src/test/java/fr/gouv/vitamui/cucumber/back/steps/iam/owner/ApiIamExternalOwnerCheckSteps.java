package fr.gouv.vitamui.cucumber.back.steps.iam.owner;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Owners dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamExternalOwnerCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un propriétaire par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_propriétaire_par_son_identifiant() {
        try {
            final String criteria = QueryDto.criteria("id", TestConstants.SYSTEM_OWNER_ID, CriterionOperator.EQUALS).toJson();
            testContext.bResponse = getOwnerRestClient().checkExist(getSystemTenantUserAdminContext(), criteria);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_OWNERS vérifie l'existence d'un propriétaire par son code dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_OWNERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_OWNERS_vérifie_l_existence_d_un_propriétaire_par_son_code_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_OWNERS() {
        final String criteria = QueryDto.criteria("code", TestConstants.SYSTEM_OWNER_CODE, CriterionOperator.EQUALS).toJson();
        testContext.bResponse = getOwnerRestClient().checkExist(getSystemTenantUserAdminContext(), criteria);
    }

    @Given("^deux tenants et un rôle par défaut pour la vérification de l'existence d'un propriétaire par son code$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_vérification_de_l_existence_d_un_propriétaire_par_son_code() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur vérifie l'existence d'un propriétaire par son code$")
    public void cet_utilisateur_vérifie_l_existence_d_un_propriétaire_par_son_code() {
        final String criteria = QueryDto.criteria("code", TestConstants.SYSTEM_OWNER_CODE, CriterionOperator.EQUALS).toJson();

        try {
            getOwnerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .checkExist(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
