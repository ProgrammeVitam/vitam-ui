package fr.gouv.vitamui.cucumber.front.pages;

import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.annotations.DefaultUrl;
import org.openqa.selenium.By;

@DefaultUrl("/")
public class CasPage extends AbstractPage {

    private final String title = "Vitam-UI";

    public void openPortal(final String portalUrl) {
        openUrl(portalUrl);
    }

    public void enterEmailLogin(final String email) {
        find(By.cssSelector(".login-box input[type='text']")).sendKeys(email);
    }

    public void enterPasswordLogin(final String password) {
        $(".login-box input[type='password']").sendKeys(password);
    }

    public void clickOk() {
        $(".login-box button[type='submit']").click();
    }

    public String getErrorMessage() {
        return find(By.tagName("h2")).getText();
    }

    public void waitForErrorDisplayed() {
        waitFor(getErrorElement().getElement());
    }

    public boolean hasErrorDispalyed() {
        return getErrorElement().isDisplayed();
    }

    private WebElementFacade getErrorElement() {
        return find(By.xpath("//div[@class='error-field']"));
    }

    public void waitForEmailInputToBeVisible() {
        waitFor(find(By.id("username")).getElement());
    }

    public void waitForPasswordInputToBeVisible() {
        waitFor(find(By.id("password")).getElement());
    }

    public void waitForTitleToAppear() {
        this.waitForTitleToAppear(title);
    }

    public void waitForButtonBack() {
        waitFor(find(By.className("back")).getElement());
    }
}
