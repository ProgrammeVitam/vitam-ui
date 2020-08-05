package fr.gouv.vitamui.cucumber.front.steps.user;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;

public class UserSteps {

    @Steps(shared = true)
    UserStepDefinitions userSteps;

    @Then("^la liste des utilisateurs est affichée$")
    public void la_liste_des_utilisateurs_est_affichée() throws Exception {
        assertThat(userSteps.userListIsDisplayed()).isTrue();
    }

}
