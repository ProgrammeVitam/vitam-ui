package fr.gouv.vitamui.cucumber.back.steps.iam.group;

import static fr.gouv.vitamui.utils.TestConstants.UPDATED;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;

/**
 * Teste l'API Groups dans IAM admin : opérations de mise à jour.
 *
 *
 */
public class ApiIamExternalGroupUpdateSteps extends CommonSteps {

    private GroupDto buildGroupToUpdate() {
        final GroupDto dto = FactoryDto.buildDto(GroupDto.class);
        dto.setId(testContext.groupDto.getId());
        dto.setDescription(UPDATED + testContext.savedGroupDto.getDescription());
        return dto;
    }

    @When("^un utilisateur met à jour un groupe$")
    public void un_utilisateur_met_à_jour_un_groupe() {
        final GroupDto dto = buildGroupToUpdate();
        try {
            getGroupRestClient().update(getSystemTenantUserAdminContext(), dto);
        } catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
