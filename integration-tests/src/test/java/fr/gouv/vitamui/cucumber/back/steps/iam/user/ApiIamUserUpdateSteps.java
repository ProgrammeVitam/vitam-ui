package fr.gouv.vitamui.cucumber.back.steps.iam.user;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_UPDATE_USERS;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Users dans IAM admin : opérations de mise à jour.
 *
 *
 */
public class ApiIamUserUpdateSteps extends CommonSteps {

    UserDto updatedUser;

    private UserDto buildUserToUpdate() {
        final UserDto dto = FactoryDto.buildDto(UserDto.class);
        dto.setId(testContext.savedUserDto.getId());
        dto.setFirstname(testContext.savedUserDto.getFirstname());
        dto.setLastname(TestConstants.UPDATED + testContext.savedUserDto.getLastname());
        dto.setEmail(testContext.savedUserDto.getEmail());
        return dto;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getSystemTenantUserAdminContext(), buildUserToUpdate());
    }

    @Then("^le serveur retourne l'utilisateur mis à jour$")
    public void le_serveur_retourne_l_utilisateur_mis_à_jour() {
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(testContext.savedUserDto.getId());
        assertThat(updatedUser.getEmail()).isEqualTo(testContext.savedUserDto.getEmail());
        assertThat(updatedUser.getLastname()).isEqualTo(TestConstants.UPDATED + testContext.savedUserDto.getLastname());
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour un utilisateur en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_un_utilisateur_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = buildUserToUpdate();
        updatedUser.setReadonly(true);
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getSystemTenantUserAdminContext(), updatedUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour de l'utilisateur à cause du readonly$")
    public void le_serveur_refuse_la_mise_à_jour_de_l_utilisateur_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update user "
                + updatedUser.getId() + ": readonly must be set to false");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour un utilisateur mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_un_utilisateur_mais_avec_un_mauvais_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = buildUserToUpdate();
        updatedUser.setCustomerId(CLIENT1_CUSTOMER_ID);
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getSystemTenantUserAdminContext(), updatedUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour de l'utilisateur à cause du mauvais client$")
    public void le_serveur_refuse_la_mise_à_jour_de_l_utilisateur_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update user: customerId " + CLIENT1_CUSTOMER_ID
                        + " is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour un utilisateur mais avec un email existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_un_utilisateur_mais_avec_un_email_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = buildUserToUpdate();
        updatedUser.setEmail(TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getSystemTenantUserAdminContext(), updatedUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour de l'utilisateur à cause de l'email existant$")
    public void le_serveur_refuse_la_mise_à_jour_de_l_utilisateur_à_cause_de_l_email_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update user " + updatedUser.getId() + ": mail already exists");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour un utilisateur mais avec un groupe inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_met_à_jour_un_utilisateur_mais_avec_un_groupe_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = buildUserToUpdate();
        updatedUser.setGroupId("fakeGroup");
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getSystemTenantUserAdminContext(), updatedUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour de l'utilisateur à cause du groupe inexistant$")
    public void le_serveur_refuse_la_mise_à_jour_de_l_utilisateur_à_cause_du_groupe_inexistant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update user "
                + updatedUser.getId() + ": group does not exist");
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS sans le bon niveau met à jour un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_sans_le_bon_niveau_met_à_jour_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = buildUserToUpdate();
        try {
            getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getContext(proofTenantIdentifier,
                    tokenUserTest(new String[] { ROLE_UPDATE_USERS }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")), updatedUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la mise à jour de l'utilisateur à cause du mauvais niveau$")
    public void le_serveur_refuse_la_mise_à_jour_de_l_utilisateur_à_cause_du_mauvais_niveau() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to update user "
                + updatedUser.getId() + ": level  is not allowed");
    }

    @Given("^un utilisateur désactivé a été créé$")
    public void un_utilisateur_désactivé_a_été_créé() {
        testContext.userDto = FactoryDto.buildDto(UserDto.class);
        testContext.userDto.setStatus(UserStatusEnum.DISABLED);
        testContext.savedUserDto = getUserRestClient().create(getSystemTenantUserAdminContext(), testContext.userDto);
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_USERS active un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_USERS_active_un_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_USERS() {
        updatedUser = getUserRestClient(true, null, new String[] { ROLE_UPDATE_USERS }).update(getSystemTenantUserAdminContext(), buildUserToUpdate());
    }

    @Then("^le serveur réactive l'utilisateur et supprime son mot de passe$")
    public void le_serveur_réactive_l_utilisateur_et_supprime_son_mot_de_passe() {
        le_serveur_retourne_l_utilisateur_mis_à_jour();
        assertThat(updatedUser.getPasswordExpirationDate()).isBefore(OffsetDateTime.now());
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatusEnum.ENABLED);
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour d'un utilisateur$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_d_un_utilisateur() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur met à jour un utilisateur$")
    public void cet_utilisateur_met_à_jour_un_utilisateur() {
        final UserDto dto = buildUserToUpdate();
        try {
            getUserRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .update(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
