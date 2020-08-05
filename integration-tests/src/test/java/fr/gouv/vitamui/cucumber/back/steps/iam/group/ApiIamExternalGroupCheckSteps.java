package fr.gouv.vitamui.cucumber.back.steps.iam.group;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_GET_GROUPS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Groups dans IAM admin : opérations de vérification.
 *
 *
 */
public class ApiIamExternalGroupCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un groupe par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_groupe_par_son_identifiant() {
        try {
            QueryDto criteria = QueryDto.criteria("id", TestConstants.ADMIN_GROUP_ID, CriterionOperator.EQUALS);
            testContext.bResponse = getGroupRestClient().checkExist(getSystemTenantUserAdminContext(),
                    criteria.toJson());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS vérifie l'existence d'un groupe par son nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_vérifie_l_existence_d_un_groupe_par_son_nom_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        QueryDto criteria = QueryDto.criteria("name", TestConstants.ADMIN_GROUP_NAME, CriterionOperator.EQUALS);

        testContext.bResponse = getGroupRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
    }


    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS sans le bon niveau vérifie l'existence d'un groupe par son nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_sans_le_bon_niveau_vérifie_l_existence_d_un_groupe_par_son_nom_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        QueryDto criteria = QueryDto.criteria("name", TestConstants.ADMIN_GROUP_NAME, CriterionOperator.EQUALS);

        testContext.bResponse = getGroupRestClient(true, new Integer[] { proofTenantIdentifier },
                new String[] { ROLE_GET_GROUPS }).checkExist(
                        getContext(proofTenantIdentifier, tokenUserTest(new String[] { ROLE_GET_GROUPS },
                                proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")),
                        criteria.toJson());
    }

    @Given("^deux tenants et un rôle par défaut pour la vérification de l'existence d'un groupe par son nom$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_vérification_de_l_existence_d_un_groupe_par_son_nom() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur vérifie l'existence d'un groupe par son nom$")
    public void cet_utilisateur_vérifie_l_existence_d_un_groupe_par_son_nom() {
        QueryDto criteria = QueryDto.criteria("name", TestConstants.ADMIN_GROUP_NAME, CriterionOperator.EQUALS);

        try {
            testContext.bResponse = getGroupRestClient(testContext.fullAccess, testContext.certificateTenants,
                    testContext.certificateRoles).checkExist(
                            getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toJson());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
