package fr.gouv.vitamui.cucumber.front.pages;

import org.openqa.selenium.By;

import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("/")
public class UserPage extends AbstractPage {

    private final String title = "Gestion utilisateurs";

    public void openUser(final String url) {
        openUrl(url);
    }

    public void waitForTitleToAppear() {
        this.waitForTitleToAppear(title);
    }

    public boolean userListIsDisplayed() {
        return this.find(By.tagName("app-user-list")).isDisplayed();
    }

}
