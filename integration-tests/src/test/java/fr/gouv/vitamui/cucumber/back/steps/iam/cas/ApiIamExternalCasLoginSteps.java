package fr.gouv.vitamui.cucumber.back.steps.iam.cas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API CAS dans IAM admin : opération d'authentification.
 *
 *
 */
public class ApiIamExternalCasLoginSteps extends CommonSteps {

    private static final String BAD_PASSWORD = "badPassword";

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGIN_authentifie_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGIN() {
        testContext.authUserDto = new AuthUserDto(
                getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN }).login(
                        getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain, adminPassword, null, null));
    }

    @Then("^le serveur retourne l'utilisateur authentifié$")
    public void le_status_de_la_réponse_doit_etre() {
        assertThat(testContext.authUserDto).isNotNull();
        assertThat(testContext.authUserDto.getId()).isEqualTo(SYSTEM_USER_ID);
    }

    @Given("^deux tenants et un rôle par défaut pour authentifier un utilisateur$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_authentifier_un_utilisateur() {
        setMainTenant(casTenantIdentifier);
        setSecondTenant(proofTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur authentifie un utilisateur$")
    public void cet_utilisateur_authentifie_un_utilisateur() {
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).login(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser),
                    TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain, adminPassword, null, null);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur avec un mauvais mot de passe dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGIN_authentifie_un_utilisateur_avec_un_mauvais_mot_de_passe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGIN() {
        createUserAndSetPassword();
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN }).login(
                    getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(), BAD_PASSWORD, null,
                    null);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    private void createUserAndSetPassword() {
        createBasicUser();
        getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_CHANGE_PASSWORD }).changePassword(
                getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(), adminPassword);
    }

    @Then("^le serveur retourne une erreur bad credentials")
    public void le_serveur_retourne_une_erreur_bad_credentials() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException: Bad credentials for username: "
                        + testContext.authUserDto.getEmail());
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur générique dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGIN_authentifie_un_utilisateur_générique_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGIN() {
        final UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setType(UserTypeEnum.GENERIC);
        testContext.authUserDto = new AuthUserDto(getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto));
        final String login = testContext.authUserDto.getEmail();
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN })
                    .login(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), login, adminPassword, null, null);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur avec un mauvais mot de passe trop de fois dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGIN_authentifie_un_utilisateur_avec_un_mauvais_mot_de_passe_trop_de_fois_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGIN() {
        createUserAndSetPassword();
        final ExternalHttpContext context = getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS);
        final String login = testContext.authUserDto.getEmail();
        for (int i = 0; i < 4; i++) {
            try {
                getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN }).login(context,
                        login, BAD_PASSWORD, null, null);
            }
            catch (final RuntimeException e) {
                // ignore the first times
            }
        }
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN }).login(context, login,
                    BAD_PASSWORD, null, null);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne une erreur de trop d'essais")
    public void le_serveur_retourne_une_erreur_de_trop_d_essais() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.TooManyRequestsException: Too many login attempts for username: "
                        + testContext.authUserDto.getEmail());
    }

    @Then("^l'utilisateur est bloqué$")
    public void l_utilisateur_est_bloqué() {
        final ExternalHttpContext context = getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS);
        final UserDto basicUserDto = getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(context, testContext.authUserDto.getEmail(), Optional.empty());
        assertThat(basicUserDto.getStatus()).isEqualTo(UserStatusEnum.BLOCKED);
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur désactivé dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGIN_authentifie_un_utilisateur_désactivé_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGIN() {
        final UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setStatus(UserStatusEnum.DISABLED);
        testContext.authUserDto = new AuthUserDto(getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto));
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN }).login(
                    getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(), adminPassword, null,
                    null);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur inexistant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGIN_authentifie_un_utilisateur_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGIN() {
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN })
                    .login(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), TestConstants.BAD_LOGIN, adminPassword, null, null);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
