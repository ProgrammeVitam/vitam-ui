package fr.gouv.vitamui.cucumber.back.steps.iam.tenant;

import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Tenants dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamExternalTenantCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_TENANTS ajoute un nouveau tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_TENANTS_ajoute_un_nouveau_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_TENANTS() {
        testContext.level = "";
        testContext.tenantDto = getTenantRestClient(true, new Integer[] { proofTenantIdentifier },
                new String[] { ServicesData.ROLE_CREATE_TENANTS })
                        .create(getContext(proofTenantIdentifier,
                                tokenUserTest(new String[] { ServicesData.ROLE_CREATE_TENANTS },
                                        proofTenantIdentifier, TestConstants.SYSTEM_CUSTOMER_ID,
                                        testContext.level)),
                                createOwnerAndBuildTenant());
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_TENANTS ajoute un tenant avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_TENANTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_ROLE_CREATE_TENANTS_ajoute_un_tenant_avec_un_nom_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_TENANTS() {
        un_utilisateur_avec_le_rôle_ROLE_CREATE_TENANTS_ajoute_un_nouveau_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_TENANTS();
        try {
            // on essaie de recréer un tenant avec les mêmes informations
            final OwnerDto ownerDto = FactoryDto.buildDto(OwnerDto.class);
            final OwnerDto rownerDto = getOwnerRestClient().create(getSystemTenantUserAdminContext(), ownerDto);
            final TenantDto tenantDto = IamDtoBuilder.buildTenantDto(null, testContext.tenantDto.getName(), null,
                    rownerDto.getId(), SYSTEM_CUSTOMER_ID);
            getTenantRestClient(true, new Integer[] { proofTenantIdentifier },
                    new String[] { ServicesData.ROLE_CREATE_TENANTS })
                            .create(getContext(proofTenantIdentifier,
                                    tokenUserTest(new String[] { ServicesData.ROLE_CREATE_TENANTS },
                                            proofTenantIdentifier, TestConstants.SYSTEM_CUSTOMER_ID,
                                            testContext.level)),
                                    tenantDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du tenant à cause du nom existant$")
    public void le_serveur_refuse_la_création_du_tenant_à_cause_du_nom_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create tenant "
                        + testContext.tenantDto.getName() + ": a tenant with the name: "
                        + testContext.tenantDto.getName() + " already exists.");
    }

    @Then("^le serveur retourne le tenant créé$")
    public void le_serveur_retourne_le_tenant_créé() {
        assertThat(testContext.tenantDto).isNotNull();
    }

    @Then("^(.*) profils sont créés pour le tenant$")
    public void des_profils_sont_créés_pour_le_tenant(final Integer profilesNumber) {
        final Integer tenantIdentifier = testContext.tenantDto.getIdentifier();
        final Collection<ProfileDto> profiles = getProfileRestClient().getAll(
                getContext(tenantIdentifier,
                        tokenUserTest(new String[] { ServicesData.ROLE_GET_PROFILES }, tenantIdentifier,
                                TestConstants.SYSTEM_CUSTOMER_ID, testContext.level)),
                Optional.empty(), Optional.empty());
        assertThat(profiles).isNotNull().isNotEmpty();
        assertThat(profiles.size()).isEqualTo(profilesNumber);
    }

    @Given("^deux tenants et un rôle par défaut pour l'ajout d'un tenant$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_l_ajout_d_un_tenant() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur ajoute un nouveau tenant$")
    public void cet_utilisateur_ajoute_un_nouveau_tenant() {
        try {
            getTenantRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .create(getContext(testContext.tenantIHMContext, testContext.tokenUser),
                            createOwnerAndBuildTenant());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de création tenant est présente dans vitam$")
    public void une_trace_de_création_d_un_tenant_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.SYSTEM_CUSTOMER_ID, testContext.tenantDto.getIdentifier().toString(), "tenants",
                "EXT_VITAMUI_CREATE_TENANT");
    }

}
