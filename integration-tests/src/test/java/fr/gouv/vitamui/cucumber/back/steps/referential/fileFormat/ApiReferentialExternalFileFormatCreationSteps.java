package fr.gouv.vitamui.cucumber.back.steps.referential.fileFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.utils.ResourcesUtils;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API FileFormat dans Referentiel admin : opérations de créations
 *
 *
 */
public class ApiReferentialExternalFileFormatCreationSteps extends CommonSteps {

    private JsonNode response;
   
    @When("^un utilisateur importe des formats de fichier à partir d'un fichier xml valide$")
    public void un_utilisateur_importe_des_formats_de_fichier_à_partir_d_un_fichier_xml_valide() throws IOException {
	    File file = new File("src/test/resources/data/import_fileFormats_valid.xml");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile("import_fileFormats_valid.xml",
	    	file.getName(), "application/xml", IOUtils.toByteArray(input));  	
	    response = getFileFormatWebClient().importFileFormats(getSystemTenantUserAdminContext(), multipartFile);
    }
    
    @Then("^l'import des formats de fichier a réussi$")
    public void l_import_des_formats_de_fichier_à_réussi() {
    	LOGGER.debug("############ file formats import response : {}", response);
    	
        assertThat(response).isNotNull();
        assertThat(response.get("httpCode").asInt()).isEqualTo(200);
    }
    
    @When("^un utilisateur importe des formats de fichier à partir d'un fichier xml invalide$")
    public void un_utilisateur_importe_des_formats_de_fichier_à_partir_d_un_fichier_xml_invalide() throws IOException {
	    File file = new File("src/test/resources/data/import_fileFormats_invalid.xml");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile("import_fileFormats_invalid.xml",
	    	file.getName(), "application/xml", IOUtils.toByteArray(input));
	    response = getFileFormatWebClient().importFileFormats(getSystemTenantUserAdminContext(), multipartFile);
    }
    
    @Then("^l'import des formats de fichier a échoué$")
    public void l_import_des_formats_de_fichier_a_échoué() {
        assertThat(response).isNotNull();
        assertThat(response.get("httpCode").asInt()).isEqualTo(400);
    }
}
