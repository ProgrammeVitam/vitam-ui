package fr.gouv.vitamui.cucumber.front.steps.cas;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gouv.vitamui.cucumber.front.pages.CasPage;
import fr.gouv.vitamui.cucumber.front.steps.common.CommonStepDefinitions;
import fr.gouv.vitamui.cucumber.front.utils.UserEnum;

public class CasStepDefinitions extends CommonStepDefinitions {

    private CasPage casPage;

    public void redirectToLoginPage() {
        casPage.waitForTitleToAppear();
        casPage.waitForEmailInputToBeVisible();
    }

    public void enterPassword(final String password) {
        casPage.waitForPasswordInputToBeVisible();
        casPage.enterPasswordLogin(password);
        casPage.clickOk();
    }

    public void enterEmail(final String email) {
        casPage.waitForEmailInputToBeVisible();
        casPage.enterEmailLogin(email);
        casPage.clickOk();
    }

    public void enterCredentials(final String login, final String password) {
        enterEmail(login);
        enterPassword(password);
    }

    public void enterCredentials(final UserEnum user) {
        enterCredentials(getEmailByUser(user), getPasswordByUser(user));
    }

    public void authenticateUserWithMultipleArchiveProfiles() {
        enterCredentials(getEmailByUser(UserEnum.ADMIN), getPasswordByUser(UserEnum.ADMIN));
    }

    public void checkErreurMessageIsPresent() {
        casPage.waitForErrorDisplayed();
        assertThat(casPage.hasErrorDispalyed())
                .overridingErrorMessage("le message d'echec mot de passe actuel est différent de celui attendu")
                .isTrue();
    }

    public void checkEmailUnknow() {
        casPage.waitForButtonBack();
        String emailErrorMessage = casPage.getErrorMessage();
        assertThat(emailErrorMessage)
                .overridingErrorMessage("le message d'echec email : " + emailErrorMessage
                        + " actuel est différent de celui attendu : " + "Votre compte a un problème de configuration.")
                .isEqualTo("Votre compte a un problème de configuration.");
    }

}
