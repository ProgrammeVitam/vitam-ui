package fr.gouv.vitamui.cucumber.back.steps.referential.securityprofile;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.utils.FactoryDto;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Security Profile dans Referential admin : opérations de création.
 *
 *
 */

public class ApiReferentialExternalSecurityProfileCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_SECURITY_PROFILE ajoute un nouveau profile de sécurité en utilisant un certificat full access avec le rôle ROLE_CREATE_SECURITY_PROFILE$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_SECURITY_PROFILE_ajoute_un_nouveau_profile_de_sécurité_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SECURITY_PROFILE() {
        final SecurityProfileDto dto = FactoryDto.buildDto(SecurityProfileDto.class);
        testContext.savedSecurityProfileDto = getSecurityProfileRestClient().create(getSystemTenantUserAdminContext(), dto);
    }

    @Then("^le serveur retourne le profile de sécurité créé$")
    public void le_status_de_la_réponse_doit_etre() {
        assertThat(testContext.savedSecurityProfileDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }
}
