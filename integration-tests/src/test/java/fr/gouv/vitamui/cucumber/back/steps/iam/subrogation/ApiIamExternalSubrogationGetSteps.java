package fr.gouv.vitamui.cucumber.back.steps.iam.subrogation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API subrogations dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalSubrogationGetSteps extends CommonSteps {

    @When("^cet utilisateur récupère toutes les subrogations$")
    public void cet_utilisateur_récupère_toutes_les_subrogations() {
        try {
            getSubrogationRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getAll(getContext(testContext.tenantIHMContext, testContext.tokenUser));
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur avec le rôle ROLE_GET_SUBROGATIONS récupère une subrogation par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_SUBROGATIONS$")
    public void un_utilisateur_avec_le_rôle_ROLE_GET_SUBROGATIONS_récupère_une_subrogation_par_son_identifiant_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_GET_SUBROGATIONS() {
        subrogationDto = getSubrogationRestClient().getOne(getSystemTenantUserAdminContext(), getOrInitializeDefaultSubrogationId(), Optional.empty());
    }

    @Then("^le serveur retourne la subrogation avec cet identifiant$")
    public void le_serveur_retourne_la_subrogation_avec_cet_identifiant() {
        assertThat(subrogationDto.getId()).isEqualTo(getOrInitializeDefaultSubrogationId());
    }

    @Given("^deux tenants et un rôle par défaut pour la récupération d'une subrogation par son identifiant$")
    public void deux_tenants_et_un_rôle_par_défaut_pour_la_récupération_d_une_subrogation_par_son_identifiant() {
        setMainTenant(proofTenantIdentifier);
        setSecondTenant(casTenantIdentifier);
        testContext.defaultRole = ServicesData.ROLE_LOGBOOKS;
    }

    @When("^cet utilisateur récupère une subrogation par son identifiant$")
    public void cet_utilisateur_récupère_une_subrogation_par_son_identifiant() {
        try {
            getSubrogationRestClient(testContext.fullAccess, testContext.certificateTenants, testContext.certificateRoles)
                    .getOne(getContext(testContext.tenantIHMContext, testContext.tokenUser), getOrInitializeDefaultSubrogationId(), Optional.empty());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @Given("^une subrogation existe pour moi en tant que subrogé$")
    public void une_subrogation_existe_pour_moi_en_tant_que_subrogé() {
        subrogationDto = buildGoodSubrogation();
        deleteAllSubrogations(subrogationDto);
        testContext.savedSubrogationDto = getSubrogationRestClient().create(getSystemTenantUserAdminContext(), subrogationDto);
        testContext.authUserDto = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        subrogationDto.getSurrogate(), Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
    }

    @When("^je demande ma subrogation courante en tant que subrogé$")
    public void je_demande_ma_subrogation_courante_en_tant_que_subrogé() {
        subrogationDto = getSubrogationRestClient().getMySubrogationAsSurrogate(
                getContext(testContext.tenantDto != null ? testContext.tenantDto.getIdentifier() : proofTenantIdentifier,
                        testContext.authUserDto.getAuthToken()));
    }

    @Then("^le serveur retourne la subrogation qui existe pour moi en tant que subrogé$")
    public void le_serveur_retourne_la_subrogation_qui_existe_pour_moi_en_tant_que_subrogé() {
        assertThat(subrogationDto.getId()).isEqualTo(testContext.savedSubrogationDto.getId());
    }

    @Given("^une subrogation existe pour moi en tant que subrogateur$")
    public void une_subrogation_existe_pour_moi_en_tant_que_subrogateur() {
        buildSubrogation(true, true, null, UserStatusEnum.ENABLED);
        testContext.savedSubrogationDto = subrogationDto;
        testContext.authUserDto = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        subrogationDto.getSuperUser(), Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
    }

    @When("^je demande ma subrogation courante en tant que subrogateur$")
    public void je_demande_ma_subrogation_courante_en_tant_que_subrogateur() {
        subrogationDto = getSubrogationRestClient().getMySubrogationAsSuperuser(
                getContext(testContext.tenantDto != null ? testContext.tenantDto.getIdentifier() : proofTenantIdentifier,
                        testContext.authUserDto.getAuthToken()));
    }

    @Then("^le serveur retourne la subrogation qui existe pour moi en tant que subrogateur$")
    public void le_serveur_retourne_la_subrogation_qui_existe_pour_moi_en_tant_que_subrogateur() {
        le_serveur_retourne_la_subrogation_qui_existe_pour_moi_en_tant_que_subrogé();
    }

    @Given("^aucune subrogation n'existe pour moi en tant que subrogé$")
    public void aucune_subrogation_n_existe_pour_moi_en_tant_que_subrogé() {
        UserDto user = FactoryDto.buildDto(UserDto.class);
        user.setCustomerId(TestConstants.SYSTEM_CUSTOMER_ID);
        user = getUserRestClient().create(getSystemTenantUserAdminContext(), user);
        testContext.authUserDto = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        user.getEmail(), Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));
    }

    @Then("^le serveur ne retourne aucune subrogation pour moi en tant que subrogé$")
    public void le_serveur_ne_retourne_aucune_subrogation_pour_moi_en_tant_que_subrogé() {
        assertThat(subrogationDto).isNull();
    }

    @Given("^aucune subrogation n'existe pour moi en tant que subrogateur$")
    public void aucune_subrogation_n_existe_pour_moi_en_tant_que_subrogateur() {
        aucune_subrogation_n_existe_pour_moi_en_tant_que_subrogé();
    }

    @Then("^le serveur ne retourne aucune subrogation pour moi en tant que subrogateur$")
    public void le_serveur_ne_retourne_aucune_subrogation_pour_moi_en_tant_que_subrogateur() {
        aucune_subrogation_n_existe_pour_moi_en_tant_que_subrogé();
    }
}
