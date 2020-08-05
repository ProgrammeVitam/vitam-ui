package fr.gouv.vitamui.cucumber.back.steps.iam.user;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_CREATE_USERS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.RoleParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.RolesParameterType;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API Users dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamUserCreationSteps extends CommonSteps {

    UserDto newUser;

    @When("^un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_USERS_ajoute_un_nouvel_utilisateur_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_USERS() {
        newUser = getUserRestClient(true, null, new String[] { ROLE_CREATE_USERS }).create(getSystemTenantUserAdminContext(),
                FactoryDto.buildDto(UserDto.class));
    }

    @Then("^le serveur retourne l'utilisateur créé$")
    public void le_serveur_retourne_l_utilisateur_créé() {
        assertThat(newUser).isNotNull();
    }

    @Then("^une trace de création utilisateur est présente dans vitam$")
    public void une_trace_de_création_utilisateur_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(newUser.getCustomerId(), newUser.getIdentifier(), "users", "EXT_VITAMUI_CREATE_USER");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_USERS_ajoute_un_nouvel_utilisateur_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_USERS() {
        newUser = FactoryDto.buildDto(UserDto.class);
        newUser.setReadonly(true);
        try {
            getUserRestClient(true, null, new String[] { ROLE_CREATE_USERS }).create(getSystemTenantUserAdminContext(), newUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de l'utilisateur à cause du readonly$")
    public void le_serveur_refuse_la_création_de_l_utilisateur_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create user "
                + newUser.getEmail() + ": readonly must be set to false");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_USERS_ajoute_un_nouvel_utilisateur_mais_avec_un_mauvais_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_USERS() {
        newUser = FactoryDto.buildDto(UserDto.class);
        newUser.setCustomerId(CLIENT1_CUSTOMER_ID);
        try {
            getUserRestClient(true, null, new String[] { ROLE_CREATE_USERS }).create(getSystemTenantUserAdminContext(), newUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de l'utilisateur à cause du mauvais client$")
    public void le_serveur_refuse_la_création_de_l_utilisateur_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create user: customerId " + CLIENT1_CUSTOMER_ID + " is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur mais avec un email existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_USERS_ajoute_un_nouvel_utilisateur_mais_avec_un_email_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_USERS() {
        newUser = FactoryDto.buildDto(UserDto.class);
        newUser.setEmail(TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
        try {
            getUserRestClient(true, null, new String[] { ROLE_CREATE_USERS }).create(getSystemTenantUserAdminContext(), newUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de l'utilisateur à cause de l'email existant$")
    public void le_serveur_refuse_la_création_de_l_utilisateur_à_cause_de_l_email_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create user "
                + TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain + ": mail already exists");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur mais avec un groupe inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_USERS_ajoute_un_nouvel_utilisateur_mais_avec_un_groupe_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_USERS() {
        newUser = FactoryDto.buildDto(UserDto.class);
        newUser.setGroupId("fakeGroup");
        try {
            getUserRestClient(true, null, new String[] { ROLE_CREATE_USERS }).create(getSystemTenantUserAdminContext(), newUser);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de l'utilisateur à cause du groupe inexistant$")
    public void le_serveur_refuse_la_création_de_l_utilisateur_à_cause_du_groupe_inexistant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create user " + newUser.getEmail() + ": group does not exist");
    }

    @Given("^deux tenants et un rôle par défaut pour l'ajout d'un utilisateur$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_l_ajout_d_un_utilisateur() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur ajoute un nouvel utilisateur$")
    public void cet_utilisateur_ajoute_un_nouvel_utilisateur() {
        final UserDto dto = FactoryDto.buildDto(UserDto.class);
        dto.setEmail(dto.getEmail().replaceFirst(CommonConstants.EMAIL_SEPARATOR + "(.*)", CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain));
        try {
            getUserRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .create(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("un utilisateur de ce niveau avec le rôle {role}")
    public void un_utilisateur_de_ce_niveau(final RoleParameterType role) {
        testContext.tokenUser = tokenUserTest(new String[]{role.getData()}, testContext.mainTenant, testContext.customerId, testContext.level);
    }

    @Given("un utilisateur de ce niveau avec les rôles {roles}")
    public void un_utilisateur_de_ce_niveau(final RolesParameterType roles) {
        testContext.tokenUser = tokenUserTest(roles.getData(), testContext.mainTenant, testContext.customerId, testContext.level);
    }

    @When("^cet utilisateur crée un nouvel utilisateur avec pour attribut ce groupe$")
    public void cet_utilisateur_crée_un_nouvel_utilisateur_avec_pour_attribut_ce_groupe() {
        final UserDto user = FactoryDto.buildDto(UserDto.class);
        user.setGroupId(testContext.groupDto.getId());
        try {
            newUser = getUserRestClient().create(getContext(testContext.mainTenant, testContext.tokenUser), user);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le nouvel utilisateur$")
    public void le_serveur_retourne_le_nouvel_utilisateur() {
        assertThat(newUser).overridingErrorMessage("L'utilisateur créé est null").isNotNull();
        assertThat(newUser.getId()).overridingErrorMessage("L'id de l'utilisateur créé est null").isNotNull();
        assertThat(newUser.getId()).overridingErrorMessage("L'id de l'utilisateur créé est vide").isNotEmpty();
    }

    @Then("^le nouvel utilisateur est bien affecté au groupe donné$")
    public void le_nouvel_utilisateur_est_bien_affecté_au_groupe_donné() {
        assertThat(newUser.getGroupId()).overridingErrorMessage("Le groupe de l'utilisateur créé est null").isNotNull();
        assertThat(newUser.getGroupId()).overridingErrorMessage("Le groupe de l'utilisateur créé est vide").isNotEmpty();
        assertThat(newUser.getGroupId()).overridingErrorMessage("Le groupe de l'utilisateur créé est incorrect").isEqualTo(testContext.groupDto.getId());
    }

    @Then("le niveau du nouvel utilisateur est bien le niveau {level}")
    public void le_niveau_du_nouvel_utilisateur_est_bien_le_niveau_vide(final LevelParameterType level) {
        assertThat(newUser.getLevel()).overridingErrorMessage("Le niveau de l'utilisateur créé est null").isNotNull();
        assertThat(newUser.getLevel()).overridingErrorMessage("Le niveau de l'utilisateur créé n'est pas le niveau \"" + level.getData() + "\"")
                .isEqualTo(level.getData());
    }

    @Then("^le serveur refuse l'accès l'API Users$")
    public void le_serveur_refuse_l_acces_à_l_api_users() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).contains("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create user");
        assertThat(testContext.exception.toString()).contains("level");
        assertThat(testContext.exception.toString()).contains("is not allowed");
    }

}
