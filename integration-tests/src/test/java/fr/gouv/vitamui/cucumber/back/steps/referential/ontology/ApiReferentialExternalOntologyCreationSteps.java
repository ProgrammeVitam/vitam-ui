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
package fr.gouv.vitamui.cucumber.back.steps.referential.ontology;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste l'API Ontology dans Referentiel admin : opérations de créations
 *
 *
 */
public class ApiReferentialExternalOntologyCreationSteps extends CommonSteps {

    private JsonNode response;

    @When("^un utilisateur importe des ontologies à partir d'un fichier json valide$")
    public void un_utilisateur_importe_des_ontologies_à_partir_d_un_fichier_json_valide() throws IOException {
        File file = new File("src/test/resources/data/import_ontologies_valid.json");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
            "import_ontologies_valid.json",
            file.getName(),
            "text/json",
            IOUtils.toByteArray(input)
        );
        response = getOntologyWebClient().importOntologies(getSystemTenantUserAdminContext(), multipartFile);
    }

    @Then("^l'import des ontologies a réussi$")
    public void l_import_des_ontologies_à_réussi() {
        assertThat(response).isNotNull();
        assertThat(response.get("httpCode").asInt()).isEqualTo(200);
    }

    @When("^un utilisateur importe des ontologies à partir d'un fichier json invalide$")
    public void un_utilisateur_importe_des_ontologies_à_partir_d_un_fichier_json_invalide() throws IOException {
        File file = new File("src/test/resources/data/import_ontologies_invalid.json");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
            "import_ontologies_invalid.json",
            file.getName(),
            "text/json",
            IOUtils.toByteArray(input)
        );
        response = getOntologyWebClient().importOntologies(getSystemTenantUserAdminContext(), multipartFile);
    }

    @Then("^l'import des ontologies a échoué$")
    public void l_import_des_ontologies_a_échoué() {
        assertThat(response).isNotNull();
        assertThat(response.get("httpCode").asInt()).isEqualTo(412);
    }
}
