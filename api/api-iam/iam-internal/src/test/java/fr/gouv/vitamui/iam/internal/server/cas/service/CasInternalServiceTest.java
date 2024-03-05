package fr.gouv.vitamui.iam.internal.server.cas.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.provisioning.service.ProvisioningInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInfoInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasInternalServiceTest {

    private static final String IDP = "IDP";

    private static final String USER_EMAIL = "user@email.test";

    private static final String GROUP_ID = "groupID";

    private static final String USER_INFO_ID = "userInfoId";

    private static final String CUSTOMER_ID = "customerID";

    @InjectMocks
    private CasInternalService casInternalService;

    @Mock
    private IdentityProviderInternalService identityProviderInternalService;

    @Mock
    private UserInternalService userInternalService;

    @Mock
    private UserInfoInternalService userInfoInternalService;

    @Mock
    private GroupInternalService groupInternalService;

    @Mock
    private ProvisioningInternalService provisioningInternalService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void should_return_the_user_known_in_database_when_idp_auto_provisioning_is_disabled(String idp) {
        when(userInternalService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID))
            .thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, CUSTOMER_ID, idp, null, null);
        assertThat(user).isNotNull();
    }

    @Test
    void should_create_new_user_when_authenticated_user_is_unknown_in_database_and_idp_auto_provisioning_is_enabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(provisioningInternalService.getUserInformation(IDP, USER_EMAIL, null, null, null, CUSTOMER_ID))
            .thenReturn(buildProvidedUser("jean-vitam", "RH"));

        when(groupInternalService.getAll(any(), any())).thenReturn(List.of(buildGroup()));

        when(userRepository.existsByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(false);

        when(userInternalService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID))
                .thenReturn(buildAuthUser(false));
        when(userInfoInternalService.create(any())).thenReturn(buildUserInfo());

        final Customer customer = new Customer();
        customer.setLanguage("fr");
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));

        final UserDto user = casInternalService.getUser(USER_EMAIL, CUSTOMER_ID, IDP, null, null);
        verify(userInternalService, times(1)).create(any());
        verify(userInternalService, times(0)).patch(any());
        assertThat(user).isNotNull();
    }

    private UserInfoDto buildUserInfo() {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setId(USER_INFO_ID);
        userInfoDto.setLanguage("FR");
        return userInfoDto;
    }

    @Test
    void should_update_user_when_authenticated_user_is_known_in_database_and_idp_and_user_auto_provisioning_is_enabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(provisioningInternalService.getUserInformation(IDP, USER_EMAIL, GROUP_ID, null, null, CUSTOMER_ID))
            .thenReturn(buildProvidedUser("jean vitam", "RH"));

        when(groupInternalService.getAll(any(), any())).thenReturn(List.of(buildGroup()));

        when(userRepository.existsByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(true);
        when(userInternalService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID))
            .thenReturn(buildAuthUser(true));
        when(userInternalService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID))
            .thenReturn(buildAuthUser(true));


        final UserDto user = casInternalService.getUser(USER_EMAIL, CUSTOMER_ID, IDP, null, null);
        verify(userInternalService, times(1)).patch(any());
        verify(userInternalService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    @Test
    void should_not_update_user_when_user_auto_provisioning_is_disabled() {
        when(identityProviderInternalService.getOne(IDP))
                .thenReturn(buildIDP(true));

        when(userRepository.existsByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(true);
        when(userInternalService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID))
            .thenReturn(buildAuthUser(false));
        when(userInternalService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID))
            .thenReturn(buildAuthUser(false));

        final UserDto user = casInternalService.getUser(USER_EMAIL, CUSTOMER_ID, IDP, null, null);
        verify(userInternalService, times(0)).patch(any());
        verify(userInternalService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    private GroupDto buildGroup() {
        final GroupDto group = new GroupDto();
        group.setId(GROUP_ID);
        group.setCustomerId(CUSTOMER_ID);
        return group;
    }

    private AuthUserDto buildAuthUser(final boolean autoProvisioningEnabled) {
        final AuthUserDto authUser = new AuthUserDto();
        authUser.setEmail(USER_EMAIL);
        authUser.setFirstname("Jean-Jacques");
        authUser.setLastname("Dupont");
        authUser.setAutoProvisioningEnabled(autoProvisioningEnabled);
        authUser.setGroupId(GROUP_ID);
        authUser.setUserInfoId(GROUP_ID);
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
        idp.setCustomerId(CUSTOMER_ID);
        idp.setAutoProvisioningEnabled(autoProvisioningEnabled);
        return idp;
    }
}
