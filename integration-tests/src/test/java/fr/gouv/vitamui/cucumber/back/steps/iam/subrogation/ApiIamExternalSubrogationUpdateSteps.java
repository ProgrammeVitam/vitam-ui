package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.common.CommonSteps;

/**
 * Teste l'API subrogations dans IAM admin : opération de mise à jour.
 *
 *
 */
public class ApiIamExternalSubrogationUpdateSteps extends CommonSteps {

    @When("^un utilisateur met à jour une subrogation$")
    public void un_utilisateur_met_à_jour_une_subrogation() {
        try {
            getSubrogationRestClient().update(getSystemTenantUserAdminContext(), testContext.savedSubrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
