package fr.gouv.vitamui.iam.external.server.utils;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;

public class ApiIamServerUtils {
    public static CustomerDto buildCustomerDto(final String id) {
        final CustomerDto dto = IamDtoBuilder.buildCustomerDto(id, "customer test", "0000000", "@test.fr");
        return dto;
    }

    public static UserDto buildUserDto(final String id) {
        final UserDto basicUserDto = new UserDto();
        basicUserDto.setId(id);
        return basicUserDto;
    }

    public static ProfileDto buildProfileDto(final String id) {
        final ProfileDto profileDto = new ProfileDto();
        profileDto.setId(id);
        return profileDto;
    }
}
