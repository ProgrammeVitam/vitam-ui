package fr.gouv.vitamui.cucumber.back.steps.iam.provider;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.utils.FactoryDto;

/**
 * Teste l'API Identity providers dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamExternalIdentityPoviderCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROVIDERS ajoute un nouveau provider dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROVIDERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROVIDERS_ajoute_un_nouveau_provider_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROVIDERS() {
        testContext.identityProviderDto = FactoryDto.buildDto(IdentityProviderDto.class);
        testContext.identityProviderDto = getIdentityProviderRestClient().create(getSystemTenantUserAdminContext(),
                testContext.identityProviderDto);
    }

    @Then("^le serveur retourne le provider créé$")
    public void le_serveur_retourne_le_provider_créé() {
        assertThat(testContext.identityProviderDto).isNotNull();
    }

    @Given("^deux tenants et un rôle par défaut pour la création d'un provider$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_création_d_un_provider() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur ajoute un nouveau provider$")
    public void cet_utilisateur_ajoute_un_nouveau_provider() {
        try {
            getIdentityProviderRestClient(testContext.fullAccess, testContext.certificateTenants,
                    testContext.certificateRoles).create(
                            getContext(testContext.tenantIHMContext, testContext.tokenUser),
                            FactoryDto.buildDto(IdentityProviderDto.class));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de création du provider est présente dans vitam$")
    public void une_trace_de_création_du_provider_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(testContext.identityProviderDto.getCustomerId(),
                testContext.identityProviderDto.getIdentifier(), "providers", "EXT_VITAMUI_CREATE_IDP");
    }
}
