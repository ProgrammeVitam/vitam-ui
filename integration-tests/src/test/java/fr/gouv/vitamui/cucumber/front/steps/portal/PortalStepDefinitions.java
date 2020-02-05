package fr.gouv.vitamui.cucumber.front.steps.portal;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gouv.vitamui.cucumber.front.pages.PortalPage;
import fr.gouv.vitamui.cucumber.front.steps.common.CommonStepDefinitions;

public class PortalStepDefinitions extends CommonStepDefinitions {

    PortalPage portalPage;

    public void waitForTitleToAppear() {
        portalPage.waitForTitleToAppear();
    }

    public void openHomePage() {
        portalPage.openPortal(portalUrl);
    }

    public void checkAppListPageDisplayed() {
        portalPage.waitForTitleToAppear();
        portalPage.waitForMenuToAppear();
        assertThat(portalPage.isAppListPageDisplayed())
                .overridingErrorMessage("La page de recherche des applications n'est pas affichée").isTrue();
    }

    public void openApplication(String applicationId) {
        portalPage.clickOn(portalPage.getApplicationElement(applicationId));

        // On Dev environnement we need to click a second time
        if (environnement.endsWith("dev")) {
            portalPage.clickOn(portalPage.getApplicationElement(applicationId));
        }
    }

}
