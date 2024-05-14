package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.execution.Event;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static fr.gouv.vitamui.cas.webflow.actions.ListCustomersAction.BAD_CONFIGURATION;
import static fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTION_VIEW;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class ListCustomersActionTest extends BaseWebflowActionTest {

    private static final String EMAIL_UNKNOWN_DOMAIN = "user@somedomain.com";
    private static final String EMAIL1 = "user1@vitamui.com";
    private static final String CUSTOMER_ID_1 = "customer1";
    private static final String EMAIL2 = "user2@vitamui.fr";
    private static final String CUSTOMER_ID_2 = "customer2";
    public static final String EMAIL_DOMAIN_1 = ".*@vitamui.com";
    public static final String EMAIL_DOMAIN_2 = ".*@vitamui.fr";
    private CasExternalRestClient casExternalRestClient;
    private ListCustomersAction listCustomersAction;

    @Before
    public void before() {
        ProvidersService providersService = mock(ProvidersService.class);
        casExternalRestClient = mock(CasExternalRestClient.class);

        final Utils utils = new Utils(null, 0, null, null, "");

        listCustomersAction = new ListCustomersAction(
            providersService,
            new IdentityProviderHelper(),
            casExternalRestClient,
            utils
        );

        IdentityProviderDto providerDto1 = getIdentityProvider(CUSTOMER_ID_1, false, EMAIL_DOMAIN_1);
        IdentityProviderDto providerDto2 = getIdentityProvider(CUSTOMER_ID_2, true, EMAIL_DOMAIN_1, EMAIL_DOMAIN_2);
        doReturn(List.of(providerDto1, providerDto2)).when(providersService).getProviders();
    }

    @Test
    public void testSubrogationThenNoCustomerSelection() throws IOException {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @Test
    public void testSubrogationWithInvalidProviderThenBadConfig() throws IOException {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL_UNKNOWN_DOMAIN);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);

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

        doReturn(List.of(userDto)).when(casExternalRestClient).getUsersByEmail(any(), eq(EMAIL1), eq(Optional.empty()));

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

        doReturn(List.of(userDto1, userDto2))
            .when(casExternalRestClient)
            .getUsersByEmail(any(), eq(EMAIL1), eq(Optional.empty()));

        CustomerDto customerDto1 = getCustomerDto(CUSTOMER_ID_1, "MyCode1", "MyCustomer1");
        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "MyCode2", "MyCustomer2");
        doReturn(List.of(customerDto1, customerDto2))
            .when(casExternalRestClient)
            .getCustomersByIds(any(), eq(List.of(CUSTOMER_ID_1, CUSTOMER_ID_2)));

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

        doReturn(emptyList()).when(casExternalRestClient).getUsersByEmail(any(), eq(EMAIL2), eq(Optional.empty()));

        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "code2", "customer2");
        doReturn(List.of(customerDto2))
            .when(casExternalRestClient)
            .getCustomersByIds(any(), eq(List.of(CUSTOMER_ID_2)));

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

        doReturn(emptyList()).when(casExternalRestClient).getUsersByEmail(any(), eq(EMAIL1), eq(Optional.empty()));

        CustomerDto customerDto1 = getCustomerDto(CUSTOMER_ID_1, "MyCode1", "MyCustomer1");
        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "MyCode2", "MyCustomer2");
        doReturn(List.of(customerDto1, customerDto2))
            .when(casExternalRestClient)
            .getCustomersByIds(any(), eq(List.of(CUSTOMER_ID_1, CUSTOMER_ID_2)));

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

        doReturn(emptyList())
            .when(casExternalRestClient)
            .getUsersByEmail(any(), eq(EMAIL_UNKNOWN_DOMAIN), eq(Optional.empty()));

        CustomerDto customerDto1 = getCustomerDto(CUSTOMER_ID_1, "MyCode1", "MyCustomer1");
        CustomerDto customerDto2 = getCustomerDto(CUSTOMER_ID_2, "MyCode2", "MyCustomer2");
        doReturn(List.of(customerDto1, customerDto2))
            .when(casExternalRestClient)
            .getCustomersByIds(any(), eq(List.of(CUSTOMER_ID_1, CUSTOMER_ID_2)));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    private static IdentityProviderDto getIdentityProvider(String customerId, boolean internal, String... patterns) {
        IdentityProviderDto providerDto1 = new IdentityProviderDto();
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
