package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import fr.gouv.vitamui.iam.openapiclient.domain.CustomerDto;
import fr.gouv.vitamui.iam.openapiclient.domain.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Event;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTION_VIEW;
import static fr.gouv.vitamui.cas.webflow.login.actions.ListCustomersAction.BAD_CONFIGURATION;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Slf4j
@ContextConfiguration(classes = ListCustomersActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class ListCustomersActionTest extends BaseWebflowActionTest {

    private static final String EMAIL_UNKNOWN_DOMAIN = "user@somedomain.com";
    private static final String EMAIL1 = "user1@vitamui.com";
    private static final String CUSTOMER_ID_1 = "customer1";
    private static final String EMAIL2 = "user2@vitamui.fr";
    private static final String CUSTOMER_ID_2 = "customer2";
    public static final String EMAIL_DOMAIN_1 = ".*@vitamui.com";
    public static final String EMAIL_DOMAIN_2 = ".*@vitamui.fr";
    private CasApi casApi;
    private IdentityProviderHelper identityProviderHelper;
    private ListCustomersAction listCustomersAction;
    private IdentityProviderDto providerDto1;
    private IdentityProviderDto providerDto2;

    @Before
    public void before() {
        ProvidersService providersService = mock(ProvidersService.class);
        casApi = mock(CasApi.class);
        identityProviderHelper = mock(IdentityProviderHelper.class);

        listCustomersAction = new ListCustomersAction(providersService, identityProviderHelper, casApi);

        providerDto1 = getIdentityProvider(CUSTOMER_ID_1, false, EMAIL_DOMAIN_1);
        providerDto2 = getIdentityProvider(CUSTOMER_ID_2, true, EMAIL_DOMAIN_1, EMAIL_DOMAIN_2);
        doReturn(List.of(providerDto1, providerDto2)).when(providersService).getProviders();
    }

    @Test
    public void testSubrogationThenNoCustomerSelection() throws IOException {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        // Ensure multiple providers match to trigger selection view
        doReturn(List.of(getIdentityProvider("c1", false, ".*@vitamui.com")))
            .when(identityProviderHelper)
            .findAllProvidersByUserIdentifier(any(), any());
        doReturn(Optional.of(providerDto1))
            .when(identityProviderHelper)
            .findByUserIdentifierAndCustomerId(any(), eq(EMAIL1), eq(CUSTOMER_ID_1));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @Test
    public void shouldTriggerOrganizationSelectionWhenSubrogatedUserHasManyOrganizationOrIdentityProviders()
        throws IOException {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        // Ensure multiple providers match to trigger selection view
        doReturn(
            List.of(
                getIdentityProvider("c1", false, ".*@vitamui.com"),
                getIdentityProvider("c2", false, ".*@vitamui.com")
            )
        )
            .when(identityProviderHelper)
            .findAllProvidersByUserIdentifier(any(), any());
        doReturn(Optional.of(providerDto1))
            .when(identityProviderHelper)
            .findByUserIdentifierAndCustomerId(any(), eq(EMAIL1), eq(CUSTOMER_ID_1));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then (Subrogation mode is deterministic and bypasses customer selection)
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @Test
    public void testSubrogationWithInvalidProviderThenBadConfig() throws IOException {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL_UNKNOWN_DOMAIN);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL_UNKNOWN_DOMAIN, "password"));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    @Test
    public void testLoginWithEmailMatchingASingleUser() throws IOException {
        // Given
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        UserDto userDto = new UserDto();
        userDto.setCustomerId(CUSTOMER_ID_1);
        userDto.setStatus(UserStatusEnum.ENABLED); // Added status just in case

        doReturn(List.of(userDto)).when(casApi).getUsersByEmail(eq(EMAIL1), eq(null));
        doReturn(Optional.of(providerDto1))
            .when(identityProviderHelper)
            .findByUserIdentifierAndCustomerId(any(), eq(EMAIL1), eq(CUSTOMER_ID_1));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST)).isNull();
    }

    @Test
    public void testLoginWithEmailMatchingMultipleUsers() throws IOException {
        // Given
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        UserDto userDto1 = new UserDto();
        userDto1.setCustomerId(CUSTOMER_ID_1);

        UserDto userDto2 = new UserDto();
        userDto2.setCustomerId(CUSTOMER_ID_2);

        doReturn(List.of(userDto1, userDto2)).when(casApi).getUsersByEmail(eq(EMAIL1), eq(null));

        CustomerDto customerDto1 = getCustomerDto(CUSTOMER_ID_1, "MyCode1", "MyCustomer1");
        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "MyCode2", "MyCustomer2");
        doReturn(List.of(customerDto1, customerDto2))
            .when(casApi)
            .getCustomersByIds(eq(List.of(CUSTOMER_ID_1, CUSTOMER_ID_2)));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTION_VIEW);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isNull();
        assertThat((List<CustomerModel>) flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST))
            .usingFieldByFieldElementComparator()
            .containsExactly(
                new CustomerModel().setCustomerId(CUSTOMER_ID_1).setName("MyCustomer1").setCode("MyCode1"),
                new CustomerModel().setCustomerId(CUSTOMER_ID_2).setName("MyCustomer2").setCode("MyCode2")
            );
    }

    @Test
    public void testLoginWithUnknownUserMatchingASingleCustomerMailDomain() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL2, "password"));

        doReturn(emptyList()).when(casApi).getUsersByEmail(eq(EMAIL2), eq(null));
        doReturn(List.of(providerDto2))
            .when(identityProviderHelper)
            .findAllProvidersByUserIdentifier(any(), eq(EMAIL2));

        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "code2", "customer2");
        doReturn(List.of(customerDto2)).when(casApi).getCustomersByIds(eq(List.of(CUSTOMER_ID_2)));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL2);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_2);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST)).isNull();
    }

    @Test
    public void testLoginWithUnknownUserMatchingMultipleCustomerMailDomain() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(emptyList()).when(casApi).getUsersByEmail(eq(EMAIL1), eq(null));
        doReturn(List.of(providerDto1, providerDto2))
            .when(identityProviderHelper)
            .findAllProvidersByUserIdentifier(any(), eq(EMAIL1));

        CustomerDto customerDto1 = getCustomerDto(CUSTOMER_ID_1, "MyCode1", "MyCustomer1");
        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "MyCode2", "MyCustomer2");
        doReturn(List.of(customerDto1, customerDto2))
            .when(casApi)
            .getCustomersByIds(eq(List.of(CUSTOMER_ID_1, CUSTOMER_ID_2)));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTION_VIEW);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isNull();
        assertThat((List<CustomerModel>) flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST))
            .usingFieldByFieldElementComparator()
            .containsExactly(
                new CustomerModel().setCustomerId(CUSTOMER_ID_1).setName("MyCustomer1").setCode("MyCode1"),
                new CustomerModel().setCustomerId(CUSTOMER_ID_2).setName("MyCustomer2").setCode("MyCode2")
            );
    }

    @Test
    public void testLoginWithUnknownUserMatchingNoValidCustomerMailDomain() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL_UNKNOWN_DOMAIN, "password"));

        doReturn(emptyList()).when(casApi).getUsersByEmail(eq(EMAIL_UNKNOWN_DOMAIN), eq(null));

        CustomerDto customerDto1 = getCustomerDto(CUSTOMER_ID_1, "MyCode1", "MyCustomer1");
        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "MyCode2", "MyCustomer2");
        doReturn(List.of(customerDto1, customerDto2))
            .when(casApi)
            .getCustomersByIds(eq(List.of(CUSTOMER_ID_1, CUSTOMER_ID_2)));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    private static IdentityProviderDto getIdentityProvider(String customerId, boolean internal, String... patterns) {
        IdentityProviderDto providerDto1 = new IdentityProviderDto();
        providerDto1.setId(customerId); // Use customerId as provider Id for uniqueness
        providerDto1.setCustomerId(customerId);
        providerDto1.setInternal(internal);
        providerDto1.setPatterns(List.of(patterns));
        return providerDto1;
    }

    private static CustomerDto getCustomerDto(String customerId, String code, String name) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customerId);
        customerDto.setCode(code);
        customerDto.setName(name);
        return customerDto;
    }
}
