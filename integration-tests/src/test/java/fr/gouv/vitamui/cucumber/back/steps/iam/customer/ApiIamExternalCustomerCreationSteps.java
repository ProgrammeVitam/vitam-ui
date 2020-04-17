package fr.gouv.vitamui.cucumber.back.steps.iam.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.utils.ResourcesUtils;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Customers dans IAM admin : opérations de création.
 *
 *
 */

public class ApiIamExternalCustomerCreationSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_CUSTOMERS_ajoute_un_nouveau_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_CUSTOMERS() {
        testContext.savedBasicCustomerDto = FactoryDto.buildDto(CustomerDto.class);
        testContext.basicCustomerDto = create(getSystemTenantUserAdminContext(), testContext.savedBasicCustomerDto, Optional.empty());
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client avec son logo dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_CUSTOMERS_ajoute_un_nouveau_client_avec_son_logo_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_CUSTOMERS()
            throws IOException {
        testContext.savedBasicCustomerDto = FactoryDto.buildDto(CustomerDto.class);
        testContext.basicCustomerDto = create(getSystemTenantUserAdminContext(), testContext.savedBasicCustomerDto,
                Optional.of(ResourcesUtils.getResourcePath("data/vitamui-logo.png")));
    }

    @When("^un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client avec un thème personnalisé dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_CUSTOMERS_ajoute_un_nouveau_client_avec_un_thème_personnalisé_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_CREATE_CUSTOMERS()
        throws IOException {
        testContext.savedBasicCustomerDto = FactoryDto.buildDto(CustomerDto.class);

        HashMap<String, String> themeColors = new HashMap<>();
        themeColors.put("vitamui-primary", "#123456");
        themeColors.put("vitamui-secondary", "#654321");
        testContext.savedBasicCustomerDto.setHasCustomGraphicIdentity(true);
        testContext.savedBasicCustomerDto.setThemeColors(themeColors);

        testContext.basicCustomerDto = create(getSystemTenantUserAdminContext(), testContext.savedBasicCustomerDto,
            Optional.of(ResourcesUtils.getResourcePath("data/vitamui-logo.png")));
    }


    public CustomerDto create(final ExternalHttpContext context, final CustomerDto customerDto, final Optional<Path> logoPath) {
        final IamExternalWebClientFactory iamExternalWebClientFactory = getIamWebClientFactory(true, null, new String[] { ServicesData.ROLE_CREATE_CUSTOMERS });

        LOGGER.debug("Create {} with logo : {}", customerDto, logoPath);

        return iamExternalWebClientFactory.getCustomerWebClient().create(context, customerDto, logoPath);
    }

    @Then("^le serveur retourne le client créé$")
    public void le_status_de_la_réponse_doit_etre() {
        assertThat(testContext.basicCustomerDto).overridingErrorMessage("la réponse retournée est null").isNotNull();
    }

    @Then("^un identity provider par défaut est associé au client$")
    public void un_identity_provider_par_défaut_est_associé_au_client() {
        final QueryDto criteria = QueryDto.criteria("customerId", testContext.basicCustomerDto.getId(), CriterionOperator.EQUALS);
        final List<IdentityProviderDto> identityProviders = getIdentityProviderRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(),
                Optional.empty());

        assertThat(identityProviders).overridingErrorMessage("Aucun identity provider trouvé pour le customer %s", testContext.basicCustomerDto.getId())
                .isNotNull().isNotEmpty();
    }

    @Then("^un tenant par défaut est créé$")
    public void un_tenant_par_défaut_est_créé() {
        final QueryDto criteria = QueryDto.criteria("name", testContext.savedBasicCustomerDto.getOwners().get(0).getName(), CriterionOperator.EQUALS);

        final List<TenantDto> tenants = getTenantRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty());

        assertThat(tenants).overridingErrorMessage("Aucun tenant trouvé pour le customer %s", testContext.basicCustomerDto.getId()).isNotNull().isNotEmpty()
                .hasSize(1);
        testContext.tenantDto = tenants.get(0);
    }

    @Then("^(.*) profils sont créés pour le tenant principal$")
    public void des_profils_sont_créés_pour_le_tenant_principal(final Integer profilesNumber) {
        final Integer tenantIdentifier = testContext.tenantDto.getIdentifier();
        final Collection<ProfileDto> profiles = getProfileRestClient().getAll(getContext(tenantIdentifier,
                tokenUserTest(new String[] { ServicesData.ROLE_GET_PROFILES }, tenantIdentifier, testContext.tenantDto.getCustomerId(), testContext.level)),
                Optional.empty(), Optional.empty());
        assertThat(profiles).isNotNull().isNotEmpty();
        assertThat(profiles.size()).isEqualTo(profilesNumber);
    }

    @Then("^un utilisateur admin est associé au client$")
    public void un_utilisateur_admin_est_associé_au_client() {
        final UserDto userAdmin = getUser("admin@", testContext.basicCustomerDto.getId());

        assertThat(userAdmin).overridingErrorMessage("Aucun utilisateur admin trouvé pour le customer %s", testContext.basicCustomerDto.getId()).isNotNull();

        checkGroupAndProfiles(userAdmin.getGroupId());
    }

    protected UserDto getUser(final String emailPrefix, final String customerId) {
        final PaginatedValuesDto<UserDto> users = getUserRestClient().getAllPaginated(getSystemTenantUserAdminContext(), 0, 100, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        assertThat(users).overridingErrorMessage("Aucun utilisateur trouvé pour le customer %s", customerId).isNotNull();
        assertThat(users.getValues()).overridingErrorMessage("Aucun utilisateur trouvé pour le customer %s", customerId).isNotNull().isNotEmpty();

        return users.getValues().stream().filter(u -> u.getEmail().startsWith(emailPrefix)).findFirst().orElse(null);
    }

    protected void checkGroupAndProfiles(final String profileGroupId) {
        final GroupDto group = getGroupRestClient().getOne(getSystemTenantUserAdminContext(), profileGroupId, Optional.empty());

        assertThat(group).overridingErrorMessage("Aucun goupe de profil trouvé pour l'identifiant %s", profileGroupId).isNotNull();

        final List<String> profileIds = group.getProfileIds();
        for (final String profileId : profileIds) {
            final ProfileDto profile = getProfileRestClient().getOne(getSystemTenantUserAdminContext(), profileId, Optional.empty());

            assertThat(profile).overridingErrorMessage("Aucun profil trouvé pour l'identifiant %s", profileId).isNotNull();
        }
    }

    @Given("^deux tenants et un rôle par défaut pour l'ajout d'un client")
    public void deux_tenants_et_un_rôle_par_défaut_pour_l_ajout_d_un_client() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
        testContext.level = "";
    }

    @When("^cet utilisateur ajoute un nouveau client")
    public void cet_utilisateur_ajoute_un_nouveau_client() {
        final CustomerDto dto = FactoryDto.buildDto(CustomerDto.class);
        try {
            final IamExternalWebClientFactory iamExternalWebClientFactory = getIamWebClientFactory(testContext.fullAccess, testContext.certificateTenants,
                    testContext.certificateRoles);

            iamExternalWebClientFactory.getCustomerWebClient().create(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^une trace de création d'un client est présente dans vitam$")
    public void une_trace_de_création_d_un_client_est_présente_dans_vitam() throws InterruptedException {
        super.testTrace(TestConstants.SYSTEM_CUSTOMER_ID, testContext.basicCustomerDto.getIdentifier(), "customers", "EXT_VITAMUI_CREATE_CUSTOMER");
    }
}
