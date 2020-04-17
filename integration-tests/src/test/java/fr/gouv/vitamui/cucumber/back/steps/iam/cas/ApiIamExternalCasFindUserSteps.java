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
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API CAS dans IAM admin : opération de récupération d'un utilisateur.
 *
 *
 */
public class ApiIamExternalCasFindUserSteps extends CommonSteps {

    private static final String BAD_LOGIN = "badLogin";

    @When("^un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur par son email en demandant un token d'authentification dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_USERS_cherche_un_utilisateur_par_son_email_en_demandant_un_token_d_authentification_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_USERS() {
        testContext.authUserDto = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                        Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur désactivé par son email en demandant un token d'authentification dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_USERS_cherche_un_utilisateur_désactivé_par_son_email_en_demandant_un_token_d_authentification_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_USERS() {
        final UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setStatus(UserStatusEnum.DISABLED);
        getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto);
        testContext.authUserDto = new AuthUserDto(basicUserDto);
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(
                    getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(),
                    Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le bon utilisateur$")
    public void le_serveur_retourne_le_bon_utilisateur() {
        assertThat(testContext.authUserDto).isNotNull();
        assertThat(testContext.authUserDto.getId()).isEqualTo(SYSTEM_USER_ID);
    }

    @Then("^le serveur retourne le bon utilisateur avec son token d'authentification$")
    public void le_serveur_retourne_le_bon_utilisateur_avec_son_token_d_authentification() {
        le_serveur_retourne_le_bon_utilisateur();
        assertThat(testContext.authUserDto.getAuthToken()).isNotNull();
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur inexistant par son email dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_USERS_cherche_un_utilisateur_inexistant_par_son_email_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_USERS() {
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_USERS })
                    .getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), BAD_LOGIN, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^deux tenants et un rôle par défaut pour la recherche d'un utilisateur par email$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_recherche_d_un_utilisateur_par_email() {
        setMainTenant(casTenantIdentifier);
        setSecondTenant(proofTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur cherche un utilisateur par son email$")
    public void cet_utilisateur_cherche_un_utilisateur_par_son_email() {
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getUserByEmail(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser),
                    TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_USERS_cherche_un_utilisateur_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_USERS() {
        final UserDto basicUserDto = getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserById(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        SYSTEM_USER_ID);
        testContext.authUserDto = new AuthUserDto(basicUserDto);
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur inexistant par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_USERS_cherche_un_utilisateur_inexistant_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_USERS() {
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_USERS })
                    .getUserById(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), BAD_LOGIN);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur désactivé par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_USERS_cherche_un_utilisateur_désactivé_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_USERS() {
        UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setStatus(UserStatusEnum.DISABLED);
        basicUserDto = getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto);
        testContext.authUserDto = new AuthUserDto(basicUserDto);
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_USERS })
                    .getUserById(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getId());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^deux tenants et un rôle par défaut pour la recherche d'un utilisateur par identifiant$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_recherche_d_un_utilisateur_par_identifiant() {
        deux_tenants_et_un_rôle_par_défaut_pour_la_recherche_d_un_utilisateur_par_email();
    }

    @When("^cet utilisateur cherche un utilisateur par son identifiant$")
    public void cet_utilisateur_cherche_un_utilisateur_par_son_identifiant() {
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getUserById(getContext(testContext.tenantIHMContext, testContext.tokenUser), SYSTEM_USER_ID);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
