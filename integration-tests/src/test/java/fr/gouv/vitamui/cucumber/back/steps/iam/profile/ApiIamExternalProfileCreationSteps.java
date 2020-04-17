package fr.gouv.vitamui.cucumber.back.steps.iam.profile;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_CREATE_PROFILES;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_USER_PROFILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API Profiles dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamExternalProfileCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un nouveau profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_ajoute_un_nouveau_profil_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROFILES() {
        testContext.profileDto = getProfileRestClient(true, null, new String[] { ROLE_CREATE_PROFILES }).create(getSystemTenantUserAdminContext(),
                FactoryDto.buildDto(ProfileDto.class));
    }

    @Then("^le serveur retourne le profil créé$")
    public void le_serveur_retourne_le_profil_créé() {
        assertThat(testContext.profileDto).isNotNull();
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_ajoute_un_profil_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROFILES() {
        testContext.profileDto = FactoryDto.buildDto(ProfileDto.class);
        testContext.profileDto.setReadonly(true);
        try {
            getProfileRestClient(true, null, new String[] { ROLE_CREATE_PROFILES }).create(getSystemTenantUserAdminContext(), testContext.profileDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du profil à cause du readonly$")
    public void le_serveur_refuse_la_création_du_profil_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create profile "
                + testContext.profileDto.getName() + ": readonly must be set to false");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_ajoute_un_profil_mais_avec_un_mauvais_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROFILES() {
        testContext.profileDto = FactoryDto.buildDto(ProfileDto.class);
        testContext.profileDto.setCustomerId(CLIENT1_CUSTOMER_ID);
        try {
            getProfileRestClient(true, null, new String[] { ROLE_CREATE_PROFILES }).create(getSystemTenantUserAdminContext(), testContext.profileDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du profil à cause du mauvais client$")
    public void le_serveur_refuse_la_création_du_profil_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create profile: customerId " + CLIENT1_CUSTOMER_ID
                        + " is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil mais avec un mauvais tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_ajoute_un_profil_mais_avec_un_mauvais_tenant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROFILES() {
        testContext.profileDto = FactoryDto.buildDto(ProfileDto.class);
        testContext.profileDto.setTenantIdentifier(client1TenantIdentifier);
        try {
            getProfileRestClient(true, null, new String[] { ROLE_CREATE_PROFILES }).create(getSystemTenantUserAdminContext(), testContext.profileDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du profil à cause du mauvais tenant$")
    public void le_serveur_refuse_la_création_du_profil_à_cause_du_mauvais_tenant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create profile: tenantIdentifier "
                        + client1TenantIdentifier + " is not allowed");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_ajoute_un_profil_avec_un_nom_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROFILES() {
        testContext.profileDto = FactoryDto.buildDto(ProfileDto.class);
        testContext.profileDto.setName(SYSTEM_USER_PROFILE_NAME);
        testContext.profileDto.setApplicationName("USERS_APP");
        try {
            getProfileRestClient(true, null, new String[] { ROLE_CREATE_PROFILES }).create(getSystemTenantUserAdminContext(), testContext.profileDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du profil à cause du nom existant$")
    public void le_serveur_refuse_la_création_du_profil_à_cause_du_nom_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create profile "
                + testContext.profileDto.getName() + ": profile already exists");
    }

    @Then("^le serveur refuse la création du profil à cause du paramètre manquant$")
    public void le_serveur_refuse_la_création_du_profil_à_cause_du_paramètre_manquant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create profile "
                + testContext.profileDto.getName() + ": externalParamId must be provided");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil avec un rôle qu'il ne possède pas dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_ajoute_un_profil_avec_un_rôle_qu_il_ne_possède_pas_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_PROFILES() {
        testContext.profileDto = FactoryDto.buildDto(ProfileDto.class);
        testContext.profileDto.setRoles(Arrays.asList(new Role(TestConstants.NEW_ROLE)));
        try {
            getProfileRestClient(true, null, new String[] { ROLE_CREATE_PROFILES }).create(getSystemTenantUserAdminContext(), testContext.profileDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du profil de l'utilisateur qui ne possède pas le bon rôle$")
    public void le_serveur_refuse_la_création_du_profil_de_l_utilisateur_qui_ne_possède_pas_le_bon_rôle() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create profile "
                + testContext.profileDto.getName() + ": role " + TestConstants.NEW_ROLE + " does not exist");
    }

    @Given("^deux tenants et un rôle par défaut pour l'ajout d'un profil$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_l_ajout_d_un_profil() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur ajoute un nouveau profil$")
    public void cet_utilisateur_ajoute_un_nouveau_profil() {
        final ProfileDto dto = FactoryDto.buildDto(ProfileDto.class);
        try {
            getProfileRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .create(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("cet utilisateur crée un nouveau profil avec pour attribut le niveau {level}")
    public void cet_utilisateur_crée_un_nouveau_profil_avec_pour_attribut_le_niveau_TEST_BIS(final LevelParameterType level) throws Exception {
        final ProfileDto profile = FactoryDto.buildDto(ProfileDto.class);
        profile.setLevel(level.getData());
        profile.setCustomerId(testContext.customerId);
        try {
            testContext.profileDto = getProfileRestClient().create(getContext(testContext.mainTenant, testContext.tokenUser), profile);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("cet utilisateur crée un nouveau profil utilisateur avec pour attribut le niveau {level}")
    public void cet_utilisateur_crée_un_nouveau_profil_utlisateur_avec_pour_attribut_le_niveau_XXX(final LevelParameterType level) throws Exception {
        final ProfileDto profile = FactoryDto.buildDto(ProfileDto.class);
        profile.setLevel(level.getData());
        profile.setApplicationName(CommonConstants.USERS_APPLICATIONS_NAME);
        profile.setCustomerId(testContext.customerId);
        try {
            testContext.profileDto = getProfileRestClient().create(getContext(testContext.mainTenant, testContext.tokenUser), profile);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le nouveau profil$")
    public void le_serveur_retourne_le_nouveau_profil() throws Exception {
        assertThat(testContext.profileDto).overridingErrorMessage("Le profil créé est null").isNotNull();
        assertThat(testContext.profileDto.getId()).overridingErrorMessage("L'id du profil créé est null").isNotNull();
        assertThat(testContext.profileDto.getId()).overridingErrorMessage("L'id du profil créé est vide").isNotEmpty();
    }

    @Then("le niveau du nouveau profil est bien le niveau {level}")
    public void le_niveau_du_nouvel_profil_est_bien_le_niveau(final LevelParameterType level) throws Exception {
        assertThat(testContext.profileDto.getLevel()).overridingErrorMessage("Le niveau de l'utilisateur créé est null").isNotNull();
        assertThat(testContext.profileDto.getLevel()).overridingErrorMessage("Le niveau de l'utilisateur créé n'est pas le niveau \"" + level.getData() + "\"")
                .isEqualTo(level.getData());
    }

    @Then("^une trace de création de profil est présente dans vitam$")
    public void une_trace_de_création_de_profil_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(testContext.profileDto.getCustomerId(), testContext.profileDto.getIdentifier(), "profiles", "EXT_VITAMUI_CREATE_PROFILE");
    }

}
