package fr.gouv.vitamui.cucumber.back.steps.iam.owner;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Owners dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamExternalOwnerCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_OWNERS ajoute un nouveau propriétaire dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_OWNERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_OWNERS_ajoute_un_nouveau_propriétaire_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_OWNERS() {
        testContext.ownerDto = getOwnerRestClient().create(getSystemTenantUserAdminContext(),
                FactoryDto.buildDto(OwnerDto.class));
    }

    @Then("^le serveur retourne le propriétaire créé$")
    public void le_serveur_retourne_le_propriétaire_créé() {
        assertThat(testContext.ownerDto).isNotNull();
    }

    @Given("^deux tenants et un rôle par défaut pour la création d'un propriétaire$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_création_d_un_propriétaire() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur ajoute un nouveau propriétaire$")
    public void cet_utilisateur_ajoute_un_nouveau_propriétaire() {
        try {
            getOwnerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .create(getContext(testContext.tenantIHMContext, testContext.tokenUser),
                            FactoryDto.buildDto(OwnerDto.class));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de création d'un propriétaire est présente dans vitam$")
    public void une_trace_de_création_d_un_propriétaire_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.SYSTEM_CUSTOMER_ID, testContext.ownerDto.getIdentifier(), "owners",
                "EXT_VITAMUI_CREATE_OWNER");
    }

}
