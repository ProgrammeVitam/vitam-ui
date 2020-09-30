package fr.gouv.vitamui.cucumber.back.steps.referential.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.utils.ResourcesUtils;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Contexte dans Referential admin : opérations de création.
 *
 *
 */

public class ApiReferentialExternalContextCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_CONTEXTS ajoute un nouveau contexte en utilisant un certificat full access avec le rôle ROLE_CREATE_CONTEXTS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_CONTEXTS_ajoute_un_nouveau_contexte_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_CONTEXTS() {
        final ContextDto dto = FactoryDto.buildDto(ContextDto.class);
        testContext.savedContextDto = getContextRestClient().create(getSystemTenantUserAdminContext(), dto);
    }
    
    @Then("^le serveur retourne le contexte créé$")
    public void le_status_de_la_réponse_doit_etre() {
        assertThat(testContext.savedContextDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }
    
    @Then("^(.*) permissions sont créés dans le contexte$")
    public void des_permissions_sont_créés_dans_le_contexte(final Integer profilesNumber) {
        assertThat(testContext.savedContextDto.getPermissions()).isNotNull().isNotEmpty();
        assertThat(testContext.savedContextDto.getPermissions().size()).isEqualTo(2);
        assertThat(testContext.savedContextDto.getPermissions().stream().anyMatch(permission -> 
        	(permission.getTenant().equals("tenant_1") || permission.getTenant().equals("tenant_2")) 
        )).isTrue();
    }


    @Then("^une trace de création d'un contexte est présente dans vitam$")
    public void une_trace_de_création_d_un_contexte_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.CONTEXT_ID, testContext.contextDto.getIdentifier(), "contexts", "EXT_VITAMUI_CREATE_CONTEXT");
    }
}
