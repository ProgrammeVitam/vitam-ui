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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Generic users can only be created by callers holding {@code ROLE_GENERIC_USERS}. Updates and patches are checked
 * by {@code UserService} (see {@code UserServiceCustomerScopeTest}).
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

    private static UserDto user(final UserTypeEnum type) {
        final UserDto dto = new UserDto();
        dto.setId(USER_ID);
        dto.setCustomerId("customerId");
        dto.setEmail("user@example.com");
        dto.setType(type);
        return dto;
    }
}
