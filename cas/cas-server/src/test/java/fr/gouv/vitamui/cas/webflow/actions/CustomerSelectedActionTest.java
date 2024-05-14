package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.model.CustomerModel;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.execution.Event;

import java.io.IOException;
import java.util.List;

import static fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer.TRANSITION_TO_CUSTOMER_SELECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class CustomerSelectedActionTest extends BaseWebflowActionTest {

    private static final String EMAIL = "user@vitamui.com";
    private static final String CUSTOMER_ID_1 = "customer1";
    private static final String CUSTOMER_ID_2 = "customer2";

    @Before
    public void before() {
        List<CustomerModel> customerModels = List.of(
            new CustomerModel().setCustomerId(CUSTOMER_ID_1),
            new CustomerModel().setCustomerId(CUSTOMER_ID_2)
        );
        this.flowParameters.put(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST, customerModels);
    }

    @Test
    public void testSelectedCustomerIdOK() throws IOException {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL);
        requestParameters.put(Constants.SELECT_CUSTOMER_ID_PARAM, CUSTOMER_ID_1);

        CustomerSelectedAction customerSelectedAction = new CustomerSelectedAction();
        Event event = customerSelectedAction.doExecute(context);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_AVAILABLE_CUSTOMER_LIST)).isNull();

        assertThat(event.getId()).isEqualTo(TRANSITION_TO_CUSTOMER_SELECTED);
    }

    @Test
    public void testSelectedCustomerIdInvalid() {
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, EMAIL);
        requestParameters.put(Constants.SELECT_CUSTOMER_ID_PARAM, "Invalid");

        CustomerSelectedAction customerSelectedAction = new CustomerSelectedAction();
        assertThatThrownBy(() -> customerSelectedAction.doExecute(context))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid customerId");
    }
}
