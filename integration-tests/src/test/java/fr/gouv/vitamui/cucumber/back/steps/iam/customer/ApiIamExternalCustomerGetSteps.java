package fr.gouv.vitamui.cucumber.back.steps.iam.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Customers dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalCustomerGetSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_GET_CUSTOMERS récupère tous les clients dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_tous_les_clients_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        basicCustomerDtos = getCustomerRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne tous les clients$")
    public void le_serveur_retourne_tous_les_clients() {
        assertThat(basicCustomerDtos).isNotNull();

        final int size = basicCustomerDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(3);
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération de clients$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_de_clients() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère tous les clients$")
    public void cet_utilisateur_récupère_tous_les_clients() {
        try {
            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser));
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_CUSTOMERS récupère un client par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_un_client_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        testContext.basicCustomerDto = getCustomerRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.SYSTEM_CUSTOMER_ID, Optional.empty());
    }

    @Then("^le serveur retourne le client avec cet identifiant$")
    public void le_serveur_retourne_le_client_avec_cet_identifiant() {
        assertThat(testContext.basicCustomerDto.getId()).isEqualTo(TestConstants.SYSTEM_CUSTOMER_ID);
    }

    @When("^cet utilisateur récupère un client par son identifiant$")
    public void cet_utilisateur_récupère_un_client_par_son_identifiant() {
        try {
            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), TestConstants.SYSTEM_CUSTOMER_ID, Optional.empty());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur sans le rôle ROLE_GET_CUSTOMERS récupère son client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_sans_le_rôle_ROLE_GET_CUSTOMERS_récupère_son_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        testContext.basicCustomerDto = getCustomerRestClient().getMyCustomer(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS));
    }

    @Then("^le serveur retourne le client de l'utilisateur courant$")
    public void le_serveur_retourne_le_client_de_l_utilisateur_courant() {
        assertThat(testContext.basicCustomerDto.getId()).isEqualTo(TestConstants.SYSTEM_CUSTOMER_ID);
    }

    @When("^un utilisateur avec le rôle ROLE_GET_CUSTOMERS récupère tous les clients par code et nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_tous_les_clients_par_code_et_nom_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        QueryDto criteria = QueryDto.criteria("code", TestConstants.SYSTEM_CUSTOMER_CODE, CriterionOperator.EQUALS).addCriterion("name",
                TestConstants.SYSTEM_CUSTOMER_ID, CriterionOperator.EQUALS);
        basicCustomerDtos = getCustomerRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @When("^cet utilisateur récupère tous les clients par code et nom$")
    public void cet_utilisateur_récupère_tous_les_clients_par_code_et_nom() {
        try {
            QueryDto criteria = QueryDto.criteria("code", TestConstants.SYSTEM_CUSTOMER_CODE, CriterionOperator.EQUALS).addCriterion("name",
                    TestConstants.SYSTEM_CUSTOMER_ID, CriterionOperator.EQUALS);
            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toOptionalJson(), Optional.empty());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_CUSTOMERS récupère tous les clients par code ou nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_tous_les_clients_par_code_ou_nom_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        QueryDto criteria = QueryDto.criteria("code", testContext.basicCustomerDto.getCode(), CriterionOperator.EQUALS);
        basicCustomerDtos = getCustomerRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @When("^cet utilisateur récupère tous les clients avec pagination$")
    public void cet_utilisateur_récupère_tous_les_clients_avec_pagination() {
        try {
            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getAllPaginated(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^cet utilisateur récupère tous les clients par code ou nom$")
    public void cet_utilisateur_récupère_tous_les_clients_par_code_ou_nom() {
        try {
            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getAllPaginated(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser), 0, 10, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty());
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne les clients par code et nom$")
    public void le_serveur_retourne_les_clients_par_code_et_nom() {
        assertThat(basicCustomerDtos).isNotNull().isNotEmpty();
        assertThat(basicCustomerDtos.stream().anyMatch(c -> c.getId().equals(TestConstants.SYSTEM_CUSTOMER_ID))).isTrue();
    }

    @Then("^le serveur retourne les clients par code ou nom$")
    public void le_serveur_retourne_les_clients_par_code_ou_nom() {
        assertThat(basicCustomerDtos).isNotNull().isNotEmpty().hasSize(1);

        assertThat(basicCustomerDtos.get(0).getId()).isEqualTo(testContext.basicCustomerDto.getId());
    }

    @When("^un utilisateur avec le rôle ROLE_GET_CUSTOMERS récupère tous les clients avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_CUSTOMERS_récupère_tous_les_clients_avec_pagination_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_CUSTOMERS() {
        final PaginatedValuesDto<CustomerDto> customers = getCustomerRestClient().getAllPaginated(getSystemTenantUserAdminContext(), 0, 10, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        basicCustomerDtos = new ArrayList<>(customers.getValues());
    }

    @Then("^le serveur retourne les clients paginés$")
    public void le_serveur_retourne_les_clients_paginés() {
        le_serveur_retourne_les_clients_par_code_et_nom();
    }
}
