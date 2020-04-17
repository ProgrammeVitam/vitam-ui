package fr.gouv.vitamui.cucumber.back.steps.iam.profile;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;

/**
 * Teste l'API Profiles dans IAM admin : opérations de mise à jour.
 *
 *
 */
public class ApiIamExternalProfileUpdateSteps extends CommonSteps {

    private ProfileDto buildProfileToUpdate() {
        final ProfileDto dto = FactoryDto.buildDto(ProfileDto.class);
        dto.setId(testContext.profileDto.getId());
        dto.setDescription(UPDATED + testContext.savedProfileDto.getDescription());
        return dto;
    }

    @When("^un utilisateur met à jour un profil$")
    public void un_utilisateur_met_à_jour_un_profil() {
        final ProfileDto dto = buildProfileToUpdate();
        try {
            getProfileRestClient().update(getSystemTenantUserAdminContext(), dto);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
