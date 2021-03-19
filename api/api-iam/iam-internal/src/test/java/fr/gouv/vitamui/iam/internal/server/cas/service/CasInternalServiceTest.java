package fr.gouv.vitamui.iam.internal.server.cas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProvidedUserDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.provisioning.service.ProvisioningInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;

@ExtendWith(MockitoExtension.class)
class CasInternalServiceTest {

    private static final String IDP = "IDP";

    private static final String USER_EMAIL = "user@email.test";

    @InjectMocks
    private CasInternalService casInternalService;

    @Mock
    private IdentityProviderInternalService identityProviderInternalService;

    @Mock
    private UserInternalService userInternalService;

    @Mock
    private GroupInternalService groupInternalService;

    @Mock
    private ProvisioningInternalService provisioningInternalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void should_return_the_user_known_in_database_when_idp_auto_provisioning_is_disabled() {
        when(identityProviderInternalService.getOne(anyString()))
                .thenReturn(buildIDP(false));

        when(userInternalService.findUserByEmail(USER_EMAIL)).thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, Optional.empty(), Optional.empty());
        assertThat(user).isNotNull();
    }

    @Test
    void should_create_new_user_when_authenticated_user_is_unknown_in_database_and_idp_auto_provisioning_is_enabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(provisioningInternalService.getUserInformation(USER_EMAIL, IDP, Optional.empty()))
                .thenReturn(buildProvidedUser("RH"));

        when(groupInternalService.getAll(any(), any())).thenReturn(List.of(buildProfilesGroup()));

        when(userInternalService.findUserByEmail(USER_EMAIL))
                .thenThrow(new NotFoundException("Not found"))
                .thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, Optional.empty(), Optional.empty());
        verify(userInternalService, times(1)).create(any());
        verify(userInternalService, times(0)).patch(any());
        assertThat(user).isNotNull();
    }

    @Test
    void should_update_user_when_authenticated_user_is_known_in_database_and_idp_and_user_auto_provisioning_is_enabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(provisioningInternalService.getUserInformation(USER_EMAIL, IDP, Optional.empty()))
                .thenReturn(buildProvidedUser("RH"));

        when(groupInternalService.getAll(any(), any())).thenReturn(List.of(buildProfilesGroup()));

        when(userInternalService.findUserByEmail(USER_EMAIL)).thenReturn(buildAuthUser(true));


        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, Optional.empty(), Optional.empty());
        verify(userInternalService, times(1)).patch(any());
        verify(userInternalService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    @Test
    void should_not_update_user_when_user_auto_provisioning_is_disabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(userInternalService.findUserByEmail(USER_EMAIL)).thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, Optional.empty(), Optional.empty());
        verify(userInternalService, times(0)).patch(any());
        verify(userInternalService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    private GroupDto buildProfilesGroup() {
        final GroupDto group = new GroupDto();
        group.setId("Group ID");
        return group;
    }

    private AuthUserDto buildAuthUser(final boolean autoProvisioningEnabled) {
        final AuthUserDto authUser = new AuthUserDto();
        authUser.setEmail(USER_EMAIL);
        authUser.setFirstname("Jean-Jacques");
        authUser.setLastname("Dupont");
        authUser.setAutoProvisioningEnabled(autoProvisioningEnabled);
        return authUser;
    }

    private ProvidedUserDto buildProvidedUser(final String unit) {
        final ProvidedUserDto providedUser = new ProvidedUserDto();
        providedUser.setEmail(USER_EMAIL);
        providedUser.setFirstname("Jean-Jacques");
        providedUser.setLastname("Dupont");
        providedUser.setUnit(unit);
        return providedUser;
    }

    private IdentityProviderDto buildIDP(final boolean autoProvisioningEnabled) {
        final IdentityProviderDto idp = new IdentityProviderDto();
        idp.setId(IDP);
        idp.setAutoProvisioningEnabled(autoProvisioningEnabled);
        return idp;
    }
}