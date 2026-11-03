package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Generic users can only be created or modified by callers holding {@code ROLE_GENERIC_USERS}.
 */
class UserControllerTypeTest {

    private static final String USER_ID = "userId";

    private final UserService userService = mock(UserService.class);
    private final SecurityService securityService = mock(SecurityService.class);

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(
            userService,
            mock(ConnectionHistoryService.class),
            securityService,
            mock(SignedDownloadTokenService.class)
        );
        when(securityService.hasRole(ServicesData.ROLE_GENERIC_USERS)).thenReturn(false);
        when(securityService.getCustomerId()).thenReturn("customerId");
        storedUserType(UserTypeEnum.NOMINATIVE);
    }

    @Test
    void creating_a_generic_user_requires_the_generic_role() {
        assertThatThrownBy(() -> controller.create(user(UserTypeEnum.GENERIC))).isInstanceOf(ForbiddenException.class);
        verify(userService, never()).create(any());
    }

    @Test
    void creating_a_nominative_user_does_not_require_the_generic_role() throws Exception {
        final UserDto dto = user(UserTypeEnum.NOMINATIVE);
        controller.create(dto);
        verify(userService).create(dto);
    }

    @Test
    void creating_a_generic_user_with_the_generic_role_is_accepted() throws Exception {
        when(securityService.hasRole(ServicesData.ROLE_GENERIC_USERS)).thenReturn(true);
        final UserDto dto = user(UserTypeEnum.GENERIC);
        controller.create(dto);
        verify(userService).create(dto);
    }

    @Test
    void updating_a_user_to_generic_requires_the_generic_role() {
        assertThatThrownBy(() -> controller.update(USER_ID, user(UserTypeEnum.GENERIC))).isInstanceOf(
            ForbiddenException.class
        );
        verify(userService, never()).update(any());
    }

    @Test
    void updating_a_generic_user_requires_the_generic_role() {
        storedUserType(UserTypeEnum.GENERIC);
        assertThatThrownBy(() -> controller.update(USER_ID, user(UserTypeEnum.NOMINATIVE))).isInstanceOf(
            ForbiddenException.class
        );
        verify(userService, never()).update(any());
    }

    @Test
    void patching_the_type_to_generic_requires_the_generic_role() {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("type", UserTypeEnum.GENERIC.name());

        assertThatThrownBy(() -> controller.patch(USER_ID, partialDto)).isInstanceOf(ForbiddenException.class);
        verify(userService, never()).patch(any());
    }

    @Test
    void patching_a_generic_user_requires_the_generic_role() {
        storedUserType(UserTypeEnum.GENERIC);
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("firstname", "John");

        assertThatThrownBy(() -> controller.patch(USER_ID, partialDto)).isInstanceOf(ForbiddenException.class);
        verify(userService, never()).patch(any());
    }

    @Test
    void patching_a_nominative_user_does_not_require_the_generic_role() throws Exception {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", USER_ID);
        partialDto.put("firstname", "John");

        controller.patch(USER_ID, partialDto);
        verify(userService).patch(partialDto);
    }

    private void storedUserType(final UserTypeEnum type) {
        final UserDto stored = user(type);
        when(userService.findUserById(USER_ID)).thenReturn(stored);
    }

    private static UserDto user(final UserTypeEnum type) {
        final UserDto dto = new UserDto();
        dto.setId(USER_ID);
        dto.setCustomerId("customerId");
        dto.setEmail("user@example.com");
        dto.setType(type);
        return dto;
    }
}
