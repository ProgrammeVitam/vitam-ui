package fr.gouv.vitamui.cucumber.back.steps.iam.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Customers dans IAM admin : opérations de mise à jour.
 *
 *
 */
public class ApiIamExternalCustomerUpdateSteps extends CommonSteps {

    @Given("^un client a été créé sans être modifiable$")
    public void un_client_a_été_créé_sans_être_modifiable() {
        final CustomerDto dto = FactoryDto.buildDto(CustomerDto.class);
        dto.setReadonly(true);
        testContext.basicCustomerDto = getCustomerRestClient().create(getSystemTenantUserAdminContext(), dto);
        testContext.savedBasicCustomerDto = testContext.basicCustomerDto;
    }

    private CustomerDto buildClientToUpdate() {
        final CustomerDto dto = FactoryDto.buildDto(CustomerDto.class);
        dto.setId(testContext.basicCustomerDto.getId());
        dto.setIdentifier("identifier");
        dto.setCode(testContext.savedBasicCustomerDto.getCode());
        dto.setCompanyName(TestConstants.UPDATED + testContext.savedBasicCustomerDto.getCompanyName());
        dto.setDefaultEmailDomain(testContext.savedBasicCustomerDto.getDefaultEmailDomain());
        dto.setEmailDomains(testContext.savedBasicCustomerDto.getEmailDomains());
        return dto;
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS met à jour un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_CUSTOMERS_met_à_jour_un_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_CUSTOMERS() {
        final CustomerDto dto = buildClientToUpdate();
        try {
            testContext.basicCustomerDto = getCustomerRestClient().update(getSystemTenantUserAdminContext(), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur retourne le client mis à jour$")
    public void le_serveur_retourne_le_client_mis_à_jour() {
        assertThat(testContext.basicCustomerDto.getName()).isNotEqualTo(testContext.savedBasicCustomerDto.getName());
        assertThat(testContext.basicCustomerDto.getCompanyName())
                .isEqualTo(TestConstants.UPDATED + testContext.savedBasicCustomerDto.getCompanyName());
    }

    @Then("^le serveur refuse la mise à jour du client")
    public void le_serveur_refuse_la_mise_à_jour() {
        assertThat(testContext.exception).isNotNull();
        assertThat(testContext.exception).isInstanceOf(InvalidFormatException.class);
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS rend le client subrogeable dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_CUSTOMERS_rend_le_client_subrogeable_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_CUSTOMERS() {
        final CustomerDto dto = buildClientToUpdate();
        dto.setSubrogeable(false);
        testContext.basicCustomerDto = getCustomerRestClient().update(getSystemTenantUserAdminContext(), dto);
    }

    @Then("^le serveur retourne le client mis à jour sur les propriétés subrogeable$")
    public void le_serveur_retourne_le_client_non_mis_à_jour_sur_les_propriétés_subrogeable() {
        assertThat(testContext.basicCustomerDto.getName()).isNotEqualTo(testContext.savedBasicCustomerDto.getName());
        assertThat(testContext.basicCustomerDto.getCompanyName())
                .isEqualTo(TestConstants.UPDATED + testContext.savedBasicCustomerDto.getCompanyName());
        assertThat(testContext.basicCustomerDto.isSubrogeable()).isEqualTo(false);
    }

    @When("^un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS désactive l'OTP d'un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_UPDATE_CUSTOMERS_désactive_l_OTP_d_un_client_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_UPDATE_CUSTOMERS() {
        final CustomerDto dto = buildClientToUpdate();
        dto.setOtp(OtpEnum.DISABLED);
        testContext.basicCustomerDto = getCustomerRestClient().update(getSystemTenantUserAdminContext(), dto);
    }

    @Then("^le serveur retourne le client avec l'OTP désactivé$")
    public void le_serveur_retourne_le_client_avec_l_OTP_désactivé() {
        assertThat(testContext.basicCustomerDto.getName()).isNotEqualTo(testContext.savedBasicCustomerDto.getName());
        assertThat(testContext.basicCustomerDto.getCompanyName())
                .isEqualTo(TestConstants.UPDATED + testContext.savedBasicCustomerDto.getCompanyName());
        assertThat(testContext.basicCustomerDto.getOtp()).isEqualTo(OtpEnum.DISABLED);
    }

    @Then("^les utilisateurs du client ont leur OTP désactivé$")
    public void les_utilisateurs_du_client_ont_leur_OTP_désactivé() {
        final String adminEmail = "admin@" + testContext.basicCustomerDto.getDefaultEmailDomain();
        final AuthUserDto adminUser = (AuthUserDto) getCasRestClient(false,
                new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_USERS })
                        .getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                                adminEmail, Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));

        QueryDto criteria = QueryDto.criteria("name",
                testContext.savedBasicCustomerDto.getOwners().get(0).getName(), CriterionOperator.EQUALS);
        final TenantDto customerTenant = getTenantRestClient()
                .getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty()).get(0);

        final ExternalHttpContext customerAdminContext = getContext(customerTenant.getIdentifier(),
                adminUser.getAuthToken());

        final PaginatedValuesDto<UserDto> users = getUserRestClient().getAllPaginated(customerAdminContext, 0, 100,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertThat(users).isNotNull();
        assertThat(users.getValues()).isNotNull().isNotEmpty();

        for (final UserDto user : users.getValues()) {
            final boolean otp = user.isOtp();

            assertThat(otp).isFalse();
        }
    }

    @Given("^deux tenants et un rôle par défaut pour la mise à jour d'un client$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_mise_à_jour_d_un_client() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur met à jour un client$")
    public void cet_utilisateur_met_à_jour_un_client() {
        final CustomerDto dto = buildClientToUpdate();
        try {
            getCustomerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .update(getContext(testContext.tenantIHMContext, testContext.tokenUser), dto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
