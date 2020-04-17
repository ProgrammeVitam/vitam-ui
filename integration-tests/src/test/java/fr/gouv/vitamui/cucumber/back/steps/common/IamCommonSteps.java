package fr.gouv.vitamui.cucumber.back.steps.common;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Common steps for the IAM API.
 *
 *
 */
public class IamCommonSteps extends CommonSteps {

    @Then("^le serveur retourne un utilisateur indisponible")
    public void le_serveur_retourne_un_utilisateur_indisponible() {
        assertThat(testContext.exception).isNotNull();
        final String message = testContext.exception.toString();
        final boolean isInvalidAuthentication =
                message.equals("fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException: User unavailable: " + testContext.authUserDto.getEmail());
        final boolean isInvalidFormat =
                message.equals("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: User unavailable: " + testContext.authUserDto.getEmail());
        assertThat(isInvalidAuthentication || isInvalidFormat).isTrue();
    }

    @Then("^le serveur retourne un utilisateur non trouvé$")
    public void le_serveur_retourne_un_utilisateur_non_trouvé() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception instanceof NotFoundException).isTrue();
    }

    @Given("^un client a été créé$")
    public void un_client_a_été_créé() {
        testContext.savedBasicCustomerDto = FactoryDto.buildDto(CustomerDto.class);
        testContext.basicCustomerDto = getCustomerWebClient(true, null, new String[] { ServicesData.ROLE_CREATE_CUSTOMERS })
                .create(getSystemTenantUserAdminContext(), testContext.savedBasicCustomerDto, Optional.empty());
    }

    @Given("^un groupe a été créé$")
    public void un_groupe_a_été_créé() {
        testContext.savedGroupDto = FactoryDto.buildDto(GroupDto.class);
        testContext.groupDto = getGroupRestClient().create(getSystemTenantUserAdminContext(), testContext.savedGroupDto);
    }

    @Given("^un profil a été créé$")
    public void un_profil_a_été_créé() {
        testContext.savedProfileDto = FactoryDto.buildDto(ProfileDto.class);
        testContext.profileDto = getProfileRestClient().create(getSystemTenantUserAdminContext(), testContext.savedProfileDto);
    }

    @Given("^un provider a été créé$")
    public void un_provider_a_été_créé() {
        testContext.identityProviderDto = FactoryDto.buildDto(IdentityProviderDto.class);
        testContext.savedIdentityProviderDto = getIdentityProviderRestClient().create(getSystemTenantUserAdminContext(), testContext.identityProviderDto);
    }

    @Given("^un utilisateur a été créé$")
    public void un_utilisateur_a_été_créé() {
        testContext.userDto = FactoryDto.buildDto(UserDto.class);
        testContext.savedUserDto = getUserRestClient().create(getSystemTenantUserAdminContext(), testContext.userDto);
    }

    @Given("^une subrogation a été créée$")
    public void une_subrogation_a_été_créée() {
        testContext.savedSubrogationDto = buildGoodSubrogation();
        writeSubrogation(testContext.savedSubrogationDto);
    }

    @When("^un utilisateur décline la subrogation$")
    public void un_utilisateur_décline_la_subrogation() {
        try {
            getSubrogationRestClient()
                    .decline(getContext(testContext.tenantDto != null ? testContext.tenantDto.getIdentifier() : proofTenantIdentifier,
                            testContext.authUserDto.getAuthToken()), testContext.savedSubrogationDto.getId());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^une subrogation a été créée pour le bon utilisateur$")
    public void une_subrogation_a_été_créée_pour_le_bon_utilisateur() {
        subrogationDto = buildGoodSubrogation();
        deleteAllSubrogations(subrogationDto);
        subrogationDto = getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        testContext.savedSubrogationDto = subrogationDto;
        testContext.authUserDto =
                (AuthUserDto) getCasRestClient(false, new Integer[]{TestConstants.CAS_TENANT_IDENTIFIER}, new String[]{ServicesData.ROLE_CAS_USERS})
                        .getUserByEmail(getContext(TestConstants.CAS_TENANT_IDENTIFIER, TestConstants.TOKEN_USER_CAS), subrogationDto.getSurrogate(),
                                Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
    }

    @Given("^on demande à ce que le subrogateur soit le user de test$")
    public void on_demande_à_ce_que_le_subrogateur_soit_le_user_de_test() {
        testContext.superUserEmail = TEST_USER_EMAIL;
    }

    @Given("un niveau {level}")
    public void un_niveau(final LevelParameterType level) throws Exception {
        testContext.level = level.getData();
    }

    @Given("^un tenant et customer system$")
    public void un_tenant_et_customer_system() throws Exception {
        testContext.mainTenant = proofTenantIdentifier;
        testContext.customerId = TestConstants.SYSTEM_CUSTOMER_ID;
    }

    @Given("^le profil est ajouté au groupe précédemment créé$")
    public void le_profil_est_ajouté_au_groupe_précédemment_créé() {
        final String profileId = testContext.profileDto.getId();
        final Map<String, Object> dto = new HashMap<>();
        dto.put("id", testContext.groupDto.getId());
        dto.put("description", UPDATED + testContext.savedGroupDto.getDescription());
        dto.put("profileIds", Arrays.asList(profileId));
        testContext.groupDto = getGroupRestClient().patch(getSystemTenantUserAdminContext(), dto);
    }
}
