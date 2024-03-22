package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.val;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CustomDelegatedClientAuthenticationAction}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class CustomDelegatedClientAuthenticationActionTest extends BaseWebflowActionTest {

    private static final String EMAIL1 = "user1@vitamui.com";
    private static final String CUSTOMER_ID_1 = "customer1";
    private static final String EMAIL2 = "user2@vitamui.fr";
    private static final String CUSTOMER_ID_2 = "customer2";
    private static final String BAD_EMAIL = "ééééàààà@vitamui.com";
    private static final String BAD_CUSTOMER_ID = "ééééàààà";

    private CustomDelegatedClientAuthenticationAction action;

    @Override
    @Before
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        super.setUp();

        val configContext = mock(DelegatedClientAuthenticationConfigurationContext.class);
        when(configContext.getDelegatedClientIdentityProvidersProducer()).thenReturn(
            mock(DelegatedClientIdentityProviderConfigurationProducer.class));
        when(configContext.getDelegatedClientNameExtractor()).thenReturn(mock(DelegatedClientNameExtractor.class));
        action = new CustomDelegatedClientAuthenticationAction(configContext,
            mock(DelegatedClientAuthenticationWebflowManager.class),
            mock(DelegatedClientAuthenticationFailureEvaluator.class), mock(IdentityProviderHelper.class),
            mock(ProvidersService.class), mock(Utils.class), mock(TicketRegistry.class), "");
    }

    @Test
    public void testPreProvidedUsername() {
        requestParameters.put("username", EMAIL1);

        action.doExecute(context);

        assertThat(flowParameters.get(Constants.PROVIDED_USERNAME)).isEqualTo(EMAIL1);

        assertNull(flowParameters.get(Constants.FLOW_SURROGATE_EMAIL));
        assertNull(flowParameters.get(Constants.FLOW_SURROGATE_CUSTOMER_ID));
        assertNull(flowParameters.get(Constants.FLOW_LOGIN_EMAIL));
        assertNull(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID));
    }

    @Test
    public void testInvalidPreProvidedUsername() {
        requestParameters.put("username", BAD_EMAIL);

        assertThatThrownBy(() -> action.doExecute(context))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("format is not allowed");
    }

    @Test
    public void testSubrogation() {
        requestParameters.put(Constants.LOGIN_SUPER_USER_EMAIL_PARAM, EMAIL1);
        requestParameters.put(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM, CUSTOMER_ID_1);
        requestParameters.put(Constants.LOGIN_SURROGATE_EMAIL_PARAM, EMAIL2);
        requestParameters.put(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM, CUSTOMER_ID_2);

        action.doExecute(context);

        assertThat(flowParameters.get("credential")).isOfAnyClassIn(SurrogateUsernamePasswordCredential.class);
        SurrogateUsernamePasswordCredential credential =
            ((SurrogateUsernamePasswordCredential) flowParameters.get("credential"));
        assertThat(credential.getUsername()).isEqualTo(EMAIL1);
        assertThat(credential.getSurrogateUsername()).isEqualTo(EMAIL2);

        assertThat(flowParameters.get(Constants.FLOW_LOGIN_EMAIL)).isEqualTo(EMAIL1);
        assertThat(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_1);
        assertThat(flowParameters.get(Constants.FLOW_SURROGATE_EMAIL)).isEqualTo(EMAIL2);
        assertThat(flowParameters.get(Constants.FLOW_SURROGATE_CUSTOMER_ID)).isEqualTo(CUSTOMER_ID_2);

        assertNull(flowParameters.get(Constants.PROVIDED_USERNAME));
    }

    @Test
    public void testInvalidSubrogationEmail() {

        requestParameters.put(Constants.LOGIN_SUPER_USER_EMAIL_PARAM, BAD_EMAIL);
        requestParameters.put(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM, CUSTOMER_ID_1);
        requestParameters.put(Constants.LOGIN_SURROGATE_EMAIL_PARAM, EMAIL2);
        requestParameters.put(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM, CUSTOMER_ID_2);

        assertThatThrownBy(() -> action.doExecute(context))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("format is not allowed");
    }

    @Test
    public void testInvalidSubrogationCustomerId() {

        requestParameters.put(Constants.LOGIN_SUPER_USER_EMAIL_PARAM, EMAIL1);
        requestParameters.put(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM, CUSTOMER_ID_1);
        requestParameters.put(Constants.LOGIN_SURROGATE_EMAIL_PARAM, EMAIL2);
        requestParameters.put(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM, BAD_CUSTOMER_ID);

        assertThatThrownBy(() -> action.doExecute(context))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid customerId");
    }

    @Test
    public void testNoUsernameAndNoSubrogation() {
        action.doExecute(context);

        assertNull(flowParameters.get(Constants.PROVIDED_USERNAME));

        assertNull(flowParameters.get(Constants.FLOW_SURROGATE_EMAIL));
        assertNull(flowParameters.get(Constants.FLOW_SURROGATE_CUSTOMER_ID));
        assertNull(flowParameters.get(Constants.FLOW_LOGIN_EMAIL));
        assertNull(flowParameters.get(Constants.FLOW_LOGIN_CUSTOMER_ID));
    }
}
