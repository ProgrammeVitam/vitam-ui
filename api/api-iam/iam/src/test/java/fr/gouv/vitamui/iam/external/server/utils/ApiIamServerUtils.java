package fr.gouv.vitamui.iam.external.server.utils;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;

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

    public static UserInfoDto buildUserInfoDto(final String id) {
        final UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setId(id);
        userInfoDto.setLanguage("fr");
        return userInfoDto;
    }

    public static ProfileDto buildProfileDto(final String id) {
        final ProfileDto profileDto = new ProfileDto();
        profileDto.setId(id);
        return profileDto;
    }

    public static ExternalParamProfileDto buildExternalParamProfile(final String id) {
        final ExternalParamProfileDto profileDto = new ExternalParamProfileDto();
        profileDto.setId(id);
        return profileDto;
    }
}
