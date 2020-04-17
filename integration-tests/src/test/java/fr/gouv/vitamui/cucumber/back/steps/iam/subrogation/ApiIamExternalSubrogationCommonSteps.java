package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import com.mongodb.client.model.Filters;

import io.cucumber.java.en.Given;
import fr.gouv.vitamui.cucumber.common.CommonSteps;

/**
 * Teste l'API subrogations dans IAM admin : opérations de refus.
 *
 *
 */
public class ApiIamExternalSubrogationCommonSteps extends CommonSteps {

    @Given("^l'utilisateur (.*) n'a pas de subrogations en cours$")
    public void l_utilisateur_n_a_pas_de_subrogations_en_cours(final String email) {
        getSubrogationsCollection().deleteMany(Filters.or(Filters.eq("superUser", email)));
    }
}
