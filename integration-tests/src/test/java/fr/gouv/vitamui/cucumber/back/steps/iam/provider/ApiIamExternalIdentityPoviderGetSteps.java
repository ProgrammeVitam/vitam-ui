package fr.gouv.vitamui.cucumber.back.steps.iam.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Identity providers dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalIdentityPoviderGetSteps extends CommonSteps {

    private List<IdentityProviderDto> identityProviderDtos;

    private List<String> domains;

    @When("^un utilisateur avec le rôle ROLE_GET_PROVIDERS récupère tous les providers dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROVIDERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROVIDERS_récupère_tous_les_providers_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROVIDERS() {
        identityProviderDtos = getIdentityProviderRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne tous les providers$")
    public void le_serveur_retourne_tous_les_providers() {
        assertThat(identityProviderDtos).isNotNull();
        final int size = identityProviderDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(4);
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération de providers$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_de_providers() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère tous les providers$")
    public void cet_utilisateur_récupère_tous_les_providers() {
        try {
            getIdentityProviderRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROVIDERS récupère un provider par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROVIDERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROVIDERS_récupère_un_provider_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROVIDERS() {
        testContext.identityProviderDto = getIdentityProviderRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.SYSTEM_IDP_ID,
                Optional.empty());
    }

    @Then("^le serveur retourne le provider avec cet identifiant$")
    public void le_serveur_retourne_le_provider_avec_cet_identifiant() {
        assertThat(testContext.identityProviderDto.getId()).isEqualTo(TestConstants.SYSTEM_IDP_ID);
    }

    @When("^cet utilisateur récupère un provider par son identifiant$")
    public void cet_utilisateur_récupère_un_provider_par_son_identifiant() {
        try {
            getIdentityProviderRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), TestConstants.SYSTEM_IDP_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_PROVIDERS récupère tous les providers d'un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_PROVIDERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_PROVIDERS_récupère_tous_les_providers_d_un_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_PROVIDERS() {
        final QueryDto criteria = QueryDto.criteria("customerId", TestConstants.SYSTEM_CUSTOMER_ID, CriterionOperator.EQUALS);

        identityProviderDtos = getIdentityProviderRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());
    }

    @Then("^le serveur retourne tous les providers d'un client$")
    public void le_serveur_retourne_tous_les_providers_d_un_client() {
        assertThat(identityProviderDtos).isNotNull().isNotEmpty();
        assertThat(identityProviderDtos.size()).isGreaterThanOrEqualTo(1);
    }

    @When("^cet utilisateur récupère tous les providers d'un client$")
    public void cet_utilisateur_récupère_tous_les_providers_d_un_client() {
        try {
            final QueryDto criteria = QueryDto.criteria("customerId", TestConstants.SYSTEM_CUSTOMER_ID, CriterionOperator.EQUALS);

            getIdentityProviderRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toOptionalJson(), Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne tous les domaines d'un client$")
    public void le_serveur_retourne_tous_les_domaines_disponibles_d_un_client() {
        assertThat(domains).isNotNull();
    }

    @When("^cet utilisateur récupère tous les domaines d'un client$")
    public void cet_utilisateur_récupère_tous_les_domaines_disponibles_d_un_client() {
        try {
            final QueryDto criteria = QueryDto.criteria("customerId", TestConstants.SYSTEM_CUSTOMER_ID, CriterionOperator.EQUALS);

            final List<IdentityProviderDto> idps = getIdentityProviderRestClient(testContext.fullAccess, testContext.certificateTenants,
                    testContext.certificateRoles).getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser), criteria.toOptionalJson(),
                            Optional.empty());

            domains = idps.stream().flatMap(i -> i.getPatterns().stream().map(s -> s.replace(".*@", ""))).collect(Collectors.toList());

        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
