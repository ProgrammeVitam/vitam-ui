package fr.gouv.vitamui.iam.server.profile.converter;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.server.profile.domain.Profile;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileConverterTest {

    private final ProfileConverter profileConverter = new ProfileConverter();

    @Test
    void testConvertEntityToDto() {
        Profile profile = new Profile();
        profile.setApplicationName("applicationName");
        profile.setCustomerId("customerId");
        profile.setDescription("Description");
        profile.setEnabled(true);
        profile.setExternalParamId("externalParamId");
        profile.setId("id");
        profile.setIdentifier("identifier");
        profile.setLevel("level");
        profile.setName("name");
        profile.setReadonly(true);
        profile.setRoles(Arrays.asList(new Role(ServicesData.ROLE_CAS_LOGIN)));
        profile.setTenantIdentifier(10);

        ProfileDto res = profileConverter.convertEntityToDto(profile);
        assertThat(res).isEqualToComparingOnlyGivenFields(
            profile,
            "applicationName",
            "customerId",
            "description",
            "enabled",
            "id",
            "identifier",
            "level",
            "name",
            "readonly",
            "roles",
            "tenantIdentifier"
        );
    }

    @Test
    void testConvertToToEntity() {
        ProfileDto profile = new ProfileDto();
        profile.setApplicationName("applicationName");
        profile.setCustomerId("customerId");
        profile.setDescription("Description");
        profile.setEnabled(true);
        profile.setExternalParamId("externalParamId");
        profile.setId("id");
        profile.setIdentifier("identifier");
        profile.setLevel("level");
        profile.setName("name");
        profile.setReadonly(true);
        profile.setRoles(Arrays.asList(new Role(ServicesData.ROLE_CAS_LOGIN)));
        profile.setTenantIdentifier(10);

        Profile res = profileConverter.convertDtoToEntity(profile);
        assertThat(res).isEqualToIgnoringGivenFields(profile, "readonly");
    }

    @Test
    void testConvertToLogbook() throws InvalidParseOperationException {
        ProfileDto profile = new ProfileDto();
        profile.setApplicationName("applicationName");
        profile.setCustomerId("customerId");
        profile.setDescription("Description");
        profile.setEnabled(true);
        profile.setExternalParamId("externalParamId");
        profile.setExternalParamIdentifier("externalIdentifier");
        profile.setId("id");
        profile.setIdentifier("identifier");
        profile.setLevel("level");
        profile.setName("name");
        profile.setReadonly(true);
        profile.setRoles(Arrays.asList(new Role(ServicesData.ROLE_CAS_LOGIN)));
        profile.setTenantIdentifier(10);

        String json = profileConverter.convertToLogbook(profile);
        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(ProfileConverter.EXTERNAL_PARAM_IDENTIFIER)).isNotNull();

        profile.setExternalParamIdentifier(null);
        json = profileConverter.convertToLogbook(profile);
        jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(ProfileConverter.EXTERNAL_PARAM_IDENTIFIER)).isNull();
    }
}
