package fr.gouv.vitamui.cucumber.front.steps.common;

import cucumber.api.Transform;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import fr.gouv.vitamui.cucumber.front.steps.cas.CasStepDefinitions;
import fr.gouv.vitamui.cucumber.front.steps.portal.PortalStepDefinitions;
import fr.gouv.vitamui.cucumber.front.transformers.ApplicationTransformer;
import fr.gouv.vitamui.cucumber.front.transformers.UserTransformer;
import fr.gouv.vitamui.cucumber.front.utils.ApplicationEnum;
import fr.gouv.vitamui.cucumber.front.utils.UserEnum;
import net.thucydides.core.annotations.Steps;

public class CommonStepsFront {

    @Steps
    private CommonStepDefinitions commonSteps;

    @Steps
    private CasStepDefinitions casSteps;

    @Steps
    private PortalStepDefinitions portalSteps;

    @Given("^l'utilisateur est connecté dans l'application (.*)$")
    @When("^l'utilisateur se connecte dans l'application (.*)$")
    public void un_utilisateur_est_connecté_dans_l_application(
            @Transform(ApplicationTransformer.class) final ApplicationEnum application) {
        portalSteps.openHomePage();
        commonSteps.waitForPreLoginPage();
        casSteps.redirectToLoginPage();
        casSteps.enterCredentials(commonSteps.getContext().getCurrentUser());
        portalSteps.waitForTitleToAppear();
        portalSteps.openApplication(application.getId());
    }

    @Given("^l'utilisateur non authentifié est redirigé vers la page de pre-login$")
    public void l_utilisateur_non_authentifié_est_redirigé_vers_la_page_de_login() {
    	commonSteps.waitForPreLoginPage();
    }

    @Given("l'utilisateur (\\w+)$")
    public void l_utilisateur(@Transform(UserTransformer.class) final UserEnum user) {
        commonSteps.saveCurrentUser(user);
    }

}
