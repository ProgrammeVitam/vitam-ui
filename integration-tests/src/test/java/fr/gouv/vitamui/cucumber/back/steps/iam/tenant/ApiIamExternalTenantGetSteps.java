package fr.gouv.vitamui.cucumber.back.steps.iam.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Tenants dans IAM admin : opération de récupération.
 *
 *
 */
public class ApiIamExternalTenantGetSteps extends CommonSteps {

    @When("^un utilisateur récupère tous les tenants$")
    public void un_utilisateur_récupère_tous_les_tenants() {
        try {
            tenantDtos = getTenantRestClient().getAll(getSystemTenantUserAdminContext());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne tous les tenants$")
    public void le_serveur_retourne_tous_les_tenants() {
        assertThat(tenantDtos).isNotEmpty();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_ALL_TENANTS récupère un tenant par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_ALL_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_ALL_TENANTS_récupère_un_tenant_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_ALL_TENANTS() {
        testContext.tenantDto = getTenantRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.SYSTEM_TENANT_ID, Optional.empty());
    }

    @Then("^le serveur retourne aucun tenant$")
    public void le_serveur_retourne_aucun_tenant() {
        assertThat(testContext.tenantDto).isNull();
    }

    @Then("^le serveur retourne le tenant avec cet identifiant$")
    public void le_serveur_retourne_le_tenant_avec_cet_identifiant() {
        assertThat(testContext.tenantDto.getId()).isEqualTo(TestConstants.SYSTEM_TENANT_ID);
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération d'un tenant par son identifiant$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_d_un_tenant_par_son_identifiant() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère un tenant par son identifiant$")
    public void cet_utilisateur_récupère_un_tenant_par_son_identifiant() {
        try {
            testContext.tenantDto = getTenantRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), TestConstants.SYSTEM_TENANT_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_TENANTS récupère un tenant par son identifiant dans un tenant auquel il est autorisé et identique au tenant récupéré en utilisant un certificat full access avec le rôle ROLE_GET_ALL_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_TENANTS_récupère_un_tenant_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_et_identique_au_tenant_récupéré_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_ALL_TENANTS() {
        testContext.tenantDto = getTenantRestClient().getOne(getContext(proofTenantIdentifier, tokenUserTestTenantSystem()),
                TestConstants.SYSTEM_TENANT_ID, Optional.empty());
    }

    @When("^un utilisateur avec le rôle ROLE_GET_TENANTS récupère un tenant par son identifiant dans un tenant auquel il est autorisé mais différent du tenant récupéré en utilisant un certificat full access avec le rôle ROLE_GET_ALL_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_TENANTS_récupère_un_tenant_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_mais_différent_du_tenant_récupéré_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_ALL_TENANTS() {
        try {
            testContext.tenantDto = getTenantRestClient().getOne(getContext(proofTenantIdentifier, tokenUserTestTenantSystem()),
                    TestConstants.CAS_TENANT_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^l'utilisateur n'a pas les accès suffisants pour récupérer ce tenant$")
    public void l_utilisateur_n_a_pas_les_accès_suffisants_pour_récupérer_ce_tenant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.ForbiddenException: Unable to access to the tenant " + TestConstants.CAS_TENANT_ID
                        + " : insufficient permissions.");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_TENANTS récupère tous les tenants par le nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_TENANTS_récupère_tous_les_tenants_par_le_nom_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_TENANTS() {
        final QueryDto criteria = QueryDto.criteria("name", TestConstants.CAS_TENANT_NAME, CriterionOperator.EQUALS);
        tenantDtos = getTenantRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne tous les tenants avec ce nom$")
    public void le_serveur_retourne_tous_les_tenants_avec_ce_nom() {
        assertThat(tenantDtos).isNotNull().isNotEmpty();
        assertThat(tenantDtos.size()).isEqualTo(1);
        assertThat(tenantDtos.get(0).getName()).isEqualTo(TestConstants.CAS_TENANT_NAME);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_TENANTS récupère tous les tenants par le nom dans un tenant auquel il n'est pas autorisé en utilisant un certificat full access avec le rôle ROLE_GET_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_TENANTS_récupère_tous_les_tenants_par_le_nom_dans_un_tenant_auquel_il_n_est_pas_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_TENANTS() {
        try {
            final QueryDto criteria = QueryDto.criteria("name", TestConstants.CAS_TENANT_NAME, CriterionOperator.EQUALS);
            tenantDtos = getTenantRestClient().getAll(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_ADMIN),
                    criteria.toOptionalJson(), Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
