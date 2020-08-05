package fr.gouv.vitamui.cucumber.back.steps.common;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.common.parametertypes.RoleParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.RolesParameterType;
import fr.gouv.vitamui.cucumber.common.parametertypes.TenantParameterType;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class SecuritySteps extends CommonSteps {

    @Before
    public void cleanTestContext() {
        testContext.reset();
    }

    @Then("^le serveur refuse l'accès à l'API (.+)$")
    public void then_le_serveur_refuse_l_accès_à_l_API_x(final String api) {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'accès : aucune exception n'a été levée").isNotNull();
        final String message = testContext.exception.toString();
        assertThat(message).isNotNull();
        final boolean isAccessUnauthorized = message.equals("fr.gouv.vitamui.commons.api.exception.ForbiddenException: Accès refusé");
        final boolean isInvalidAuthentication = message
                .equals("fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException: Full authentication is required to access this resource");
        final boolean isForbiddenException = message.equals("fr.gouv.vitamui.commons.api.exception.ForbiddenException: Unknown problem");
        final boolean isAccessDenied = message.equals("fr.gouv.vitamui.commons.api.exception.ForbiddenException: Access is denied");
        final boolean isInvalidAuthentication2 = message.equals("fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException: Unknown problem");
        assertThat(isAccessUnauthorized || isInvalidAuthentication || isForbiddenException || isAccessDenied || isInvalidAuthentication2)
                .overridingErrorMessage(
                        "Le serveur a bien refusé l'accès, mais pour une raison inattendue. Le message d'erreur obtenu est le suivant : " + message)
                .isTrue();
    }

    /**
     * Méthode redéfinit, car sur la requête HEAD, il n'y a pas de réponse
     * @param api
     */
    @Then("^le serveur refuse l'accès à HEAD (.+)$")
    public void then_le_serveur_refuse_l_accès_à_l_API_HEAD_x(final String api) {
        assertThat(testContext.exception).isNotNull();
    }

    @Then("^le serveur autorise l'accès à l'API (.+)$")
    public void le_serveur_autorise_l_accès_à_l_API_x(final String api) {
        assertThat(testContext.exception).isNull();
    }

    @Then("^le serveur refuse la vérification de l'existence car l'opération n'est pas implémentée$")
    public void le_serveur_refuse_la_vérification_de_l_existence_car_l_opération_n_est_pas_implémentée() {
        headNotSupported();
    }

    @Then("^le serveur refuse la mise à jour car l'opération n'est pas implémentée$")
    public void le_serveur_refuse_la_mise_à_jour_car_l_opération_n_est_pas_implémentée() {
        updateNotSupported();
    }

    @Then("^le serveur refuse la suppression car l'opération n'est pas implémentée$")
    public void le_serveur_refuse_la_suppression_car_l_opération_n_est_pas_implémentée() {
        deleteNotSupported();
    }

    @Then("^le serveur refuse la récupération car l'opération n'est pas implémentée$")
    public void le_serveur_refuse_la_récupération_car_l_opération_n_est_pas_implémentée() {
        getNotSupported();
    }

    @Then("^le serveur retourne vrai$")
    public void le_serveur_retourne_vrai() {
        assertThat(testContext.bResponse).isTrue();
    }

    @Then("^le serveur retourne faux")
    public void le_serveur_retourne_faux() {
        assertThat(testContext.bResponse).isFalse();
    }

    @Then("^le serveur ne retourne pas d'erreur$")
    public void le_serveur_ne_retourne_pas_d_erreur() throws Exception {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur a renvoyé l'erreur suivante : " + testContext.exception).isNull();
    }

    protected void headNotSupported() {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'appel : aucune exception n'a été levée").isNotNull();
        final String message = testContext.exception.toString();
        assertThat(message).isNotNull();
        final boolean internalServerException = message.equals("fr.gouv.vitamui.commons.api.exception.InternalServerException: Unknown problem");
        final boolean notImplementedException = message.equals("fr.gouv.vitamui.commons.api.exception.NotImplementedException: Unknown problem");
        assertThat(internalServerException || notImplementedException).isTrue();
    }

    protected void updateNotSupported() {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'appel : aucune exception n'a été levée").isNotNull();
        final String message = testContext.exception.toString();
        assertThat(message).isNotNull();
        final boolean invalidFormatException = message
                .equals("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Invalid Request : Request method 'PUT' not supported.");
        final boolean notImplementedException = message.equals("fr.gouv.vitamui.commons.api.exception.NotImplementedException: Update not supported");
        assertThat(invalidFormatException || notImplementedException).isTrue();
    }

    protected void deleteNotSupported() {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'appel : aucune exception n'a été levée").isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Invalid Request : Request method 'DELETE' not supported.");
    }

    protected void getNotSupported() {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'appel : aucune exception n'a été levée").isNotNull();
        assertThat(testContext.exception.toString())
                .isEqualTo("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Invalid Request : Request method 'GET' not supported.");
    }

    @Then("^le serveur refuse l'accès pour cause de niveau non autorisé")
    public void then_le_serveur_refuse_l_accès_pour_cause_de_niveau_non_autorise() {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'accès : aucune exception n'a été levée").isNotNull();
        final String message = testContext.exception.toString();
        assertThat(message).isNotNull();
        final boolean isInvalidFormatException = message
                .matches("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create (.+): level (.*) is not allowed");
        assertThat(isInvalidFormatException).overridingErrorMessage(
                "Le serveur a bien refusé l'accès, mais pour une raison inattendue. Le message d'erreur obtenu est le suivant : " + message).isTrue();
    }

    @Then("^le serveur refuse l'accès pour cause de groupe non trouvé")
    public void then_le_serveur_refuse_l_accès_pour_cause_de_groupe_non_trouvé() {
        assertThat(testContext.exception).overridingErrorMessage("Le serveur n'a pas refusé l'accès : aucune exception n'a été levée").isNotNull();
        final String message = testContext.exception.toString();
        assertThat(message).isNotNull();
        final boolean isInvalidFormatException =
                message.matches("fr.gouv.vitamui.commons.api.exception.InvalidFormatException: Unable to create user (.+): group does not exist");
        final boolean isINotFoundException = message.matches(
                "fr.gouv.vitamui.commons.api.exception.NotFoundException: Entity not found fr.gouv.vitamui.iam.internal.server.group.domain.Group with id : (.*)");
        assertThat(isInvalidFormatException || isINotFoundException).overridingErrorMessage(
                "Le serveur a bien refusé l'accès, mais pour une raison inattendue. Le message d'erreur obtenu est le suivant : " + message).isTrue();
    }

    @Given("l'utilisateur a selectionné le tenant {tenant} dans l'IHM")
    public void le_même_tenant_choisi_dans_l_IHM(final TenantParameterType tenant) throws Exception {
        testContext.tenantIHMContext = tenant.getTenant(testContext);
    }

    @Given("un utilisateur avec le rôle {role} sur le tenant {tenant}")
    public void un_utilisateur_avec_le_rôle_ROLE_sur_le_tenant_x(final RoleParameterType role, final TenantParameterType tenant) throws Exception {
        testContext.tokenUser = tokenUserTestSystemCustomer(role.getData(), tenant.getTenant(testContext));
    }

    @Given("un utilisateur avec les rôles {roles} sur le tenant {tenant}")
    public void un_utilisateur_avec_les_rôles_ROLE_sur_le_tenant_x(final RolesParameterType roles, final TenantParameterType tenant) throws Exception {
        testContext.tokenUser = tokenUserTestSystemCustomer(roles.getData(), tenant.getTenant(testContext));
    }

    @Given("un utilisateur sans le rôle {role} sur le tenant {tenant}")
    public void un_utilisateur_sans_le_rôle_ROLE_sur_ce_tenant(final RoleParameterType role, final TenantParameterType tenant) throws Exception {
        testContext.tokenUser = tokenUserNoRole(tenant.getTenant(testContext));
    }

    @Given("un utilisateur sans les rôles {roles} sur le tenant {tenant}")
    public void un_utilisateur_sans_les_rôles_ROLE_sur_ce_tenant(final RolesParameterType roles, final TenantParameterType tenant) throws Exception {
        testContext.tokenUser = tokenUserNoRole(tenant.getTenant(testContext));
    }

    @Given("un certificat avec le rôle {role} sur le tenant {tenant}")
    public void un_certificat_avec_le_rôle_ROLE_sur_le_même_tenant(final RoleParameterType role, final TenantParameterType tenant) throws Exception {
        setCertificateContext(false, tenant.getTenant(testContext), role.getData());
    }

    @Given("un certificat avec les rôles {roles} sur le tenant {tenant}")
    public void un_certificat_avec_les_rôles_ROLE_sur_le_même_tenant(final RolesParameterType roles, final TenantParameterType tenant) throws Exception {
        setCertificateContext(false, tenant.getTenant(testContext), roles.getData());
    }

    @Given("un certificat sans le rôle {role} sur le tenant {tenant}")
    public void un_certificat_sans_le_rôle_ROLE_FLOWS_sur_le_même_tenant(final RoleParameterType role, final TenantParameterType tenant) throws Exception {
        setCertificateContext(false, tenant.getTenant(testContext), testContext.defaultRole);
    }

    @Given("un certificat sans les rôles {roles} sur le tenant {tenant}")
    public void un_certificat_sans_les_rôles_ROLE_FLOWS_sur_le_même_tenant(final RolesParameterType roles, final TenantParameterType tenant) throws Exception {
        setCertificateContext(false, tenant.getTenant(testContext), testContext.defaultRole);
    }

    @Given("un certificat avec le rôle {role} étant fullAccess")
    public void un_certificat_avec_le_rôle_ROLE_étant_fullAccess(final RoleParameterType role) throws Exception {
        setCertificateContext(true, null, role.getData());
    }

    @Given("un certificat avec les rôles {roles} étant fullAccess")
    public void un_certificat_avec_les_rôles_ROLE_étant_fullAccess(final RolesParameterType roles) throws Exception {
        setCertificateContext(true, null, roles.getData());
    }

    @Given("un certificat sans le rôle {role} étant fullAccess")
    public void un_certificat_sans_le_rôle_ROLE_étant_fullAccess(final RoleParameterType role) throws Exception {
        setCertificateContext(true, null, testContext.defaultRole);
    }

    @Given("un certificat sans les rôles {roles} étant fullAccess")
    public void un_certificat_sans_les_rôles_ROLE_étant_fullAccess(final RolesParameterType roles) throws Exception {
        setCertificateContext(true, null, testContext.defaultRole);
    }

    private void setCertificateContext(final boolean fullAccess, final Integer tenant, final String role) {
        setCertificateContext(fullAccess, tenant, new String[]{role});
    }

    private void setCertificateContext(final boolean fullAccess, final Integer tenant, final String[] roles) {
        if (fullAccess == true || tenant == null) {
            testContext.certificateTenants = new Integer[]{};
        }
        else {
            testContext.certificateTenants = new Integer[] { tenant };
        }
        testContext.certificateRoles = roles;
        testContext.fullAccess = fullAccess;
    }

}
