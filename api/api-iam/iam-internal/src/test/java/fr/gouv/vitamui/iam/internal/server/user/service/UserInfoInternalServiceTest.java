package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserInfoConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserInfoRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.UserInfo;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserInfoInternalServiceTest {

    @InjectMocks
    private UserInfoInternalService userInfoInternalService;

    @Mock
    private CustomSequenceRepository sequenceRepository;

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private InternalHttpContext internalHttpContext;

    @Mock
    private UserInfoConverter userInfoConverter;

    @Test
    void testGetMe_should_be_ok() {
        //Given
        final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto("1", "test@vitamui.com", "customerId");
        userDto.setUserInfoId("userInfoId");
        Mockito.when(internalSecurityService.getUser()).thenReturn(userDto);
        Mockito.when(userInfoRepository.findById(any())).thenReturn(buildUserInfoEntity());
        Mockito.when(userInfoConverter.convertEntityToDto(any())).thenReturn(buildUserInfoDto());

        //when
        final UserInfoDto userInfoDto = userInfoInternalService.getMe();

        //then
        Assertions.assertEquals("fr", userInfoDto.getLanguage());
    }

    @Test
    void testGetMe_should_throw_ApplicationServerException_when_user_not_have_userInfoId() {
        assertThrows(ApplicationServerException.class, () -> {
            //Given
            final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto("1", "test@vitamui.com", "customerId");
            Mockito.when(internalSecurityService.getUser()).thenReturn(userDto);

            //when
            final UserInfoDto userInfoDto = userInfoInternalService.getMe();

            //then
            Assertions.assertEquals("fr", userInfoDto.getLanguage());
        });
    }

    @Test
    void testGetMe_should_throw_ApplicationServerException_when_userInfoId_not_found() {
        assertThrows(ApplicationServerException.class, () -> {
            //Given
            final AuthUserDto userDto = IamDtoBuilder.buildAuthUserDto("1", "test@vitamui.com", "customerId");
            userDto.setUserInfoId("userInfoId");
            Mockito.when(internalSecurityService.getUser()).thenReturn(userDto);
            Mockito.when(userInfoRepository.findById(any())).thenReturn(Optional.empty());

            //when
            final UserInfoDto userInfoDto = userInfoInternalService.getMe();

            //then
            Assertions.assertEquals("fr", userInfoDto.getLanguage());
        });
    }

    private Optional<UserInfo> buildUserInfoEntity() {
        final UserInfo userInfo = new UserInfo();
        userInfo.setId("userInfoId");
        userInfo.setLanguage("fr");
        return Optional.of(userInfo);
    }

    private UserInfoDto buildUserInfoDto() {
        final UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setId("userInfoId");
        userInfoDto.setLanguage("fr");
        return userInfoDto;
    }

    @Test
    void testGetEntityClass() {
        Assertions.assertEquals(UserInfo.class, userInfoInternalService.getEntityClass());
    }

    @Test
    void testGetRepository() {
        Assertions.assertEquals(userInfoRepository, userInfoInternalService.getRepository());
    }

    @Test
    void testGetConverter() {
        Assertions.assertEquals(userInfoConverter, userInfoInternalService.getUserInfoConverter());
    }
}
