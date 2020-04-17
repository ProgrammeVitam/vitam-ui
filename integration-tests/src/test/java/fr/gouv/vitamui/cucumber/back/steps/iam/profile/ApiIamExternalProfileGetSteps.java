package fr.gouv.vitamui.cucumber.back.steps.iam.profile;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_GET_PROFILES;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_USER_PROFILE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.external.client.ProfileExternalRestClient;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Groups dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalProfileGetSteps extends CommonSteps {

    private List<ProfileDto> profileDtos;

    private ProfileDto profileDto;

    private static final String WRONG_LEVEL = "WRONGLEVEL";

    @When("^un utilisateur avec le rôle ROLE_GET_PROFILES récupère tous les profils dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROFILES_récupère_tous_les_profils_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROFILES() {
        profileDtos = getProfileRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne tous les profils$")
    public void le_serveur_retourne_tous_les_profils() {
        assertThat(profileDtos).isNotNull();

        final int size = profileDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(8);
        for (final ProfileDto profileDto : profileDtos) {
            assertThat(profileDto.getCustomerId()).isEqualTo(SYSTEM_CUSTOMER_ID);
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROFILES sans le bon niveau récupère tous les profils dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROFILES_sans_le_bon_niveau_récupère_tous_les_profils_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROFILES() {
        profileDtos = getGoodClient().getAll(getGoodContextWithWrongLevel());
    }

    private ProfileExternalRestClient getGoodClient() {
        return getProfileRestClient(true, new Integer[] { proofTenantIdentifier }, new String[] { ROLE_GET_PROFILES });
    }

    private ExternalHttpContext getGoodContextWithWrongLevel() {
        final UserDto user = FactoryDto.buildDto(UserDto.class);
        user.setLevel(WRONG_LEVEL);
        user.setGroupId(TestConstants.TESTS_GROUP_ID);
        testContext.authUserDto = new AuthUserDto(user);
        return getContext(proofTenantIdentifier,
                tokenUserTest(new String[] { ROLE_GET_PROFILES }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, WRONG_LEVEL));
    }

    @Then("^le serveur ne retourne aucun profil$")
    public void le_serveur_ne_retourne_aucun_profil() {
        if (profileDtos != null) {
            assertThat(profileDtos.size()).isEqualTo(0);
        }
        else {
            assertThat(profileDtos).isNull();
        }
    }

    @Then("^le serveur ne retourne que le profil de l'utilisateur$")
    public void le_serveur_ne_retourne_que_le_profil_de_l_utilisateur() throws Exception {
        assertThat(profileDtos.size()).isEqualTo(1);
        assertThat(profileDtos.get(0).getLevel())
                .overridingErrorMessage(
                        "Le profil retourné n'a pas le niveau attendu : " + profileDtos.get(0).getLevel() + " au lieu de " + testContext.authUserDto.getLevel())
                .isEqualTo(testContext.authUserDto.getLevel());
        assertThat(profileDtos.get(0).getId()).isEqualTo(TestConstants.TESTS_PROFILE_ID);
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération de profils$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_de_profils() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère tous les profils$")
    public void cet_utilisateur_récupère_tous_les_profils() {
        testContext.level = "";
        try {
            getProfileRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROFILES récupère un profil par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROFILES_récupère_un_profil_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROFILES() {
        profileDto = getProfileRestClient().getOne(getSystemTenantUserAdminContext(), SYSTEM_USER_PROFILE_ID, Optional.empty());
    }

    @Then("^le serveur retourne le profil avec cet identifiant$")
    public void le_serveur_retourne_le_profil_avec_cet_identifiant() {
        assertThat(profileDto.getId()).isEqualTo(SYSTEM_USER_PROFILE_ID);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROFILES sans le bon niveau récupère un profil par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROFILES_sans_le_bon_niveau_récupère_un_profil_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROFILES() {
        try {
            profileDto = getGoodClient().getOne(getGoodContextWithWrongLevel(), SYSTEM_USER_PROFILE_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^cet utilisateur récupère un profil de niveau autorisé par son identifiant$")
    public void cet_utilisateur_récupère_un_profil_autorise_par_son_identifiant() {
        testContext.level = "";
        try {
            getProfileRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), SYSTEM_USER_PROFILE_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROFILES récupère tous les profils avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROFILES_récupère_tous_les_profils_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROFILES() {
        final PaginatedValuesDto<ProfileDto> profiles = getProfileRestClient().getAllPaginated(getSystemTenantUserAdminContext(), 0, 10, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        profileDtos = new ArrayList<>(profiles.getValues());
    }

    @Then("^le serveur retourne les profils paginés$")
    public void le_serveur_retourne_les_profils_paginés() {
        assertThat(profileDtos).isNotNull().isNotEmpty();
        assertThat(profileDtos.stream().anyMatch(c -> c.getId().equals(SYSTEM_USER_PROFILE_ID))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROFILES sans le bon niveau récupère tous les profils avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROFILES_sans_le_bon_niveau_récupère_tous_les_profils_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROFILES() {
        final PaginatedValuesDto<ProfileDto> profiles = getGoodClient().getAllPaginated(getGoodContextWithWrongLevel(), 0, 10, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        profileDtos = new ArrayList<>(profiles.getValues());
    }

    @When("^cet utilisateur récupère tous les profils avec pagination$")
    public void cet_utilisateur_récupère_tous_les_profils_avec_pagination() {
        testContext.level = "";
        try {
            getProfileRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getAllPaginated(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
