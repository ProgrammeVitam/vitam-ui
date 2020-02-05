package fr.gouv.vitamui.cucumber.front.steps.user;

import fr.gouv.vitamui.cucumber.front.pages.UserPage;
import fr.gouv.vitamui.cucumber.front.steps.common.CommonStepDefinitions;

public class UserStepDefinitions extends CommonStepDefinitions {

    UserPage userPage;

    public void waitForTitleToAppear() {
        userPage.waitForTitleToAppear();
    }

    public boolean userListIsDisplayed() {
        return userPage.userListIsDisplayed();
    }

}
