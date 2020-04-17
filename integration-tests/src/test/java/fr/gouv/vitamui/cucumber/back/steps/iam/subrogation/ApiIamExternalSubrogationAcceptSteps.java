package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API subrogations dans IAM admin : opérations d'acceptation.
 *
 *
 */
public class ApiIamExternalSubrogationAcceptSteps extends CommonSteps {

    @When("^un utilisateur accepte la subrogation$")
    public void un_utilisateur_accepte_la_subrogation() {
        subrogationDto = getSubrogationRestClient().accept(getContext(testContext.tenantDto.getIdentifier(), testContext.authUserDto.getAuthToken()),
                testContext.savedSubrogationDto.getId());
    }

    @Then("^le serveur retourne la subrogation acceptée$")
    public void le_serveur_retourne_la_subrogation_acceptée() {
        assertThat(subrogationDto.getId()).isEqualTo(testContext.savedSubrogationDto.getId());
    }

    @When("^un autre utilisateur accepte la subrogation$")
    public void un_autre_utilisateur_accepte_la_subrogation() {
        try {
            getSubrogationRestClient().accept(getSystemTenantUserAdminContext(), testContext.savedSubrogationDto.getId());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse d'accepter la subrogation$")
    public void le_serveur_refuse_d_accepter_la_subrogation() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Users " + TestConstants.SYSTEM_USER_PREFIX_EMAIL
                        + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain + " can't accept subrogation of " + testContext.authUserDto.getEmail());
    }
}
