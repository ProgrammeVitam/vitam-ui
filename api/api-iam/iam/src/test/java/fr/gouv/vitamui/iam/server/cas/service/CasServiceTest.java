package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.provisioning.service.ProvisioningService;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasServiceTest {

    private static final String IDP = "IDP";

    private static final String USER_EMAIL = "user@email.test";

    private static final String GROUP_ID = "groupID";

    private static final String USER_INFO_ID = "userInfoId";

    private static final String CUSTOMER_ID = "customerID";

    private static final String POLICY_PATTERN = "^.{12,}$";

    @InjectMocks
    private CasService casService;

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private UserService userService;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private GroupService groupService;

    @Mock
    private ProvisioningService provisioningService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private IdentityProviderHelper identityProviderHelper;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private PasswordConfiguration passwordConfiguration;

    @Test
    void should_reject_a_password_not_matching_the_policy() {
        givenAnEnabledUserAndCustomer();
        givenAnInternalIdentityProvider();
        when(passwordConfiguration.getPolicyPattern()).thenReturn(POLICY_PATTERN);
        when(passwordValidator.isValid(POLICY_PATTERN, "weak")).thenReturn(false);

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "weak", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void should_reject_a_password_containing_the_user_name() {
        final User user = givenAnEnabledUserAndCustomer();
        givenAnInternalIdentityProvider();
        when(passwordConfiguration.getPolicyPattern()).thenReturn(POLICY_PATTERN);
        when(passwordValidator.isValid(POLICY_PATTERN, "Dupont2026!")).thenReturn(true);
        when(passwordConfiguration.isCheckOccurrence()).thenReturn(true);
        when(passwordConfiguration.getOccurrencesCharsNumber()).thenReturn(3);
        when(passwordValidator.isContainsUserOccurrences(user.getLastname(), "Dupont2026!", 3)).thenReturn(true);

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "Dupont2026!", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void should_reject_a_password_change_for_a_user_without_identity_provider() {
        givenAnEnabledUserAndCustomer();
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "Str0ng!Password", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void should_reject_a_password_change_for_a_user_behind_an_external_provider() {
        givenAnEnabledUserAndCustomer();
        final IdentityProviderDto externalProvider = new IdentityProviderDto();
        externalProvider.setInternal(false);
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString()))
            .thenReturn(Optional.of(externalProvider));

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "Str0ng!Password", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class);
    }

    private void givenAnInternalIdentityProvider() {
        final IdentityProviderDto internalProvider = new IdentityProviderDto();
        internalProvider.setInternal(true);
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString()))
            .thenReturn(Optional.of(internalProvider));
    }

    private User givenAnEnabledUserAndCustomer() {
        final User user = new User();
        user.setEmail(USER_EMAIL);
        user.setCustomerId(CUSTOMER_ID);
        user.setLastname("Dupont");
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setStatus(UserStatusEnum.ENABLED);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(new Customer()));
        when(userRepository.findByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(user);
        return user;
    }

    @ParameterizedTest
    @NullAndEmptySource
    void should_return_the_user_known_in_database_when_idp_auto_provisioning_is_disabled(String idp) {
        when(userService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(buildAuthUser(false));

        final UserDto user = casService.getUser(USER_EMAIL, CUSTOMER_ID, idp, null, null);
        assertThat(user).isNotNull();
    }

    @Test
    void should_create_new_user_when_authenticated_user_is_unknown_in_database_and_idp_auto_provisioning_is_enabled() {
        when(identityProviderService.getOne(IDP)).thenReturn(buildIDP(true));

        when(provisioningService.getUserInformation(IDP, USER_EMAIL, CUSTOMER_ID, null, null, null)).thenReturn(
            buildProvidedUser("jean-vitam", "RH")
        );

        when(groupService.getGroupByUnitInternal(any())).thenReturn(buildGroup());

        when(userRepository.existsByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(false);

        when(userService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(buildAuthUser(false));
        when(userInfoService.create(any())).thenReturn(buildUserInfo());

        final Customer customer = new Customer();
        customer.setLanguage("fr");
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));

        final UserDto user = casService.getUser(USER_EMAIL, CUSTOMER_ID, IDP, null, null);
        verify(userService, times(1)).create(any());
        verify(userService, times(0)).patch(any());
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
        when(identityProviderService.getOne(IDP)).thenReturn(buildIDP(true));

        when(provisioningService.getUserInformation(IDP, USER_EMAIL, CUSTOMER_ID, GROUP_ID, null, null)).thenReturn(
            buildProvidedUser("jean vitam", "RH")
        );

        when(groupService.getGroupByUnitInternal(any())).thenReturn(buildGroup());

        when(userRepository.existsByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(true);
        when(userService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(buildAuthUser(true));

        final UserDto user = casService.getUser(USER_EMAIL, CUSTOMER_ID, IDP, null, null);
        verify(userService, times(1)).patch(any());
        verify(userService, times(0)).create(any());
        assertThat(user).isNotNull();
    }

    @Test
    void should_not_update_user_when_user_auto_provisioning_is_disabled() {
        when(identityProviderService.getOne(IDP)).thenReturn(buildIDP(true));

        when(userRepository.existsByEmailIgnoreCaseAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(true);
        when(userService.findUserByEmailAndCustomerId(USER_EMAIL, CUSTOMER_ID)).thenReturn(buildAuthUser(false));

        final UserDto user = casService.getUser(USER_EMAIL, CUSTOMER_ID, IDP, null, null);
        verify(userService, times(0)).patch(any());
        verify(userService, times(0)).create(any());
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
