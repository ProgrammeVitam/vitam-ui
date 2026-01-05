package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.logout.TerminateApiSessionAction;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the customized {@link TerminateApiSessionAction}.
 */
@ContextConfiguration(classes = TerminateApiSessionActionTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class TerminateApiSessionActionTest extends BaseWebflowActionTest {

    private static final String LOGOUT_URL = "http://dev.vitamui.com:8080/cas/app1/callback";

    @Test
    public void test() {
        final ServicesManager servicesManager = mock(ServicesManager.class);
        RegexRegisteredService registeredService = new RegexRegisteredService();
        registeredService.setLogoutUrl(LOGOUT_URL);
        when(servicesManager.getAllServices()).thenReturn(Arrays.asList(registeredService));

        final LogoutManager logoutManager = mock(LogoutManager.class);

        final TerminateApiSessionAction action = new TerminateApiSessionAction(
            null,
            null,
            null,
            null,
            logoutManager,
            null,
            null,
            null,
            null,
            null
        );
        // TODO: Check how to fix
        // action.performGeneralLogout("tgtId");
    }
}
