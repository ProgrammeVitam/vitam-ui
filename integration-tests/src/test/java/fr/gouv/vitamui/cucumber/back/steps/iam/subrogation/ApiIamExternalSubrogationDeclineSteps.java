package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API subrogations dans IAM admin : opérations de refus.
 *
 *
 */
public class ApiIamExternalSubrogationDeclineSteps extends CommonSteps {

    @When("^le serveur accepte de décliner la subrogation$")
    public void le_serveur_accepte_de_décliner_la_subrogation() {
        assertThat(testContext.exception).isNull();
    }

    @When("^un autre utilisateur décline la subrogation$")
    public void un_autre_utilisateur_décline_la_subrogation() {
        try {
            getSubrogationRestClient().decline(getSystemTenantUserAdminContext(), testContext.savedSubrogationDto.getId());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse de décliner la subrogation$")
    public void le_serveur_refuse_de_décliner_la_subrogation() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Users " + TestConstants.SYSTEM_USER_PREFIX_EMAIL
                        + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain + " can't decline subrogation of " + testContext.authUserDto.getEmail());
    }
}
