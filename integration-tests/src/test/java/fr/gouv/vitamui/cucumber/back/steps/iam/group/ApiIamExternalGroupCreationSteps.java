package fr.gouv.vitamui.cucumber.back.steps.iam.group;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_CREATE_GROUPS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.ADMIN_GROUP_NAME;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.model.Filters;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API Groups dans IAM admin : opérations de création.
 *
 *
 */
public class ApiIamExternalGroupCreationSteps extends CommonSteps {

    private GroupDto savedGroupDto;

    private GroupDto groupDto;

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_ajoute_un_nouveau_groupe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(getSystemTenantUserAdminContext(),
                FactoryDto.buildDto(GroupDto.class));
    }

    @Then("^le serveur retourne le groupe créé$")
    public void le_serveur_retourne_le_groupe_créé() {
        assertThat(groupDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_ajoute_un_nouveau_groupe_en_readonly_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        savedGroupDto = FactoryDto.buildDto(GroupDto.class);
        savedGroupDto.setReadonly(true);
        try {
            groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(getSystemTenantUserAdminContext(), savedGroupDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du groupe à cause du readonly$")
    public void le_serveur_refuse_la_création_du_groupe_à_cause_du_readonly() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create group "
                + savedGroupDto.getName() + ": readonly must be set to false");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS mais avec un mauvais client ajoute un nouveau groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_mais_avec_un_mauvais_client_ajoute_un_nouveau_groupe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        savedGroupDto = FactoryDto.buildDto(GroupDto.class);
        savedGroupDto.setCustomerId(CLIENT1_CUSTOMER_ID);
        testContext.level = "";
        try {
            groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(
                    getContext(client1TenantIdentifier,
                            tokenUserTest(new String[] { ROLE_CREATE_GROUPS }, client1TenantIdentifier, CLIENT1_CUSTOMER_ID, testContext.level)),
                    savedGroupDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du groupe à cause du mauvais client$")
    public void le_serveur_refuse_la_création_du_groupe_à_cause_du_mauvais_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create group "
                + savedGroupDto.getName() + ": profile and group customerId must be equals");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS et un client désactivé ajoute un nouveau groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_et_un_client_désactivé_ajoute_un_nouveau_groupe_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        CustomerDto customer = FactoryDto.buildDto(CustomerDto.class);
        final String ownerName = customer.getOwners().get(0).getName();
        customer = getCustomerWebClient().create(getSystemTenantUserAdminContext(), customer, Optional.empty());
        customer.setEnabled(false);
        customer = getCustomerRestClient().update(getSystemTenantUserAdminContext(), customer);
        final String customerId = customer.getId();

        final QueryDto criteria = QueryDto.criteria("name", ownerName, CriterionOperator.EQUALS);

        final TenantDto tenant = getTenantRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty()).get(0);
        final int tenantIdentifier = tenant.getIdentifier();

        savedGroupDto = FactoryDto.buildDto(GroupDto.class);
        savedGroupDto.setCustomerId(customerId);
        testContext.level = "";
        try {
            groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(
                    getContext(tenantIdentifier, tokenUserTest(new String[] { ROLE_CREATE_GROUPS }, tenantIdentifier, customerId, testContext.level)),
                    savedGroupDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du groupe à cause du client désactivé$")
    public void le_serveur_refuse_la_création_du_groupe_à_cause_du_client_désactivé() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create group "
                + savedGroupDto.getName() + ": customer must be enabled");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_ajoute_un_nouveau_groupe_avec_un_nom_existant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        savedGroupDto = FactoryDto.buildDto(GroupDto.class);
        savedGroupDto.setName(ADMIN_GROUP_NAME);
        try {
            groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(getSystemTenantUserAdminContext(), savedGroupDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du groupe à cause du nom existant$")
    public void le_serveur_refuse_la_création_du_groupe_à_cause_du_nom_existant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create group " + savedGroupDto.getName() + ": group already exists");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe avec un profil inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_ajoute_un_nouveau_groupe_avec_un_profil_inexistant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        savedGroupDto = FactoryDto.buildDto(GroupDto.class);
        savedGroupDto.setProfileIds(Arrays.asList("fakeId"));
        try {
            groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(getSystemTenantUserAdminContext(), savedGroupDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du groupe à cause du profil inexistant$")
    public void le_serveur_refuse_la_création_du_groupe_à_cause_du_profil_inexistant() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create group " + savedGroupDto.getName() + ": no profiles");
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe avec un profil d'un autre client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_GROUPS_ajoute_un_nouveau_groupe_avec_un_profil_d_un_autre_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_GROUPS() {
        savedGroupDto = FactoryDto.buildDto(GroupDto.class);

        final Bson filter = Filters.ne("customerId", TestConstants.SYSTEM_CUSTOMER_ID);
        final Document profileFromAnotherClient = getProfilesCollection().find(filter).first();
        savedGroupDto.setProfileIds(Arrays.asList(profileFromAnotherClient.get("_id").toString()));
        try {
            groupDto = getGroupRestClient(true, null, new String[] { ROLE_CREATE_GROUPS }).create(getSystemTenantUserAdminContext(), savedGroupDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse la création du groupe à cause du profil d'un autre client$")
    public void le_serveur_refuse_la_création_du_groupe_à_cause_du_profil_d_un_autre_client() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create group "
                + savedGroupDto.getName() + ": profile and group customerId must be equals");
    }

    @Given("^deux tenants et un rôle par défaut pour l'ajout d'un groupe$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_l_ajout_d_un_groupe() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur ajoute un nouveau groupe$")
    public void cet_utilisateur_ajoute_un_nouveau_groupe() {
        final GroupDto dto = FactoryDto.buildDto(GroupDto.class);
        try {
            getGroupRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .create(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^un groupe de ce niveau$")
    public void un_groupe_de_ce_niveau() throws Exception {
        GroupDto group = FactoryDto.buildDto(GroupDto.class);
        group.setLevel(testContext.level);
        group.setCustomerId(testContext.customerId);
        writeProfile(group.getName(), testContext.level, testContext.mainTenant, new String[]{}, testContext.customerId);
        group.setProfileIds(Collections.singletonList(group.getName()));
        group = getGroupRestClient().create(getSystemTenantUserAdminContext(), group);
        testContext.groupDto = group;
    }

    @When("cet utilisateur crée un nouveau groupe de niveau {level}")
    public void cet_utilisateur_crée_un_nouveau_groupe_de_niveau_x(final LevelParameterType level) throws Exception {
        final GroupDto group = FactoryDto.buildDto(GroupDto.class);
        group.setLevel(level.getData());
        group.setCustomerId(testContext.customerId);
        writeProfile(group.getName(), level.getData(), testContext.mainTenant, new String[]{}, testContext.customerId);
        group.setProfileIds(Arrays.asList(group.getName()));
        try {
            testContext.groupDto = getGroupRestClient().create(getContext(testContext.mainTenant, testContext.tokenUser), group);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("un groupe de niveau {level}")
    public void un_nouveau_groupe_de_niveau_x(final LevelParameterType level) throws Exception {
        final GroupDto group = FactoryDto.buildDto(GroupDto.class);
        group.setLevel(level.getData());
        group.setCustomerId(testContext.customerId);
        writeProfile(group.getName(), level.getData(), testContext.mainTenant, new String[]{ServicesData.ROLE_GET_USERS}, testContext.customerId);
        group.setProfileIds(Arrays.asList(group.getName()));
        try {
            testContext.groupDto = getGroupRestClient().create(getSystemTenantUserAdminContext(), group);
        } catch (final RuntimeException e) {
            testContext.exception = e;
            return;
        }
        getGroupRestClient().getOne(getSystemTenantUserAdminContext(), testContext.groupDto.getId(), Optional.empty());
    }

    @Then("^le serveur retourne le nouveau groupe$")
    public void le_serveur_retourne_le_nouveau_groupe() throws Exception {
        assertThat(testContext.groupDto).overridingErrorMessage("Le groupe créé est null").isNotNull();
        assertThat(testContext.groupDto.getId()).overridingErrorMessage("L'id du groupe créé est null").isNotNull();
        assertThat(testContext.groupDto.getId()).overridingErrorMessage("L'id du groupe créé est vide").isNotEmpty();
    }

    @Then("le niveau du nouveau groupe est bien le niveau {level}")
    public void le_niveau_du_nouveau_groupe_est_bien_le_niveau(final LevelParameterType level) throws Exception {
        assertThat(testContext.groupDto.getLevel()).overridingErrorMessage("Le niveau de l'utilisateur créé est null").isNotNull();
        assertThat(testContext.groupDto.getLevel()).overridingErrorMessage("Le niveau de l'utilisateur créé n'est pas le niveau \"" + level.getData() + "\"")
                .isEqualTo(level.getData());
    }

    @Then("^une trace de création du groupe est présente dans vitam$")
    public void une_trace_de_création__du_groupe_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(groupDto.getCustomerId(), groupDto.getIdentifier(), "groups", "EXT_VITAMUI_CREATE_GROUP");
    }

}
