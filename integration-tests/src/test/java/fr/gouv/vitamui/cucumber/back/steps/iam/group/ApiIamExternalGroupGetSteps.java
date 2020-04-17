package fr.gouv.vitamui.cucumber.back.steps.iam.group;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_GET_GROUPS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.ADMIN_GROUP_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_USER_PROFILE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.external.client.GroupExternalRestClient;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Groups dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalGroupGetSteps extends CommonSteps {

    private List<GroupDto> groupDtos;

    private GroupDto groupDto;

    private static final String WRONG_LEVEL = "WRONGLEVEL";

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS récupère tous les groupes dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_récupère_tous_les_groupes_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        groupDtos = getGroupRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne tous les groupes$")
    public void le_serveur_retourne_tous_les_groupes() {
        assertThat(groupDtos).isNotNull();

        final int size = groupDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(8);
        for (final GroupDto groupDto : groupDtos) {
            assertThat(groupDto.getCustomerId()).isEqualTo(SYSTEM_CUSTOMER_ID);
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS sans le bon niveau récupère tous les groupes dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_sans_le_bon_niveau_récupère_tous_les_groupes_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        groupDtos = getGoodClient().getAll(getGoodContextWithWrongLevel());
    }

    private GroupExternalRestClient getGoodClient() {
        return getGroupRestClient(true, new Integer[] { proofTenantIdentifier }, new String[] { ROLE_GET_GROUPS });
    }

    private ExternalHttpContext getGoodContextWithWrongLevel() {
        final UserDto user = FactoryDto.buildDto(UserDto.class);
        user.setLevel(WRONG_LEVEL);
        user.setGroupId(TestConstants.TESTS_GROUP_ID);
        testContext.authUserDto = new AuthUserDto(user);
        return getContext(proofTenantIdentifier, tokenUserTest(new String[] { ROLE_GET_GROUPS }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, WRONG_LEVEL));
    }

    @Then("^le serveur ne retourne aucun groupe$")
    public void le_serveur_ne_retourne_aucun_groupe() {
        if (groupDtos != null) {
            assertThat(groupDtos.size()).isEqualTo(0);
        }
        else {
            assertThat(groupDto).isNull();
        }
    }

    @Then("^le serveur ne retourne que le groupe de l'utilisateur$")
    public void le_serveur_ne_retourne_que_le_groupe_de_l_utilisateur() throws Exception {
        assertThat(groupDtos.size()).isEqualTo(1);
        assertThat(groupDtos.get(0).getLevel())
                .overridingErrorMessage(
                        "Le groupe retourné n'a pas le niveau attendu : " + groupDtos.get(0).getLevel() + " au lieu de " + testContext.authUserDto.getLevel())
                .isEqualTo(testContext.authUserDto.getLevel());
        assertThat(groupDtos.get(0).getId()).isEqualTo(testContext.authUserDto.getGroupId());
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération de groupes$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_de_groupes() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère tous les groupes$")
    public void cet_utilisateur_récupère_tous_les_groupes() {
        testContext.level = "";
        try {
            getGroupRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS récupère un groupe par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_récupère_un_groupe_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        groupDto = getGroupRestClient().getOne(getSystemTenantUserAdminContext(), ADMIN_GROUP_ID, Optional.empty());
    }

    @Then("^le serveur retourne le groupe avec cet identifiant$")
    public void le_serveur_retourne_le_groupe_avec_cet_identifiant() {
        assertThat(groupDto.getId()).isEqualTo(ADMIN_GROUP_ID);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS sans le bon niveau récupère un groupe par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_sans_le_bon_niveau_récupère_un_groupe_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        try {
            groupDto = getGoodClient().getOne(getGoodContextWithWrongLevel(), ADMIN_GROUP_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur refuse l'accès pour cause de profil non trouvé")
    public void then_le_serveur_refuse_l_accès_pour_cause_de_profil_non_trouvé() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.NotFoundException: Entity not found fr.gouv.vitamui.iam.internal.server.profile.domain.Profile with id : "
                        + SYSTEM_USER_PROFILE_ID);
    }

    @When("^cet utilisateur récupère un groupe de niveau autorisé par son identifiant$")
    public void cet_utilisateur_récupère_un_groupe_autorise_par_son_identifiant() {
        testContext.level = "";
        try {
            getGroupRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), ADMIN_GROUP_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS récupère tous les groupes avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_récupère_tous_les_groupes_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        final PaginatedValuesDto<GroupDto> groups = getGroupRestClient().getAllPaginated(getSystemTenantUserAdminContext(), 0, 10, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        groupDtos = new ArrayList<>(groups.getValues());
    }

    @Then("^le serveur retourne les groupes paginés$")
    public void le_serveur_retourne_les_groupes_paginés() {
        assertThat(groupDtos).isNotNull().isNotEmpty();
        assertThat(groupDtos.stream().anyMatch(c -> c.getId().equals(ADMIN_GROUP_ID))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_GROUPS sans le bon niveau récupère tous les groupes avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_GROUPS_sans_le_bon_niveau_récupère_tous_les_groupes_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_GROUPS() {
        final PaginatedValuesDto<GroupDto> groups = getGoodClient().getAllPaginated(getGoodContextWithWrongLevel(), 0, 10, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());

        groupDtos = new ArrayList<>(groups.getValues());
    }

    @When("^cet utilisateur récupère tous les groupes avec pagination$")
    public void cet_utilisateur_récupère_tous_les_groupes_avec_pagination() {
        testContext.level = "";
        try {
            getGroupRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getAllPaginated(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
