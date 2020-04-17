package fr.gouv.vitamui.cucumber.back.steps.iam.cas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Optional;

import org.bson.Document;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API CAS dans IAM admin : opérations liées au logout.
 *
 *
 */
public class ApiIamExternalCasLogoutSteps extends CommonSteps {

    @When("^un utilisateur avec le rôle ROLE_CAS_LOGOUT fait un logout dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGOUT$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_LOGOUT_fait_un_logout_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_LOGOUT() {
        createSubrogationByUserStatus(false);
        surrogateUser = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        surrogateUser.getEmail(), Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
        getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_LOGOUT })
                .logout(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), surrogateUser.getAuthToken(), superUser.getEmail());
    }

    @Then("^la subrogation a bien été supprimée$")
    public void la_subrogation_a_bien_été_supprimée() {
        try {
            getSubrogationRestClient().getOne(getSystemTenantUserAdminContext(), subrogationDto.getId(), Optional.empty());
            fail("should throw a NotFoundException");
        }
        catch (final NotFoundException e) {
        }
    }

    @Then("^le token d'authentification a bien été supprimé$")
    public void le_token_d_authentification_a_bien_été_supprimé() {
        final Document authTokenId = new Document("_id", surrogateUser.getAuthToken());
        final long nb = getTokensCollection().countDocuments(authTokenId);
        assertThat(nb).isEqualTo(0);
    }

    @Given("^deux tenants et un rôle par défaut pour faire un logout$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_faire_un_logout() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur fait un logout$")
    public void cet_utilisateur_fait_un_logout() {
        createSubrogationByUserStatus(false);
        surrogateUser = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        surrogateUser.getEmail(), Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .logout(getContext(testContext.tenantIHMContext, testContext.tokenUser), surrogateUser.getAuthToken(), superUser.getEmail());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
