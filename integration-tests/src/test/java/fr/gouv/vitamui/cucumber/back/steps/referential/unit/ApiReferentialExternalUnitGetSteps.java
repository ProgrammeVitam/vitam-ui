/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.cucumber.back.steps.referential.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Teste l'API Unit dans Referentiel admin : opérations de récupération par requête dsl et id
 *
 *
 */
public class ApiReferentialExternalUnitGetSteps extends CommonSteps {

    private JsonNode response;

    private static final Optional<String> EMPTY_ID = Optional.empty();

    private static final Optional<String> WRONG_UNIT_ID = Optional.of("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

    private static final String FIND_DSL_QUERY =
        "{\n" +
        "  \"$query\": [{\n" +
        "    \"$match\": {\n" +
        "      \"id\": \"XXXXXXXXXXX\"\n" +
        "    }\n" +
        "  }],\n" +
        "  \"$filter\": {},\n" +
        "  \"$projection\": {}\n" +
        "}";

    @When("^un utilisateur recherche des unités avec une requête dsl invalide$")
    public void un_utilisateur_recherche_des_unités_avec_une_requête_dsl_invalide() {
        String dslQuery = "{}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonQuery;
        try {
            jsonQuery = mapper.readTree(dslQuery);
            response = getUnitRestClient().findUnitByDsl(getUnitAdminContext(), EMPTY_ID, jsonQuery);
        } catch (JsonProcessingException e) {
            fail("Failed to create dsl query from : " + dslQuery);
        }
    }

    @When("^un utilisateur recherche des unités avec une requête dsl valide sans identifiant d'unité$")
    public void un_utilisateur_recherche_des_unités_avec_une_requête_dsl_valide_sans_identifiant_dunité() {
        JsonNode jsonQuery;
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectReader reader = mapper.reader();
            jsonQuery = reader.readTree(FIND_DSL_QUERY);
            response = getUnitRestClient().findUnitByDsl(getUnitAdminContext(), EMPTY_ID, jsonQuery);
        } catch (JsonProcessingException e) {
            fail("Failed to create dsl query from : " + FIND_DSL_QUERY);
        }
    }

    @When("^un utilisateur recherche des unités avec une requête dsl valide mais un identifiant d'unité inconnu$")
    public void un_utilisateur_recherche_des_unités_avec_une_requête_dsl_valide_mais_un_identifiant_dunité_inconnu() {
        JsonNode jsonQuery;
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectReader reader = mapper.reader();
            jsonQuery = reader.readTree(FIND_DSL_QUERY);
            response = getUnitRestClient().findUnitByDsl(getUnitAdminContext(), WRONG_UNIT_ID, jsonQuery);
        } catch (JsonProcessingException e) {
            fail("Failed to create dsl query from : " + FIND_DSL_QUERY);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When(
        "^un utilisateur recherche des unités archivistiques avec une requête dsl valide mais un identifiant d'unité inconnu$"
    )
    public void un_utilisateur_recherche_des_unités_archivistiques_avec_une_requête_dsl_valide_mais_un_identifiant_dunité_inconnu() {
        JsonNode jsonQuery;
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectReader reader = mapper.reader();
            jsonQuery = reader.readTree(FIND_DSL_QUERY);
            response = getUnitRestClient()
                .findObjectMetadataById(getUnitAdminContext(), "AZERTYUIOPQSDFGHJKLMWXCVBN_AZERTYUIO", jsonQuery);
        } catch (JsonProcessingException e) {
            fail("Failed to create dsl query from : " + FIND_DSL_QUERY);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur indique_que_la_requête_est_invalide$")
    public void le_serveur_indique_que_la_requête_est_invalide() {
        assertThat(response).isNotNull();
        assertThat(response.get("httpCode").asInt()).isEqualTo(400);
        assertThat(response.get("code").asText()).isEqualTo("000601");
        assertThat(response.get("message").asText()).isEqualTo("Dsl query is not valid.");
    }

    @Then("^le serveur retourne une réponse valide$")
    public void le_serveur_retourne_une_réponse_valide() {
        assertThat(response).isNotNull();
        assertThat(response.get("httpCode").asInt()).isEqualTo(200);
        assertThat(response.get("$results").asText()).isEqualTo("");
    }

    @Then("^le serveur indique que l'unité n'existe pas$")
    public void le_serveur_indique_que_lunité_nexiste_pas() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
            "fr.gouv.vitamui.commons.api.exception.InternalServerException: Error with the response, get status: '400' and reason 'Bad Request'."
        );
    }

    @Then("^le serveur indique que l'unité archivistique n'existe pas$")
    public void le_serveur_indique_que_lunité_archivistique_nexiste_pas() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
            "fr.gouv.vitamui.commons.api.exception.InternalServerException: Error with the response, get status: '404' and reason 'Not Found'."
        );
    }
}
