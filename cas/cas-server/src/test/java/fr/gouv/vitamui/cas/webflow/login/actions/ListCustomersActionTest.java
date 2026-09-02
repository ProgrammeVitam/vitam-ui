package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Event;

import java.io.IOException;
import java.util.List;

import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTION_VIEW;
import static fr.gouv.vitamui.cas.webflow.login.actions.ListCustomersAction.BAD_CONFIGURATION;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
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
    private CasApi casApi;
    private ListCustomersAction listCustomersAction;

    @Before
    public void before() {
        casApi = mock(CasApi.class);
        listCustomersAction = new ListCustomersAction(casApi);
    }

    @Test
    public void testSubrogationThenNoCustomerSelection() throws IOException {
        // Étant donné
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(List.of(hrd(CUSTOMER_ID_1, "code1", "customer1", "provider1"))).when(casApi).resolveHrd(eq(EMAIL1));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @Test
    public void shouldTriggerOrganizationSelectionWhenSubrogatedUserHasManyOrganizationOrIdentityProviders()
        throws IOException {
        // Étant donné
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL1);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(
            List.of(
                hrd(CUSTOMER_ID_1, "code1", "customer1", "provider1"),
                hrd(CUSTOMER_ID_2, "code2", "customer2", "provider2")
            )
        )
            .when(casApi)
            .resolveHrd(eq(EMAIL1));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors (le mode subrogation est déterministe et court-circuite la sélection du customer)
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @Test
    public void testSubrogationWithInvalidProviderThenBadConfig() throws IOException {
        // Étant donné
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL_UNKNOWN_DOMAIN);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID_1);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, EMAIL2);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID_2);
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL_UNKNOWN_DOMAIN, "password"));

        doReturn(emptyList()).when(casApi).resolveHrd(eq(EMAIL_UNKNOWN_DOMAIN));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    @Test
    public void testLoginWithEmailMatchingASingleUser() throws IOException {
        // Étant donné
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(List.of(hrd(CUSTOMER_ID_1, "code1", "customer1", "provider1"))).when(casApi).resolveHrd(eq(EMAIL1));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST)).isNull();
    }

    @Test
    public void testLoginWithEmailMatchingMultipleUsers() throws IOException {
        // Étant donné
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(
            List.of(
                hrd(CUSTOMER_ID_1, "MyCode1", "MyCustomer1", "provider1"),
                hrd(CUSTOMER_ID_2, "MyCode2", "MyCustomer2", "provider2")
            )
        )
            .when(casApi)
            .resolveHrd(eq(EMAIL1));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
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

        doReturn(List.of(hrd(CUSTOMER_ID_2, "code2", "customer2", "provider2"))).when(casApi).resolveHrd(eq(EMAIL2));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL2);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_2);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST)).isNull();
    }

    @Test
    public void testLoginWithUnknownUserMatchingMultipleCustomerMailDomain() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(
            List.of(
                hrd(CUSTOMER_ID_1, "MyCode1", "MyCustomer1", "provider1"),
                hrd(CUSTOMER_ID_2, "MyCode2", "MyCustomer2", "provider2")
            )
        )
            .when(casApi)
            .resolveHrd(eq(EMAIL1));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
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

        doReturn(emptyList()).when(casApi).resolveHrd(eq(EMAIL_UNKNOWN_DOMAIN));

        // Quand
        Event event = listCustomersAction.doExecute(context);

        // Alors
        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    private static HrdEntryDto hrd(String customerId, String code, String name, String providerId) {
        HrdEntryDto entry = new HrdEntryDto();
        entry.setCustomerId(customerId);
        entry.setCustomerCode(code);
        entry.setCustomerName(name);
        entry.setIdentityProviderId(providerId);
        return entry;
    }
}
