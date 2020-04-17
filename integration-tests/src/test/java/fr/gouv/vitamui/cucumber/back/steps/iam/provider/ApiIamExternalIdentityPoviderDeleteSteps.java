package fr.gouv.vitamui.cucumber.back.steps.iam.provider;

import static org.assertj.core.api.Assertions.fail;

import java.util.Optional;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.cucumber.common.CommonSteps;

/**
 * Teste l'API Identity providers dans IAM admin : opérations de suppression.
 *
 *
 */
public class ApiIamExternalIdentityPoviderDeleteSteps extends CommonSteps {

    @When("^un utilisateur supprime le provider$")
    public void un_utilisateur_supprime_le_provider() {
        try {
            getIdentityProviderRestClient().delete(getSystemTenantUserAdminContext(), testContext.savedIdentityProviderDto.getId());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur ne retourne plus le provider supprimé$")
    public void le_serveur_ne_retourne_plus_le_provider_supprimé() {
        try {
            getIdentityProviderRestClient().getOne(getSystemTenantUserAdminContext(), testContext.savedIdentityProviderDto.getId(), Optional.empty());
            fail("should fail");
        } catch (final NotFoundException e) {
            // expected behavior
        }
    }
}
