package fr.gouv.vitamui.cucumber.back.steps.iam.cas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API CAS dans IAM admin : opération de changement de mot de passe.
 *
 *
 */
public class ApiIamExternalCasChangePasswordSteps extends CommonSteps {

    private static final String NEW_PASSWORD = "newPassword";

    private OffsetDateTime pwdExpirationDate;

    @When("^un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD_change_le_mot_de_passe_d_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD() {
        createBasicUser();
        pwdExpirationDate = testContext.authUserDto.getPasswordExpirationDate();
        getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_CHANGE_PASSWORD }).changePassword(
                getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(), NEW_PASSWORD);
    }

    @Then("^le mot de passe a été changé car l'utilisateur peut s'authentifier avec son nouveau mot de passe$")
    public void le_mot_de_passe_a_été_changé_car_l_utilisateur_peut_s_authentifier_avec_son_nouveau_mot_de_passe() {
        testContext.authUserDto = new AuthUserDto(
                getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGIN }).login(
                        getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(), NEW_PASSWORD, null,
                        null));
        assertThat(testContext.authUserDto.getPasswordExpirationDate().isEqual(pwdExpirationDate)).isFalse();
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur générique dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD_change_le_mot_de_passe_d_un_utilisateur_générique_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD() {
        final UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setType(UserTypeEnum.GENERIC);
        testContext.authUserDto = new AuthUserDto(getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto));
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_CHANGE_PASSWORD })
                    .changePassword(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(),
                            NEW_PASSWORD);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur désactivé dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD_change_le_mot_de_passe_d_un_utilisateur_désactivé_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD() {
        final UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setStatus(UserStatusEnum.ANONYM);
        testContext.authUserDto = new AuthUserDto(getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto));
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_CHANGE_PASSWORD })
                    .changePassword(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), testContext.authUserDto.getEmail(),
                            NEW_PASSWORD);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur inexistant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD_change_le_mot_de_passe_d_un_utilisateur_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_CHANGE_PASSWORD() {
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_CHANGE_PASSWORD })
                    .changePassword(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), TestConstants.BAD_LOGIN, NEW_PASSWORD);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^deux tenants et un rôle par défaut pour le changement de mot de passe$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_le_changement_de_mot_de_passe() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur change le mot de passe d'un utilisateur$")
    public void cet_utilisateur_change_le_mot_de_passe_d_un_utilisateur() {
        createBasicUser();
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .changePassword(getContext(testContext.tenantIHMContext, testContext.tokenUser), testContext.authUserDto.getEmail(), NEW_PASSWORD);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
