package fr.gouv.vitamui.cucumber.back.steps.iam.user;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_GET_USERS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_LOGBOOKS;
import static fr.gouv.vitamui.utils.TestConstants.CLIENT1_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.LevelParameterType;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API Users dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamUserGetSteps extends CommonSteps {

    private Map<String, String> existingUsers;

    private Collection<UserDto> paginatedUsers;

    private UserDto userDto;

    @When("^un utilisateur avec le rôle ROLE_GET_USERS récupère tous les utilisateurs dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_récupère_tous_les_utilisateurs_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        try {
            getUserRestClient().getAll(getSystemTenantUserAdminContext());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur renvoie une erreur car la récupération de tous les utilisateurs n'est pas possible$")
    public void le_serveur_renvoie_une_erreur_car_la_récupération_de_tous_les_utilisateurs_n_est_pas_possible() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Invalid Request : Parameter conditions \"page, size\" not met for actual request parameters: .");
    }

    @When("^un utilisateur avec le rôle ROLE_GET_USERS récupère un utilisateur par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_récupère_un_utilisateur_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        userDto = getUserRestClient().getOne(getSystemTenantUserAdminContext(), SYSTEM_USER_ID, Optional.empty());
    }

    @Then("^le serveur retourne l'utilisateur avec cet identifiant$")
    public void le_serveur_retourne_l_utilisateur_avec_cet_identifiant() {
        assertThat(userDto.getId()).isEqualTo(SYSTEM_USER_ID);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_USERS récupère un utilisateur d'un autre client par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_récupère_un_utilisateur_d_un_autre_client_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        try {
            userDto = getUserRestClient(true, new Integer[] { proofTenantIdentifier }, new String[] { ROLE_GET_USERS }).getOne(
                    getContext(proofTenantIdentifier, tokenUserTest(new String[] { ROLE_GET_USERS }, proofTenantIdentifier, CLIENT1_CUSTOMER_ID, "")),
                    SYSTEM_USER_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur ne renvoie aucun utilisateur$")
    public void le_serveur_ne_renvoie_aucun_utilisateur() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception.toString()).isEqualTo(
                "fr.gouv.vitamui.commons.api.exception.NotFoundException: Entity not found fr.gouv.vitamui.iam.internal.server.user.domain.User with id : "
                        + SYSTEM_USER_ID);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_USERS sans le bon niveau récupère un utilisateur par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_sans_le_bon_niveau_récupère_un_utilisateur_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        try {
            userDto = getUserRestClient(true, new Integer[] { proofTenantIdentifier }, new String[] { ROLE_GET_USERS }).getOne(
                    getContext(proofTenantIdentifier,
                            tokenUserTest(new String[] { ROLE_GET_USERS }, proofTenantIdentifier, SYSTEM_CUSTOMER_ID, "WRONGLEVEL")),
                    SYSTEM_USER_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération d'utilisateurs$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_d_utilisateurs() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur récupère un utilisateur par son identifiant$")
    public void cet_utilisateur_récupère_un_utilisateur_par_son_identifiant() {
        testContext.level = "";
        try {
            getUserRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), SYSTEM_USER_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_USERS récupère tous les utilisateurs avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_récupère_tous_les_utilisateurs_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        paginatedUsers = getUserRestClient()
                .getAllPaginated(getSystemTenantUserAdminContext(), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).getValues();
    }

    @Then("^le serveur retourne les utilisateurs paginés$")
    public void le_serveur_retourne_les_utilisateurs_paginés() {
        assertThat(paginatedUsers).isNotNull().isNotEmpty();
        assertThat(paginatedUsers.stream().anyMatch(c -> c.getId().equals(SYSTEM_USER_ID))).isTrue();
    }

    @When("^un utilisateur avec le rôle ROLE_GET_USERS récupère tous les utilisateurs d'un autre client avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_USERS_récupère_tous_les_utilisateurs_d_un_autre_client_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_USERS() {
        paginatedUsers = getUserRestClient(true, new Integer[] { proofTenantIdentifier }, new String[] { ROLE_GET_USERS }).getAllPaginated(
                getContext(proofTenantIdentifier, tokenUserTest(new String[] { ROLE_GET_USERS }, proofTenantIdentifier, CLIENT1_CUSTOMER_ID, "")), 0, 10,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).getValues();
    }

    @Then("^le serveur retourne les utilisateurs paginés du bon client$")
    public void le_serveur_retourne_les_utilisateurs_paginés_du_bon_client() {
        assertThat(paginatedUsers).isNotNull().isNotEmpty();
        assertThat(paginatedUsers.stream().noneMatch(c -> c.getId().equals(SYSTEM_USER_ID))).isTrue();
    }

    @When("^cet utilisateur récupère tous les utilisateurs avec pagination$")
    public void cet_utilisateur_récupère_tous_les_utilisateurs_avec_pagination() {
        testContext.level = "";
        try {
            getUserRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getAllPaginated(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^il existe plusieurs utilisateurs de différents niveaux$")
    public void il_existe_plusieurs_utilisateurs_de_différents_niveaux(final List<List<String>> levels) throws Exception {
        existingUsers = new HashMap<>();
        //final List<String> levelsList = levels.asLists().get(0);
        for (final List<String> levelsList : levels) {
            for (final String level : levelsList) {
                final LevelParameterType levelParameterType = new LevelParameterType(level);
                final String convertedLevel = levelParameterType.getData();
                tokenUser(new String[]{ServicesData.ROLE_GET_USERS}, testContext.customerId, "level" + convertedLevel + "@test.com", convertedLevel,
                        testContext.mainTenant, "idExistingUser" + convertedLevel);
                existingUsers.put(convertedLevel, "idExistingUser" + convertedLevel);
            }
        }

    }

    @When("cet utilisateur récupère les utilisateurs de niveau {level}")
    public void cet_utilisateur_récupère_les_utilisateurs_de_niveau_TEST(final LevelParameterType level) throws Exception {
        try {
            paginatedUsers = getAllPaginatedCustom(Optional.of(level.getData()));
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("la liste renvoyée par le serveur ne contient pas l'utilisateur de niveau {level}")
    public void la_liste_renvoyée_par_le_serveur_ne_contient_pas_l_utilisateur_de_niveau_vide(final LevelParameterType level) throws Exception {
        assertThat(paginatedUsers.stream().filter(user -> user.getId().equals(existingUsers.get(level.getData()))).findFirst())
                .overridingErrorMessage("La liste des utilisateurs récupérés contient l'utilisateur de niveau " + level.getData()).isEmpty();
    }

    @Then("la liste renvoyée par le serveur contient l'utilisateur de niveau {level}")
    public void la_liste_renvoyée_par_le_serveur_contient_l_utilisateur_de_niveau_TEST(final LevelParameterType level) throws Exception {
        assertThat(paginatedUsers.stream().filter(user -> user.getId().equals(existingUsers.get(level.getData()))).findFirst())
                .overridingErrorMessage("La liste des utilisateurs récupérés ne contient pas l'utilisateur de niveau " + level.getData()).isNotEmpty();
    }

    @Then("^la liste renvoyée par le serveur contient l'utilisateur ayant effectué l'action de récupération$")
    public void la_liste_renvoyée_par_le_serveur_contient_l_utilisateur_ayant_effectué_l_action_de_récupération() throws Exception {
        assertThat(paginatedUsers.stream().filter(user -> user.getId().equals(TestConstants.TESTS_USER_ID)).findFirst())
                .overridingErrorMessage("La liste des utilisateurs récupérés ne contient pas l'utilisateur ayant effectué l'action getAllPaginated")
                .isNotEmpty();
    }

    @Then("^la liste renvoyée par le serveur ne contient pas l'utilisateur ayant effectué l'action de récupération$")
    public void la_liste_renvoyée_par_le_serveur_ne_contient_pas_l_utilisateur_ayant_effectué_l_action_de_récupération() throws Exception {
        assertThat(paginatedUsers.stream().filter(user -> user.getId().equals(TestConstants.TESTS_USER_ID)).findFirst())
                .overridingErrorMessage("La liste des utilisateurs récupérés contient l'utilisateur ayant effectué l'action getAllPaginated").isEmpty();
    }

    @When("^cet utilisateur récupère tous les utilisateurs$")
    public void cet_utilisateur_récupère_tous_les_utilisateurs() throws Exception {
        try {
            paginatedUsers = getAllPaginatedCustom(Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    protected Collection<UserDto> getAllPaginatedCustom(final Optional<String> level) {
        final QueryDto criteria = QueryDto.criteria();
        level.ifPresent(l -> criteria.addCriterion("level", l, CriterionOperator.EQUALS));
        return getUserRestClient().getAllPaginated(getContext(testContext.mainTenant, testContext.tokenUser), 0, Integer.MAX_VALUE,
                Optional.of(criteria.toJson()), Optional.empty(), Optional.empty(), Optional.empty()).getValues();
    }
}
