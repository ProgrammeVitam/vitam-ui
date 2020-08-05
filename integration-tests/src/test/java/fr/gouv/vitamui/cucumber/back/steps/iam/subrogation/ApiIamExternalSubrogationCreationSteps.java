package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API subrogations dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamExternalSubrogationCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_SUBROGATIONS_ajoute_une_nouvelle_subrogation_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SUBROGATIONS() {
        subrogationDto = buildGoodSubrogation();
        deleteAllSubrogations(subrogationDto);
        subrogationDto = getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
    }

    @Then("^le serveur retourne la subrogation créée$")
    public void le_serveur_retourne_la_subrogation_créée() {
        assertThat(subrogationDto).isNotNull();
    }

    @Given("^deux tenants et un rôle par défaut pour la création d'une subrogation$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_création_d_une_subrogation() {
        subrogationDto = buildGoodSubrogation();
        setMainTenant(testContext.tenantDto.getIdentifier());
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur ajoute une nouvelle subrogation$")
    public void cet_utilisateur_ajoute_une_nouvelle_subrogation() {
        try {
            getSubrogationRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .create(getContext(testContext.tenantIHMContext, testContext.tokenUser), subrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé non subrogeable dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_SUBROGATIONS_ajoute_une_nouvelle_subrogation_pour_un_subrogé_non_subrogeable_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SUBROGATIONS() {
        try {
            buildSubrogation(true, false, null, null);
            subrogationDto = getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de la subrogation à cause de l'utilisateur$")
    public void le_serveur_refuse_la_création_de_la_subrogation_à_cause_de_l_utilisateur() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException:  User is not subrogeable");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé dont le client est non subrogeable dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS$")
    public void n_utilisateur_avec_le_rôle_ROLE_CREATE_SUBROGATIONS_ajoute_une_nouvelle_subrogation_pour_un_subrogé_dont_le_client_est_non_subrogeable_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SUBROGATIONS() {
        try {
            UserDto surrogate = FactoryDto.buildDto(UserDto.class);
            surrogate.setSubrogeable(true);
            surrogate.setCustomerId(TestConstants.SYSTEM_CUSTOMER_ID);
            surrogate = getUserRestClient().create(getSystemTenantUserAdminContext(), surrogate);
            final SubrogationDto subrogationDto = IamDtoBuilder.buildSubrogationDto(null,
                    TestConstants.JULIEN_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                    TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
            subrogationDto.setSurrogate(surrogate.getEmail());
            getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de la subrogation à cause du client$")
    public void le_serveur_refuse_la_création_de_la_subrogation_à_cause_du_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException:  Customer is not subrogeable");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogateur non existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_SUBROGATIONS_ajoute_une_nouvelle_subrogation_pour_un_subrogateur_non_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SUBROGATIONS() {
        try {
            buildSubrogation(true, true, null, null);
            subrogationDto.setSuperUser(TestConstants.FAKE_USER_EMAIL);
            getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de la subrogation à cause du subrogateur non existant$")
    public void le_serveur_refuse_la_création_de_la_subrogation_à_cause_du_subrogateur_non_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: No superUser found with email : " + TestConstants.FAKE_USER_EMAIL);
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé non existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_SUBROGATIONS_ajoute_une_nouvelle_subrogation_pour_un_subrogé_non_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SUBROGATIONS() {
        try {
            final SubrogationDto subrogationDto = IamDtoBuilder.buildSubrogationDto(null,
                    TestConstants.JULIEN_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                    TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
            subrogationDto.setSurrogate(TestConstants.FAKE_USER_EMAIL);
            getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de la subrogation à cause du subrogé non existant$")
    public void le_serveur_refuse_la_création_de_la_subrogation_à_cause_du_subrogé_non_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: No surrogate found with email : " + TestConstants.FAKE_USER_EMAIL);
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé désactivé dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_SUBROGATIONS_ajoute_une_nouvelle_subrogation_pour_un_subrogé_désactivé_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_SUBROGATIONS() {
        try {
            buildSubrogation(true, true, UserStatusEnum.DISABLED, null);
            getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création de la subrogation à cause du subrogé désactivé$")
    public void le_serveur_refuse_la_création_de_la_subrogation_à_cause_du_subrogé_désactivé() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: User status is not enabled");
    }
}
