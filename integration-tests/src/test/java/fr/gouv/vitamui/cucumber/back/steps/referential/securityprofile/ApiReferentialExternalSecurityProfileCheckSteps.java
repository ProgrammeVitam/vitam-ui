package fr.gouv.vitamui.cucumber.back.steps.referential.securityprofile;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.When;

/**
 * Teste l'API Security Profiles dans Referential admin : opérations de vérification.
 *
 *
 */
public class ApiReferentialExternalSecurityProfileCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un profile de sécurité par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_profile_de_sécurité_par_son_identifiant() {
        try {
            final SecurityProfileDto ingestContractDto = new SecurityProfileDto();
            ingestContractDto.setIdentifier(TestConstants.SECURITY_PROFILE_IDENTIFIER);
            testContext.bResponse = getSecurityProfileRestClient().check(getSystemTenantUserAdminContext(), ingestContractDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
