package fr.gouv.vitamui.cucumber.front.steps.common;

import fr.gouv.vitamui.cucumber.common.parametertypes.ApplicationParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.UserParameterType;
import fr.gouv.vitamui.cucumber.front.steps.cas.CasStepDefinitions;
import fr.gouv.vitamui.cucumber.front.steps.portal.PortalStepDefinitions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;

public class CommonStepsFront {

    @Steps
    private CommonStepDefinitions commonSteps;

    @Steps
    private CasStepDefinitions casSteps;

    @Steps
    private PortalStepDefinitions portalSteps;

    @Given("l'utilisateur est connecté dans l'application {application}")
    @When("l'utilisateur se connecte dans l'application {application}")
    public void un_utilisateur_est_connecté_dans_l_application(final ApplicationParameterType application) {
        portalSteps.openHomePage();
        commonSteps.waitForPreLoginPage();
        casSteps.redirectToLoginPage();
        casSteps.enterCredentials(commonSteps.getContext().getCurrentUser());
        portalSteps.waitForTitleToAppear();
        portalSteps.openApplication(application.getData().getId());
    }

    @Given("^l'utilisateur non authentifié est redirigé vers la page de pre-login$")
    public void l_utilisateur_non_authentifié_est_redirigé_vers_la_page_de_login() {
        commonSteps.waitForPreLoginPage();
    }

    @Given("l'utilisateur {user}")
    public void l_utilisateur(final UserParameterType user) {
        commonSteps.saveCurrentUser(user.getData());
    }

}
