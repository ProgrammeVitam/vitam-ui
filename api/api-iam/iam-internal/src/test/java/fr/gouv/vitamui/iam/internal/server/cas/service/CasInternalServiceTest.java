package fr.gouv.vitamui.iam.internal.server.cas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.provisioning.service.ProvisioningInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;

@ExtendWith(MockitoExtension.class)
class CasInternalServiceTest {

    private static final String IDP = "IDP";

    private static final String USER_EMAIL = "user@email.test";

    private static final String GROUP_ID = "groupID";

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

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void should_return_the_user_known_in_database_when_idp_auto_provisioning_is_disabled(String idp) {
        when(userInternalService.findUserByEmail(USER_EMAIL)).thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, idp, null, null);
        assertThat(user).isNotNull();
    }

    @Test
    void should_create_new_user_when_authenticated_user_is_unknown_in_database_and_idp_auto_provisioning_is_enabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(provisioningInternalService.getUserInformation(IDP, USER_EMAIL, null, null, null))
                .thenReturn(buildProvidedUser("jean-vitam","RH"));

        when(groupInternalService.getAll(any(), any())).thenReturn(List.of(buildGroup()));

        when(userRepository.existsByEmail(ArgumentMatchers.any())).thenReturn(false);

        when(userInternalService.findUserByEmail(USER_EMAIL))
                .thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, null, null);
        verify(userInternalService, times(1)).create(any());
        verify(userInternalService, times(0)).patch(any());
        assertThat(user).isNotNull();
    }

    @Test
    void should_update_user_when_authenticated_user_is_known_in_database_and_idp_and_user_auto_provisioning_is_enabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(provisioningInternalService.getUserInformation(IDP, USER_EMAIL, GROUP_ID, null, null))
                .thenReturn(buildProvidedUser("jean vitam", "RH"));

        when(groupInternalService.getAll(any(), any())).thenReturn(List.of(buildGroup()));

        when(userRepository.existsByEmail(ArgumentMatchers.any())).thenReturn(true);
        when(userInternalService.findUserByEmail(USER_EMAIL)).thenReturn(buildAuthUser(true));


        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, null, null);
        verify(userInternalService, times(1)).patch(any());
        verify(userInternalService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    @Test
    void should_not_update_user_when_user_auto_provisioning_is_disabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(userRepository.existsByEmail(ArgumentMatchers.any())).thenReturn(true);
        when(userInternalService.findUserByEmail(USER_EMAIL)).thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, IDP, null, null);
        verify(userInternalService, times(0)).patch(any());
        verify(userInternalService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    private GroupDto buildGroup() {
        final GroupDto group = new GroupDto();
        group.setId(GROUP_ID);
        return group;
    }

    private AuthUserDto buildAuthUser(final boolean autoProvisioningEnabled) {
        final AuthUserDto authUser = new AuthUserDto();
        authUser.setEmail(USER_EMAIL);
        authUser.setFirstname("Jean-Jacques");
        authUser.setLastname("Dupont");
        authUser.setAutoProvisioningEnabled(autoProvisioningEnabled);
        authUser.setGroupId(GROUP_ID);
        return authUser;
    }

    private ProvidedUserDto buildProvidedUser(final String firstName, final String unit) {
        final ProvidedUserDto providedUser = new ProvidedUserDto();
        providedUser.setEmail(USER_EMAIL);
        providedUser.setFirstname(firstName);
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
