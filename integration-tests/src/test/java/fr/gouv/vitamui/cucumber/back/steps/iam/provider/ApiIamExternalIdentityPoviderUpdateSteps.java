package fr.gouv.vitamui.cucumber.back.steps.iam.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Identity providers dans IAM admin : opérations de mise à jour
 *
 *
 */
public class ApiIamExternalIdentityPoviderUpdateSteps extends CommonSteps {

    @Given("^un provider en readonly a été créé$")
    public void un_provider_en_readonly_a_été_créé() {
        testContext.identityProviderDto = FactoryDto.buildDto(IdentityProviderDto.class);
        testContext.identityProviderDto.setReadonly(true);
        testContext.savedIdentityProviderDto = getIdentityProviderRestClient().create(getSystemTenantUserAdminContext(),
                testContext.identityProviderDto);
    }

    private IdentityProviderDto buildProviderToUpdate() {
        final IdentityProviderDto dto = FactoryDto.buildDto(IdentityProviderDto.class);
        dto.setId(testContext.savedIdentityProviderDto.getId());
        dto.setTechnicalName(testContext.savedIdentityProviderDto.getTechnicalName());
        dto.setName(TestConstants.UPDATED + testContext.savedIdentityProviderDto.getName());
        return dto;
    }

    @Then("^le serveur retourne le provider mis à jour$")
    public void le_serveur_retourne_le_provider_mis_à_jour() {
        assertThat(testContext.identityProviderDto.getName())
                .isEqualTo(TestConstants.UPDATED + testContext.savedIdentityProviderDto.getName());
    }

    @When("^un utilisateur met à jour le provider$")
    public void un_utilisateur_met_à_jour_le_provider() {
        try {
            testContext.identityProviderDto = getIdentityProviderRestClient().update(getSystemTenantUserAdminContext(),
                    buildProviderToUpdate());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour du provider$")
    public void le_serveur_refuse_la_mise_à_jour_du_provider() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: The identity provider "
                        + testContext.identityProviderDto.getName() + " can't be updated.");
    }

    private Map<String, Object> buildProviderToPatch() {
        final Map<String, Object> dto = new HashMap<>();
        dto.put("id", testContext.savedIdentityProviderDto.getId());
        dto.put("name", TestConstants.UPDATED + testContext.savedIdentityProviderDto.getName());
        return dto;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROVIDERS met à jour partiellement un provider dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROVIDERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROVIDERS_met_à_jour_partiellement_un_provider_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROVIDERS() {
        testContext.identityProviderDto = getIdentityProviderRestClient().patch(getSystemTenantUserAdminContext(),
                buildProviderToPatch());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_PROVIDERS met à jour partiellement un provider en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROVIDERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_PROVIDERS_met_à_jour_partiellement_un_provider_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_PROVIDERS() {
        try {
            getIdentityProviderRestClient().patch(getSystemTenantUserAdminContext(), buildProviderToPatch());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour partielle d'un provider$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_partielle_d_un_provider() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur met à jour partiellement un provider$")
    public void cet_utilisateur_met_à_jour_partiellement_un_provider() {
        try {
            getIdentityProviderRestClient(testContext.fullAccess, testContext.certificateTenants,
                    testContext.certificateRoles).patch(getContext(testContext.tenantIHMContext, testContext.tokenUser),
                            buildProviderToPatch());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de mise à jour du provider est présente dans vitam$")
    public void une_trace_de_mise_à_jour_du_propriétaire_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(testContext.identityProviderDto.getCustomerId(),
                testContext.identityProviderDto.getIdentifier(), "providers", "EXT_VITAMUI_UPDATE_IDP");
    }
}
