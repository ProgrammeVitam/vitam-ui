package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.cas.OrganizationCandidateDto;
import fr.gouv.vitamui.iam.common.dto.cas.ResolvedIdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
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
    private static final String PROVIDER_ID = "providerId";
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

        listCustomersAction = new ListCustomersAction(casApi);

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

        doReturn(new ResolvedIdentityProviderDto(PROVIDER_ID, true))
            .when(casApi)
            .resolveIdentityProvider(eq(EMAIL1), eq(CUSTOMER_ID_1));

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

        doReturn(new ResolvedIdentityProviderDto(PROVIDER_ID, true))
            .when(casApi)
            .resolveIdentityProvider(eq(EMAIL1), eq(CUSTOMER_ID_1));

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

        doReturn(new ResolvedIdentityProviderDto(null, false))
            .when(casApi)
            .resolveIdentityProvider(eq(EMAIL_UNKNOWN_DOMAIN), eq(CUSTOMER_ID_1));

        // When
        Event event = listCustomersAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    @Test
    public void testLoginWhenASingleOrganizationClaimsTheEmail() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(List.of(organization(CUSTOMER_ID_1, "MyCode1", "MyCustomer1")))
            .when(casApi)
            .resolveOrganizations(eq(EMAIL1));

        Event event = listCustomersAction.doExecute(context);

        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST)).isNull();
    }

    @Test
    public void testLoginWhenSeveralOrganizationsClaimTheEmail() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(
            List.of(
                organization(CUSTOMER_ID_1, "MyCode1", "MyCustomer1"),
                organization(CUSTOMER_ID_2, "MyCode2", "MyCustomer2")
            )
        )
            .when(casApi)
            .resolveOrganizations(eq(EMAIL1));

        Event event = listCustomersAction.doExecute(context);

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
    public void testTheOrganizationsAreOfferedSortedByCode() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(
            List.of(
                organization(CUSTOMER_ID_2, "MyCode2", "MyCustomer2"),
                organization(CUSTOMER_ID_1, "MyCode1", "MyCustomer1")
            )
        )
            .when(casApi)
            .resolveOrganizations(eq(EMAIL1));

        listCustomersAction.doExecute(context);

        assertThat((List<CustomerModel>) flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST))
            .extracting(CustomerModel::getCode)
            .containsExactly("MyCode1", "MyCode2");
    }

    @Test
    public void testLoginWhenNoOrganizationClaimsTheEmail() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL_UNKNOWN_DOMAIN, "password"));

        doReturn(List.of()).when(casApi).resolveOrganizations(eq(EMAIL_UNKNOWN_DOMAIN));

        Event event = listCustomersAction.doExecute(context);

        assertThat(event.getId()).isEqualTo(BAD_CONFIGURATION);
    }

    @Test
    public void testAnOrganizationClaimingTwiceIsOfferedOnlyOnce() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential(EMAIL1, "password"));

        doReturn(
            List.of(
                organization(CUSTOMER_ID_1, "MyCode1", "MyCustomer1"),
                organization(CUSTOMER_ID_1, "MyCode1", "MyCustomer1")
            )
        )
            .when(casApi)
            .resolveOrganizations(eq(EMAIL1));

        Event event = listCustomersAction.doExecute(context);

        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTION_VIEW);
        assertThat((List<CustomerModel>) flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST))
            .usingFieldByFieldElementComparator()
            .containsExactly(
                new CustomerModel().setCustomerId(CUSTOMER_ID_1).setName("MyCustomer1").setCode("MyCode1")
            );
    }

    @Test
    public void testTheEmailIsNormalizedBeforeBeingResolved() throws IOException {
        flowParameters.put("credential", new UsernamePasswordCredential("  " + EMAIL1.toUpperCase() + " ", "pwd"));

        doReturn(List.of(organization(CUSTOMER_ID_1, "MyCode1", "MyCustomer1")))
            .when(casApi)
            .resolveOrganizations(eq(EMAIL1));

        Event event = listCustomersAction.doExecute(context);

        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
    }

    private static OrganizationCandidateDto organization(String customerId, String code, String name) {
        return new OrganizationCandidateDto(customerId, code, name);
    }

    private static IdentityProviderDto getIdentityProvider(String customerId, boolean internal, String... patterns) {
        IdentityProviderDto providerDto1 = new IdentityProviderDto();
        providerDto1.setId(customerId); // Use customerId as provider Id for uniqueness
        providerDto1.setCustomerId(customerId);
        providerDto1.setInternal(internal);
        providerDto1.setPatterns(List.of(patterns));
        return providerDto1;
    }

}
