package fr.gouv.vitamui.cas.config;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.util.Constants;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = CustomSurrogateInitialAuthenticationActionTest.class)
public class CustomSurrogateInitialAuthenticationActionTest extends BaseWebflowActionTest {

    private static final String USER = "user@domain.com";
    private static final String CUSTOMER_ID = "customerId";
    private static final String SUPER_USER = "superuser@domain.com";
    private static final String SUPER_CUSTOMER_ID = "superCustomerId";

    @Test
    public void testNoSubrogationThanCredentialUnchanged() throws Exception {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, USER);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, CUSTOMER_ID);
        flowParameters.remove(Constants.FLOW_SURROGATE_EMAIL);
        flowParameters.remove(Constants.FLOW_SURROGATE_CUSTOMER_ID);

        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredential(USER, "password");
        flowParameters.put("credential", usernamePasswordCredential);

        CustomSurrogateInitialAuthenticationAction instance = new CustomSurrogateInitialAuthenticationAction();

        // When
        instance.doExecute(context);

        // Then
        assertThat(flowParameters.get("credential")).isEqualTo(usernamePasswordCredential);
    }

    @Test
    public void testSubrogationThanCredentialChanged() throws Exception {
        // Given
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, SUPER_USER);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, SUPER_CUSTOMER_ID);
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, USER);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, CUSTOMER_ID);

        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredential(
            SUPER_CUSTOMER_ID,
            "password"
        );
        flowParameters.put("credential", usernamePasswordCredential);

        CustomSurrogateInitialAuthenticationAction instance = new CustomSurrogateInitialAuthenticationAction();

        // When
        instance.doExecute(context);

        // Then
        Object credential = flowParameters.get("credential");
        assertThat(credential).isInstanceOf(SurrogateUsernamePasswordCredential.class);
        SurrogateUsernamePasswordCredential surrogateUsernamePasswordCredential =
            (SurrogateUsernamePasswordCredential) credential;
        assertThat(surrogateUsernamePasswordCredential.getUsername()).isEqualTo(SUPER_USER);
        assertThat(surrogateUsernamePasswordCredential.getSurrogateUsername()).isEqualTo(USER);
    }
}
