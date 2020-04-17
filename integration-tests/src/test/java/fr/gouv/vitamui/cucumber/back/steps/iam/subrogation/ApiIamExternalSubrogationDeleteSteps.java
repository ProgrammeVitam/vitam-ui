package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.cucumber.common.CommonSteps;

/**
 * Teste l'API subrogations dans IAM admin : opérations de suppression.
 *
 *
 */
public class ApiIamExternalSubrogationDeleteSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_DELETE_SUBROGATIONS supprime une subrogation dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_DELETE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_DELETE_SUBROGATIONS_supprime_une_subrogation_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_DELETE_SUBROGATIONS() {
        getSubrogationRestClient().delete(getSystemTenantUserAdminContext(), testContext.savedSubrogationDto.getId());
    }

    @Then("^le serveur ne retourne plus la subrogaton supprimée$")
    public void le_serveur_ne_retourne_plus_la_subrogaton_supprimée() {
        try {
            getSubrogationRestClient().getOne(getSystemTenantUserAdminContext(), testContext.savedSubrogationDto.getId(),
                    Optional.empty());
            fail("should fail");
        }
        catch (final NotFoundException e) {
            // expected behavior
        }
    }

    @Given("^deux tenants et un rôle par défaut pour la suppression d'une subrogation$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_suppression_d_une_subrogation() {
        setMainTenant(testContext.tenantDto.getIdentifier());
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur supprime une subrogation$")
    public void cet_utilisateur_supprime_une_subrogation() {
        try {
            getSubrogationRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                .delete(getContext(testContext.tenantIHMContext, testContext.tokenUser), testContext.savedSubrogationDto.getId());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^une subrogation a été créée avec un autre subrogateur$")
    public void une_subrogation_a_été_créée_avec_un_autre_subrogateur() {
        buildSubrogation(true, true, null, UserStatusEnum.ENABLED);
        writeSubrogation(subrogationDto);
        testContext.savedSubrogationDto = subrogationDto;
    }

    @When("^un utilisateur avec le rôle ROLE_DELETE_SUBROGATIONS supprime une subrogation dont il n'est pas le subrogateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_DELETE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_DELETE_SUBROGATIONS_supprime_une_subrogation_dont_il_n_est_pas_le_subrogateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_DELETE_SUBROGATIONS() {
        try {
            getSubrogationRestClient().delete(getSystemTenantUserAdminContext(), testContext.savedSubrogationDto.getId());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la suppression de la subrogation à cause du mauvais subrogateur$")
    public void le_serveur_refuse_la_suppression_de_la_subrogation_à_cause_du_mauvais_subrogateur() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Only super user can stop subrogation");
    }
}
