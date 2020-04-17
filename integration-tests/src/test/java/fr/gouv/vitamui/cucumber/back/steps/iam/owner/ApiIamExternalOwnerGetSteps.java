package fr.gouv.vitamui.cucumber.back.steps.iam.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Owners dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalOwnerGetSteps extends CommonSteps {

    @When("^un utilisateur récupère tous les propriétaires$")
    public void un_utilisateur_récupère_tous_les_propriétaires() {
        try {
            getOwnerRestClient().getAll(getSystemTenantUserAdminContext());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_OWNERS récupère un propriétaire par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_OWNERS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_OWNERS_récupère_un_propriétaire_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_OWNERS() {
        testContext.ownerDto = getOwnerRestClient().getOne(getSystemTenantUserAdminContext(), TestConstants.SYSTEM_OWNER_ID, Optional.empty());
    }

    @Then("^le serveur retourne le propriétaire avec cet identifiant$")
    public void le_serveur_retourne_le_propriétaire_avec_cet_identifiant() {
        assertThat(testContext.ownerDto.getId()).isEqualTo(TestConstants.SYSTEM_OWNER_ID);
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération d'un propriétaire par son identifant$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_d_un_propriétaire_par_son_identifant() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère un propriétaire par son identifiant$")
    public void cet_utilisateur_récupère_un_propriétaire_par_son_identifiant() {
        try {
            getOwnerRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), TestConstants.SYSTEM_OWNER_ID, Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
