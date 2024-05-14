package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the customized {@link GeneralTerminateSessionAction}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class GeneralTerminateSessionActionTest extends BaseWebflowActionTest {

    private static final String LOGOUT_URL = "http://dev.vitamui.com:8080/cas/app1/callback";

    @Test
    public void test() {
        final ServicesManager servicesManager = mock(ServicesManager.class);
        RegexRegisteredService registeredService = new RegexRegisteredService();
        registeredService.setLogoutUrl(LOGOUT_URL);
        when(servicesManager.getAllServices()).thenReturn(Arrays.asList(registeredService));

        final LogoutManager logoutManager = mock(LogoutManager.class);

        final GeneralTerminateSessionAction action = new GeneralTerminateSessionAction(
            null,
            null,
            null,
            null,
            logoutManager,
            null,
            null,
            null,
            null,
            servicesManager,
            new CasConfigurationProperties(),
            null,
            null,
            null
        );

        action.performGeneralLogout("tgtId");
    }
}
