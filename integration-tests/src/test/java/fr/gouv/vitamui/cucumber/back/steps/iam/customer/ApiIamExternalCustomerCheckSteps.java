package fr.gouv.vitamui.cucumber.back.steps.iam.customer;

import java.util.Arrays;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Customers dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamExternalCustomerCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un client par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_client_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", TestConstants.SYSTEM_CUSTOMER_ID, CriterionOperator.EQUALS);
            testContext.bResponse = getCustomerRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_CUSTOMERS vérifie l'existence d'un client par son code et domain dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_vérifie_l_existence_d_un_client_par_son_code_et_domain_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        final QueryDto criteria = QueryDto.criteria("emailDomains", Arrays.asList(defaultEmailDomain), CriterionOperator.IN).addCriterion("code",
                TestConstants.SYSTEM_CUSTOMER_CODE, CriterionOperator.EQUALS);

        testContext.bResponse = getCustomerRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
    }

    @Given("^deux tenants et un rôle par défaut pour la vérification de l'existence d'un client par son code et domain$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_vérification_de_l_existence_d_un_client_par_son_code_et_domain() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur vérifie l'existence d'un client par son code et domain$")
    public void cet_utilisateur_vérifie_l_existence_d_un_client_par_son_code_et_domain() {
        try {
            final QueryDto criteria = QueryDto.criteria("emailDomains", Arrays.asList(defaultEmailDomain), CriterionOperator.EQUALS).addCriterion("code",
                    TestConstants.SYSTEM_CUSTOMER_CODE, CriterionOperator.EQUALS);

            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .checkExist(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
