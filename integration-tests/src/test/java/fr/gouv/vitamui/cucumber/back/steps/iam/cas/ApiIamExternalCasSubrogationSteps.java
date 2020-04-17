package fr.gouv.vitamui.cucumber.back.steps.iam.cas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API CAS dans IAM admin : opérations liées à la subrogation.
 *
 *
 */
public class ApiIamExternalCasSubrogationSteps extends CommonSteps {

    private List<SubrogationDto> subrogationDtos;

    @When("^un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation par l'email de son superuser dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_SUBROGATIONS_cherche_une_subrogation_par_l_email_de_son_superuser_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_SUBROGATIONS() {
        // Get or Initialize defaultSubrogation before requesting superUser
        getOrInitializeDefaultSubrogationId();
        subrogationDtos = getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_SUBROGATIONS })
                .getSubrogationsBySuperUserEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        TestConstants.JULIEN_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
    }

    @Then("^le serveur retourne la bonne subrogation$")
    public void le_serveur_retourne_la_bonne_subrogation() {
        assertThat(subrogationDtos).isNotNull().isNotEmpty();
        assertThat(subrogationDtos.size()).isEqualTo(1);
        assertThat(subrogationDtos.get(0).getId()).isEqualTo("juliensurrogatespierre");
    }

    @Given("^deux tenants et un rôle par défaut pour chercher une subrogation par l'email de son superuser$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_chercher_une_subrogation_par_l_email_de_son_superuser() {
        setMainTenant(casTenantIdentifier);
        setSecondTenant(proofTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur cherche une subrogation par l'email de son superuser$")
    public void cet_utilisateur_cherche_une_subrogation_par_l_email_de_son_superuser() {
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles).getSubrogationsBySuperUserEmail(
                    getContext(testContext.tenantIHMContext, testContext.tokenUser),
                    TestConstants.JULIEN_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation par l'identifiant de son superuser dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_SUBROGATIONS_cherche_une_subrogation_par_l_identifiant_de_son_superuser_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_SUBROGATIONS() {
        final Integer[] tenants = new Integer[] { casTenantIdentifier };
        final String[] roles = new String[] { ServicesData.ROLE_CAS_SUBROGATIONS };
        // Get or Initialize defaultSubrogation before requesting superUser
        getOrInitializeDefaultSubrogationId();
        subrogationDtos = getCasRestClient(false, tenants, roles)
                .getSubrogationsBySuperUserId(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), TestConstants.JULIEN_USER_ID);
    }

    @Given("^deux tenants et un rôle par défaut pour chercher une subrogation par l'identifiant de son superuser$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_chercher_une_subrogation_par_l_identifiant_de_son_superuser() {
        deux_tenants_et_un_rôle_par_défaut_pour_chercher_une_subrogation_par_l_email_de_son_superuser();
    }

    @When("^cet utilisateur cherche une subrogation par l'identifiant de son superuser$")
    public void cet_utilisateur_cherche_une_subrogation_par_l_identifiant_de_son_superuser() {
        try {
            getCasRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getSubrogationsBySuperUserId(getContext(testContext.tenantIHMContext, testContext.tokenUser), TestConstants.JULIEN_USER_ID);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation avec un mauvais identifiant de superuser dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_SUBROGATIONS_cherche_une_subrogation_avec_un_mauvais_identifiant_de_superuser_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_SUBROGATIONS() {
        try {
            getCasRestClient(false, new Integer[] { casTenantIdentifier }, new String[] { ServicesData.ROLE_CAS_SUBROGATIONS })
                    .getSubrogationsBySuperUserId(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), TestConstants.BAD_LOGIN);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Then("^le serveur ne retourne aucune subrogation$")
    public void le_serveur_ne_retourne_aucune_subrogation() {
        assertThat(subrogationDtos).isNotNull().isEmpty();
    }

    @When("^un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation par l'identifiant d'un superuser désactivé dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_CAS_SUBROGATIONS_cherche_une_subrogation_par_l_identifiant_d_un_superuser_désactivé_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_sur_le_tenant_et_avec_le_rôle_ROLE_CAS_SUBROGATIONS() {
        createSubrogationByUserStatus(true);
        final Integer[] tenants = new Integer[] { casTenantIdentifier };
        final String[] roles = new String[] { ServicesData.ROLE_CAS_SUBROGATIONS };
        subrogationDtos = getCasRestClient(false, tenants, roles)
                .getSubrogationsBySuperUserId(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS), superUser.getId());
    }
}
