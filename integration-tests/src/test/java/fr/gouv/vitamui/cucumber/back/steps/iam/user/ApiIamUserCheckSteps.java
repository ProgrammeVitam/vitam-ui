package fr.gouv.vitamui.cucumber.back.steps.iam.user;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_GET_USERS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Users dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamUserCheckSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_GET_USERS vérifie l'existence d'un utilisateur par son email dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_vérifie_l_existence_d_un_utilisateur_par_son_email_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        final QueryDto criteria = QueryDto.criteria("email", TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                CriterionOperator.EQUALS);

        testContext.bResponse = getUserRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
    }

    @When("^un utilisateur avec le rôle ROLE_GET_USERS sans le bon niveau vérifie l'existence d'un utilisateur par son email dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_sans_le_bon_niveau_vérifie_l_existence_d_un_utilisateur_par_son_email_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        final QueryDto criteria = QueryDto.criteria("email", TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                CriterionOperator.EQUALS);

        testContext.bResponse = getUserRestClient(true, new Integer[] { proofTenantIdentifier }, new String[] { ROLE_GET_USERS })
                .checkExist(getContext(proofTenantIdentifier,
                        tokenUserTest(new String[] { ROLE_GET_USERS }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")), criteria.toJson());
    }

    @Given("^deux tenants et un rôle par défaut pour la vérification de l'existence d'un utilisateur par son email$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_vérification_de_l_existence_d_un_utilisateur_par_son_email() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur vérifie l'existence d'un utilisateur par son email$")
    public void cet_utilisateur_vérifie_l_existence_d_un_utilisateur_par_son_email() {
        final QueryDto criteria = QueryDto.criteria("email", TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                CriterionOperator.EQUALS);

        try {
            testContext.bResponse = getUserRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .checkExist(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
