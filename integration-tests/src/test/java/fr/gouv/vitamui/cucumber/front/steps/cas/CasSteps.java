package fr.gouv.vitamui.cucumber.front.steps.cas;

import java.util.UUID;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.cucumber.front.steps.common.CommonStepDefinitions;
import net.thucydides.core.annotations.Steps;

public class CasSteps extends CommonStepDefinitions {

    @Steps(shared = true)
    private CasStepDefinitions casSteps;

    @Given("^l'utilisateur non authentifié est redirigé vers la page de login$")
    public void l_utilisateur_non_authentifié_est_redirigé_vers_la_page_de_login() {
        casSteps.redirectToLoginPage();
    }

    @When("^l'utilisateur saisit son email$")
    public void l_utilisateur_saisit_son_email() {
        casSteps.enterEmail(getEmailByUser(getContext().getCurrentUser()));
    }

    @When("^l'utilisateur saisit son mot de passe$")
    public void l_utilisateur_saisit_son_mot_de_passe() {
        casSteps.enterPassword(getPasswordByUser(getContext().getCurrentUser()));
    }

    @When("^l'utilisateur saisit un mot de passe incorrect$")
    public void l_utilisateur_saisit_son_mot_de_passe_incorect() {
        casSteps.enterPassword(UUID.randomUUID().toString());
    }

    @Then("^l'email est inconnu$")
    public void l_email_est_inconnu() {
        casSteps.checkEmailUnknow();
    }

    @Then("^un message d'erreur est affiché$")
    public void un_message_d_erreur_est_affiché() {
        casSteps.checkErreurMessageIsPresent();
    }

    @Then("^Une trace d'authentification de mon utilisateur est présente dans vitam$")
    public void une_trace_d_authentification_de_mon_utilisateur_est_présente_dans_vitam() {
        casSteps.checkTraceIsPresentForCurrentUser("EXT_VITAMUI_AUTHENTICATION_USER");
    }
}
