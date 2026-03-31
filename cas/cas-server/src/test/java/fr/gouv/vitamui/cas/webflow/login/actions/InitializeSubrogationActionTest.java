package fr.gouv.vitamui.cas.webflow.login.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Event;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = InitializeSubrogationActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class InitializeSubrogationActionTest extends BaseWebflowActionTest {

    private CasApi casApi;
    private InitializeSubrogationAction initializeSubrogationAction;

    @Before
    public void before() {
        casApi = mock(CasApi.class);
        initializeSubrogationAction = new InitializeSubrogationAction(casApi);
    }

    @Test
    public void shouldReturnProceedWhenNoSubrogationParams() {
        // When
        Event event = initializeSubrogationAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(InitializeSubrogationAction.PROCEED);
    }

    @Test
    public void shouldReturnProceedWhenValidParams() {
        // Given
        requestParameters.put(Constants.LOGIN_SURROGATE_EMAIL_PARAM, "surrogate@vitamui.fr");
        requestParameters.put(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM, "customerSurrogate");
        requestParameters.put(Constants.LOGIN_SUPER_USER_EMAIL_PARAM, "admin@vitamui.fr");
        requestParameters.put(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM, "customerAdmin");

        CustomerDto customerDto = new CustomerDto();
        customerDto.setCode("SURR");
        customerDto.setName("Surrogate Customer");
        when(casApi.getCustomersByIds(anyList())).thenReturn(List.of(customerDto));

        // When
        Event event = initializeSubrogationAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(InitializeSubrogationAction.PROCEED);
        assertThat(flowParameters.get(Constants.FLOW_SURROGATE_EMAIL)).isEqualTo("surrogate@vitamui.fr");
        assertThat(flowParameters.get(Constants.FLOW_SURROGATE_CUSTOMER_ID)).isEqualTo("customerSurrogate");
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo("admin@vitamui.fr");
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo("customerAdmin");

        assertThat(flowParameters.get("userEmail")).isEqualTo("surrogate@vitamui.fr");
        assertThat(flowParameters.get("userCustomerId")).isEqualTo("customerSurrogate");
        assertThat(flowParameters.get("superUserEmail")).isEqualTo("admin@vitamui.fr");
        assertThat(flowParameters.get("superUserCustomerId")).isEqualTo("customerAdmin");

        assertThat(flowParameters.get(Constants.SHOW_SURROGATE_CUSTOMER_CODE)).isEqualTo("SURR");
        assertThat(flowParameters.get(Constants.SHOW_SURROGATE_CUSTOMER_NAME)).isEqualTo("Surrogate Customer");
    }

    @Test
    public void shouldReturnProceedWhenInvalidEmail() {
        // Given
        requestParameters.put(Constants.LOGIN_SURROGATE_EMAIL_PARAM, "invalid-email");
        requestParameters.put(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM, "customerSurrogate");
        requestParameters.put(Constants.LOGIN_SUPER_USER_EMAIL_PARAM, "admin@vitamui.fr");
        requestParameters.put(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM, "customerAdmin");

        // When
        Event event = initializeSubrogationAction.doExecute(context);

        // Then
        assertThat(event.getId()).isEqualTo(InitializeSubrogationAction.PROCEED);
    }
}
