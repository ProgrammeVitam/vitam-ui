package fr.gouv.vitamui.iam.internal.server.user.converter;

import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.iam.internal.server.user.domain.UserInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoConverterTest {

    private final UserInfoConverter userInfoConverter = new UserInfoConverter();

    @Test
    void testConvertFromDtoToEntity() {
        //Given
        final UserInfoDto dto = new UserInfoDto();
        dto.setLanguage("fr");

        //when
        final UserInfo entity = userInfoConverter.convertDtoToEntity(dto);

        //then
        assertThat(entity).isEqualToComparingFieldByField(dto);
    }

    @Test
    void testConvertFromEntityToDto() {
        //Given
        final UserInfo dto = new UserInfo();
        dto.setLanguage("fr");

        //when
        final UserInfoDto entity = userInfoConverter.convertEntityToDto(dto);

        //then
        assertThat(entity).isEqualToComparingFieldByField(dto);
    }
}
