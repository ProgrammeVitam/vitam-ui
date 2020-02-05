package fr.gouv.vitamui.cucumber.front.pages;

import org.openqa.selenium.By;

import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("/")
public class PreLoginPage extends AbstractPage {

    private final String title = "Vitam-UI";

    public void openPage(final String url) {
        openUrl(url);
    }

    public boolean isPreLoginPageDisplayed() {
        return $("//div[@class='login-background']").isDisplayed() && $("//div[@class='login-box']").isDisplayed()
                && $("//img[@class='vitam-logo']").isDisplayed();
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
