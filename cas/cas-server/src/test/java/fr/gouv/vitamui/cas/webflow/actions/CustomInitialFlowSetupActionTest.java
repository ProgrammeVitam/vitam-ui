package fr.gouv.vitamui.cas.webflow.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;

/**
 * Tests {@link CustomInitialFlowSetupAction}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class CustomInitialFlowSetupActionTest extends BaseWebflowActionTest {

    private static final String EMAIL1 = "julien@vitamui.com";

    private static final String EMAIL2 = "pierre@vitamui.com";

    private CustomInitialFlowSetupAction action;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        action = new CustomInitialFlowSetupAction(new ArrayList<>(), mock(ServicesManager.class),
                mock(AuthenticationServiceSelectionPlan.class), mock(CookieRetrievingCookieGenerator.class),
                mock(CookieRetrievingCookieGenerator.class), new CasConfigurationProperties());
        action.setSeparator(",");
    }

    @Test
    public void testUsernameNoSubrogation() {
        requestParameters.put("username", EMAIL1);

        action.doExecute(context);

        assertEquals(EMAIL1, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals(null, flowParameters.get("surrogate"));
        assertEquals(null, flowParameters.get("superUser"));
    }

    @Test
    public void testUsernameRequestedSubrogation() {
        requestParameters.put("username", "," + EMAIL2);

        action.doExecute(context);

        assertEquals(EMAIL2, ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals(null, flowParameters.get("surrogate"));
        assertEquals(null, flowParameters.get("superUser"));
    }

    @Test
    public void testUsernameWithSubrogation() {
        requestParameters.put("username", EMAIL1 + "," + EMAIL2);

        action.doExecute(context);

        assertEquals(EMAIL1 + "," + EMAIL2,
                ((UsernamePasswordCredential) flowParameters.get("credential")).getUsername());
        assertEquals(EMAIL1, flowParameters.get("surrogate"));
        assertEquals(EMAIL2, flowParameters.get("superUser"));
    }

    @Test
    public void testNoUsername() {
        action.doExecute(context);

        assertEquals(null, flowParameters.get("credential"));
        assertEquals(null, flowParameters.get("surrogate"));
        assertEquals(null, flowParameters.get("superUser"));
    }
}
