package fr.gouv.vitamui.cucumber.back.steps.iam.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Tenants dans IAM admin : opérations de mise à jour.
 *
 *
 */
public class ApiIamExternalTenantUpdateSteps extends CommonSteps {

    @Given("^un tenant a été créé$")
    public void un_tenant_a_été_créé() {
        testContext.tenantDto = createOwnerAndBuildTenant();
        testContext.savedTenantDto = getTenantRestClient().create(getSystemTenantUserAdminContext(),
                testContext.tenantDto);
    }

    @Then("^le serveur refuse la mise à jour du tenant$")
    public void le_serveur_refuse_la_mise_à_jour_du_tenant() {
        assertThat(testContext.exception)
                .overridingErrorMessage("Le serveur n'a pas refusé l'appel : aucune exception n'a été levée")
                .isNotNull();
        assertThat(testContext.exception.toString())
                .contains("Unable to update tenant " + testContext.savedTenantDto.getId());
    }

    private TenantDto buildTenantToUpdate() {
        final TenantDto dto = FactoryDto.buildDto(TenantDto.class);
        dto.setId(testContext.savedTenantDto.getId());
        dto.setIdentifier(testContext.savedTenantDto.getIdentifier());
        dto.setOwnerId(testContext.savedTenantDto.getOwnerId());
        dto.setName(TestConstants.UPDATED + testContext.savedTenantDto.getName());
        return dto;
    }

    @Then("^le serveur retourne le tenant mis à jour$")
    public void le_serveur_retourne_le_tenant_mis_à_jour() {
        assertThat(testContext.tenantDto.getName())
                .isEqualTo(TestConstants.UPDATED + testContext.savedTenantDto.getName());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour un tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_TENANTS_met_à_jour_un_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_TENANTS() {
        testContext.tenantDto = getTenantRestClient().update(getSystemTenantUserAdminContext(), buildTenantToUpdate());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour un tenant en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_TENANTS_met_à_jour_un_tenant_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_TENANTS() {
        testContext.tenantDto = buildTenantToUpdate();
        testContext.tenantDto.setReadonly(true);
        try {
            testContext.tenantDto = getTenantRestClient().update(getSystemTenantUserAdminContext(),
                    testContext.tenantDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur sans le rôle ROLE_UPDATE_TENANTS met à jour un tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS$")
    public void un_utilisateur_sans_le_rôle_ROLE_UPDATE_TENANTS_met_à_jour_un_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_TENANTS() {
        try {
            getTenantRestClient().update(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                    buildTenantToUpdate());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour un tenant avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_TENANTS_met_à_jour_un_tenant_avec_un_nom_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROFILES() {
        testContext.savedTenantDto.setName("Tenant Système");
        try {
            testContext.tenantDto = getTenantRestClient().update(getSystemTenantUserAdminContext(),
                    testContext.savedTenantDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

}
