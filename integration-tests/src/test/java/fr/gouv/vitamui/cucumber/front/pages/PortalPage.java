package fr.gouv.vitamui.cucumber.front.pages;

import org.openqa.selenium.By;

import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("/")
public class PortalPage extends AbstractPage {

    private final String title = "Vitam-UI";

    public void openPortal(final String url) {
        openUrl(url);
    }

    public boolean isAppListPageDisplayed() {
        return $("//div[@class='account']").isDisplayed() && $("//div[@class='user-app-container']").isDisplayed()
                && $("//div[@class='admin-app-container']").isDisplayed()
                && $("//div[@class='settings-app-container']").isDisplayed();
    }

    public void waitForTitleToAppear() {
        this.waitForTitleToAppear(title);
    }

    public void waitForMenuToAppear() {
        waitFor(find(By.tagName("vitamui-menu-tile")).getWrappedElement());
    }

    public WebElementFacade getApplicationElement(String applicationId) {
        return find(By.id(applicationId));
    }

}
