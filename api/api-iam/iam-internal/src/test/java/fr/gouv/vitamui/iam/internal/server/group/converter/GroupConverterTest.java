package fr.gouv.vitamui.iam.internal.server.group.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;

public class GroupConverterTest {

    private final ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);

    private final GroupConverter groupConverter = new GroupConverter(profileRepository);

    @Test
    public void testConvertEntityToDto() {
        Group group = new Group();
        group.setCustomerId("customerId");
        group.setDescription("description");
        group.setEnabled(true);
        group.setId("id");
        group.setIdentifier("identifier");
        group.setLevel("level");
        group.setName("name");
        group.setProfileIds(Arrays.asList("1", "2"));

        GroupDto res = groupConverter.convertEntityToDto(group);
        assertThat(group).isEqualToIgnoringGivenFields(res);
    }

    @Test
    public void testConvertDtoToEntity() {
        GroupDto groupDto = new GroupDto();
        groupDto.setCustomerId("customerID");
        groupDto.setDescription("description");
        groupDto.setEnabled(true);
        groupDto.setId("id");
        groupDto.setIdentifier("identifier");
        groupDto.setLevel("level");
        groupDto.setName("name");

        Group res = groupConverter.convertDtoToEntity(groupDto);
        assertThat(groupDto).isEqualToIgnoringGivenFields(res, "profiles", "usersCount");
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        GroupDto groupDto = new GroupDto();
        groupDto.setCustomerId("customerID");
        groupDto.setDescription("description");
        groupDto.setEnabled(true);
        groupDto.setId("id");
        groupDto.setIdentifier("identifier");
        groupDto.setLevel("level");
        groupDto.setName("name");

        String json = groupConverter.convertToLogbook(groupDto);

        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(GroupConverter.DESCRIPTION_KEY)).isNotNull();
        assertThat(jsonNode.get(GroupConverter.NAME_KEY)).isNotNull();
        assertThat(jsonNode.get(GroupConverter.ENABLED_KEY)).isNotNull();
        assertThat(jsonNode.get(GroupConverter.NAME_KEY)).isNotNull();
        assertThat(jsonNode.get(GroupConverter.PROFILE_IDS_KEY)).isNotNull();

    }
}
