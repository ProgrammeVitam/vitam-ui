package fr.gouv.vitamui.cucumber.front.steps.portal;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;

public class PortalSteps {

    @Steps(shared = true)
    private PortalStepDefinitions portalSteps;

    @Given("^l'utilisateur affiche la page d'accueil$")
    public void un_utilisateur_affiche_la_page_d_acceuil() throws InterruptedException {
        portalSteps.openHomePage();
    }

    @Then("^la page qui liste les applications est affichée$")
    public void la_page_qui_liste_les_applications_est_affichée() {
        portalSteps.checkAppListPageDisplayed();
    }

}
