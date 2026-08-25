package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.common.dto.cas.OrganizationCandidateDto;
import fr.gouv.vitamui.iam.common.error.PasswordChangeErrorKeys;
import fr.gouv.vitamui.iam.common.dto.cas.ResolvedIdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private CustomerService customerService;

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

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "weak", CUSTOMER_ID)).isInstanceOf(
            BadRequestException.class
        );
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

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "Dupont2026!", CUSTOMER_ID)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    void should_reject_a_password_change_for_a_user_without_identity_provider() {
        givenAnEnabledUserAndCustomer();
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString())).thenReturn(
            Optional.empty()
        );

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "Str0ng!Password", CUSTOMER_ID)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    void should_reject_a_password_change_for_a_user_behind_an_external_provider() {
        givenAnEnabledUserAndCustomer();
        final IdentityProviderDto externalProvider = new IdentityProviderDto();
        externalProvider.setInternal(false);
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString())).thenReturn(
            Optional.of(externalProvider)
        );

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "Str0ng!Password", CUSTOMER_ID)).isInstanceOf(
            BadRequestException.class
        );
    }

    private void givenAnInternalIdentityProvider() {
        final IdentityProviderDto internalProvider = new IdentityProviderDto();
        internalProvider.setInternal(true);
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString())).thenReturn(
            Optional.of(internalProvider)
        );
    }

    @Test
    void should_refuse_a_password_change_for_a_non_nominative_user() {
        final User genericUser = givenAnEnabledUserAndCustomer();
        genericUser.setType(UserTypeEnum.GENERIC);

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "whatever", CUSTOMER_ID)).isInstanceOf(
            InvalidAuthenticationException.class
        );
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

    @Test
    void should_resolve_the_organization_of_a_single_known_user_covered_by_a_provider() {
        final IdentityProviderDto provider = buildProviderOf(CUSTOMER_ID);
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(List.of(buildUserOf(CUSTOMER_ID)));
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of(provider));
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(Optional.of(provider));
        givenTheCustomers(buildCustomer(CUSTOMER_ID, "code1", "Organisation 1"));

        assertThat(casService.resolveOrganizations(USER_EMAIL))
            .extracting(OrganizationCandidateDto::getCustomerId)
            .containsExactly(CUSTOMER_ID);
    }

    @Test
    void should_resolve_nothing_when_a_known_user_is_covered_by_no_provider() {
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(List.of(buildUserOf(CUSTOMER_ID)));
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(Optional.empty());

        assertThat(casService.resolveOrganizations(USER_EMAIL)).isEmpty();
    }

    @Test
    void should_resolve_every_organization_of_a_user_known_in_several_of_them() {
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(
            List.of(buildUserOf(CUSTOMER_ID), buildUserOf("customerID2"))
        );
        givenTheCustomers(
            buildCustomer(CUSTOMER_ID, "code1", "Organisation 1"),
            buildCustomer("customerID2", "code2", "Organisation 2")
        );

        assertThat(casService.resolveOrganizations(USER_EMAIL))
            .extracting(OrganizationCandidateDto::getCustomerId)
            .containsExactly(CUSTOMER_ID, "customerID2");
    }

    @Test
    void should_fall_back_on_the_providers_covering_the_address_when_no_user_is_known() {
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(List.of());
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(identityProviderHelper.findAllProvidersByUserIdentifier(any(), eq(USER_EMAIL))).thenReturn(
            List.of(buildProviderOf(CUSTOMER_ID))
        );
        givenTheCustomers(buildCustomer(CUSTOMER_ID, "code1", "Organisation 1"));

        assertThat(casService.resolveOrganizations(USER_EMAIL))
            .extracting(OrganizationCandidateDto::getCustomerId)
            .containsExactly(CUSTOMER_ID);
    }

    @Test
    void should_repeat_an_organization_covering_an_unknown_address_with_two_providers() {
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(List.of());
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(identityProviderHelper.findAllProvidersByUserIdentifier(any(), eq(USER_EMAIL))).thenReturn(
            List.of(buildProviderOf(CUSTOMER_ID), buildProviderOf(CUSTOMER_ID))
        );
        givenTheCustomers(buildCustomer(CUSTOMER_ID, "code1", "Organisation 1"));

        assertThat(casService.resolveOrganizations(USER_EMAIL))
            .extracting(OrganizationCandidateDto::getCustomerId)
            .containsExactly(CUSTOMER_ID, CUSTOMER_ID);
    }

    @Test
    void should_resolve_nothing_when_neither_a_user_nor_a_provider_matches() {
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(List.of());
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(identityProviderHelper.findAllProvidersByUserIdentifier(any(), eq(USER_EMAIL))).thenReturn(List.of());

        assertThat(casService.resolveOrganizations(USER_EMAIL)).isEmpty();
    }

    @Test
    void should_normalize_the_identifier_before_resolving() {
        when(userService.findUsersByEmail(USER_EMAIL)).thenReturn(List.of());
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(identityProviderHelper.findAllProvidersByUserIdentifier(any(), eq(USER_EMAIL))).thenReturn(List.of());

        assertThat(casService.resolveOrganizations("  " + USER_EMAIL.toUpperCase() + " ")).isEmpty();
    }

    @Test
    void should_key_the_refusal_when_the_password_does_not_match_the_policy() {
        givenAnEnabledUserAndCustomer();
        givenAnInternalIdentityProvider();
        when(passwordConfiguration.getPolicyPattern()).thenReturn(POLICY_PATTERN);
        when(passwordValidator.isValid(POLICY_PATTERN, "weak")).thenReturn(false);

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "weak", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class)
            .extracting("key")
            .isEqualTo(PasswordChangeErrorKeys.POLICY_NOT_MATCHED);
    }

    @Test
    void should_key_the_refusal_when_the_user_has_no_identity_provider() {
        givenAnEnabledUserAndCustomer();
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString())).thenReturn(
            Optional.empty()
        );

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "whatever", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class)
            .extracting("key")
            .isEqualTo(PasswordChangeErrorKeys.NO_IDENTITY_PROVIDER);
    }

    @Test
    void should_key_the_refusal_when_the_user_is_behind_an_external_provider() {
        givenAnEnabledUserAndCustomer();
        final IdentityProviderDto externalProvider = buildIDP(false);
        externalProvider.setInternal(false);
        when(identityProviderHelper.findByUserIdentifierAndCustomerId(any(), anyString(), anyString())).thenReturn(
            Optional.of(externalProvider)
        );

        assertThatThrownBy(() -> casService.updatePassword(USER_EMAIL, "whatever", CUSTOMER_ID))
            .isInstanceOf(BadRequestException.class)
            .extracting("key")
            .isEqualTo(PasswordChangeErrorKeys.EXTERNAL_IDENTITY_PROVIDER);
    }

    @Test
    void should_resolve_the_internal_provider_covering_an_identifier_in_an_organization() {
        final IdentityProviderDto provider = buildProviderOf(CUSTOMER_ID);
        provider.setId(IDP);
        provider.setInternal(true);
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of(provider));
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(Optional.of(provider));

        final ResolvedIdentityProviderDto resolved = casService.resolveIdentityProvider(USER_EMAIL, CUSTOMER_ID);

        assertThat(resolved.getIdentityProviderId()).isEqualTo(IDP);
        assertThat(resolved.isInternal()).isTrue();
    }

    @Test
    void should_resolve_an_external_provider_as_not_internal() {
        final IdentityProviderDto provider = buildProviderOf(CUSTOMER_ID);
        provider.setId(IDP);
        provider.setInternal(false);
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of(provider));
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(Optional.of(provider));

        final ResolvedIdentityProviderDto resolved = casService.resolveIdentityProvider(USER_EMAIL, CUSTOMER_ID);

        assertThat(resolved.getIdentityProviderId()).isEqualTo(IDP);
        assertThat(resolved.isInternal()).isFalse();
    }

    @Test
    void should_resolve_no_provider_when_none_covers_the_identifier() {
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(Optional.empty());

        final ResolvedIdentityProviderDto resolved = casService.resolveIdentityProvider(USER_EMAIL, CUSTOMER_ID);

        assertThat(resolved.getIdentityProviderId()).isNull();
        assertThat(resolved.isInternal()).isFalse();
    }

    @Test
    void should_normalize_the_identifier_before_resolving_the_provider() {
        when(identityProviderService.getAll(any(), any())).thenReturn(List.of());
        when(
            identityProviderHelper.findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID))
        ).thenReturn(Optional.empty());

        casService.resolveIdentityProvider("  " + USER_EMAIL.toUpperCase() + " ", CUSTOMER_ID);

        verify(identityProviderHelper).findByUserIdentifierAndCustomerId(any(), eq(USER_EMAIL), eq(CUSTOMER_ID));
    }

    private void givenTheCustomers(final CustomerDto... customers) {
        when(customerService.getAllById(any())).thenReturn(List.of(customers));
    }

    private UserDto buildUserOf(final String customerId) {
        final UserDto user = new UserDto();
        user.setEmail(USER_EMAIL);
        user.setCustomerId(customerId);
        return user;
    }

    private IdentityProviderDto buildProviderOf(final String customerId) {
        final IdentityProviderDto provider = new IdentityProviderDto();
        provider.setCustomerId(customerId);
        return provider;
    }

    private CustomerDto buildCustomer(final String id, final String code, final String name) {
        final CustomerDto customer = new CustomerDto();
        customer.setId(id);
        customer.setCode(code);
        customer.setName(name);
        return customer;
    }

    private IdentityProviderDto buildIDP(final boolean autoProvisioningEnabled) {
        final IdentityProviderDto idp = new IdentityProviderDto();
        idp.setId(IDP);
        idp.setCustomerId(CUSTOMER_ID);
        idp.setAutoProvisioningEnabled(autoProvisioningEnabled);
        return idp;
    }
}
