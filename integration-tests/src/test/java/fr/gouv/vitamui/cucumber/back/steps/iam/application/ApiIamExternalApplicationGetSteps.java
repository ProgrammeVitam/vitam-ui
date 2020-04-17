package fr.gouv.vitamui.cucumber.back.steps.iam.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Teste l'API Applications dans IAM admin : opérations de récupération.
 *
 *
 */
public class ApiIamExternalApplicationGetSteps extends CommonSteps {

    private List<ApplicationDto> applicationDtos;

    @When("^un utilisateur récupère toutes les applications dans un tenant auquel il est autorisé en utilisant un certificat full access$")
    public void un_utilisateur_récupère_tous_les_applications_dans_un_tenant_auquel_il_est_autorisé_en_utilisant_un_certificat_full_access() {
        applicationDtos = getApplicationRestClient().getAll(getSystemTenantUserAdminContext());
    }

    @Then("^le serveur retourne toutes les applications$")
    public void le_serveur_retourne_tous_les_applicationes() {
        assertThat(applicationDtos).isNotNull();

        final int size = applicationDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(7);
    }

    @When("^un utilisateur récupère toutes les applications dans un tenant auquel il est autorisé avec un accès limité aux applications$")
    public void un_utilisateur_récupère_tous_les_applications_dans_un_tenant_auquel_il_est_autorisé_avec_un_acces_limite_aux_applications() {
        applicationDtos = getApplicationRestClient().getAll(getContextForLimitedApps());
    }

    @Then("^le serveur retourne les applications autorisées$")
    public void le_serveur_retourne_les_applications_authorisees() {
        assertThat(applicationDtos).isNotNull();

        final int size = applicationDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(1);
        assertThat(applicationDtos.get(0).getIdentifier()).isEqualTo("CUSTOMERS_APP");
    }

    @When("^un utilisateur récupère toutes les applications dans un tenant auquel il est autorisé sans filtre sur les applications")
    public void un_utilisateur_récupère_tous_les_applications_dans_un_tenant_auquel_il_est_autorisé_sans_filtre_sur_les_applications() {
        applicationDtos = getApplicationRestClient().getAll(getContextForLimitedApps(), getQueryWithoutFilterApp());
    }

    @Then("^le serveur retourne toutes les applications non filtrées$")
    public void le_serveur_retourne_tous_les_applicationes_non_filtrees() {
        assertThat(applicationDtos).isNotNull();

        final int size = applicationDtos.size();
        assertThat(size).isGreaterThanOrEqualTo(7);
    }

    private Optional<String> getQueryWithoutFilterApp() {
        final QueryDto query = new QueryDto(QueryOperator.AND);
        query.addCriterion(new Criterion("filterApp", false, CriterionOperator.EQUALS));

        return Optional.of(query.toJson());
    }

    private ExternalHttpContext getContextForLimitedApps() {
        final AuthUserDto user = new AuthUserDto();
        user.setLevel("");
        user.setTenantsByApp(getTenantInformationByApp());

        testContext.authUserDto = new AuthUserDto(user);
        return getSystemTenantUserAdminContext();
    }

    private List<TenantInformationDto> getTenantInformationByApp() {
        final TenantInformationDto tenantForApp = new TenantInformationDto();
        tenantForApp.setName(CommonConstants.CUSTOMERS_APPLICATIONS_NAME);
        tenantForApp.setTenants(new HashSet<>());
        return Arrays.asList(tenantForApp);
    }

}
