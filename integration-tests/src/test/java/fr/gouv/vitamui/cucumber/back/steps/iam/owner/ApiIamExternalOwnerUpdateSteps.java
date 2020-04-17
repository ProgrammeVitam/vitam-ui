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
 * Teste l'API Owners dans IAM admin : opérations de mise à jour.
 *
 *
 */
public class ApiIamExternalOwnerUpdateSteps extends CommonSteps {

    @Given("^un propriétaire a été créé$")
    public void un_propriétaire_a_été_créé() {
        testContext.ownerDto = FactoryDto.buildDto(OwnerDto.class);
        testContext.savedOwnerDto = getOwnerRestClient().create(getSystemTenantUserAdminContext(),
                testContext.ownerDto);
    }

    @Given("^un propriétaire en readonly a été créé$")
    public void un_propriétaire_en_readonly_a_été_créé() {
        testContext.ownerDto = FactoryDto.buildDto(OwnerDto.class);
        testContext.ownerDto.setReadonly(true);
        testContext.savedOwnerDto = getOwnerRestClient().create(getSystemTenantUserAdminContext(),
                testContext.ownerDto);
    }

    private OwnerDto buildOwnerToUpdate() {
        final OwnerDto dto = FactoryDto.buildDto(OwnerDto.class);
        dto.setId(testContext.savedOwnerDto.getId());
        dto.setCode(testContext.savedOwnerDto.getCode());
        dto.setIdentifier("identifier");
        dto.setName(TestConstants.UPDATED + testContext.savedOwnerDto.getName());
        return dto;
    }

    @Then("^le serveur retourne le propriétaire mis à jour$")
    public void le_serveur_retourne_le_propriétaire_mis_à_jour() {
        assertThat(testContext.ownerDto.getName())
                .isEqualTo(TestConstants.UPDATED + testContext.savedOwnerDto.getName());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_OWNERS met à jour un propriétaire dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_OWNERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_OWNERS_met_à_jour_un_propriétaire_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_OWNERS() {
        testContext.ownerDto = getOwnerRestClient().update(getSystemTenantUserAdminContext(), buildOwnerToUpdate());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_OWNERS met à jour un propriétaire en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_OWNERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_OWNERS_met_à_jour_un_propriétaire_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_OWNERS() {
        testContext.ownerDto = buildOwnerToUpdate();
        try {
            getOwnerRestClient().update(getSystemTenantUserAdminContext(), testContext.ownerDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour du propriétaire")
    public void le_serveur_refuse_la_mise_à_jour_du_propriétaire() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: The owner "
                        + testContext.ownerDto.getName() + " can't be updated.");
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour d'un propriétaire$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_d_un_propriétaire() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur met à jour un propriétaire$")
    public void cet_utilisateur_met_à_jour_un_propriétaire() {
        try {
            getOwnerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .update(getContext(testContext.tenantIHMContext, testContext.tokenUser), buildOwnerToUpdate());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
