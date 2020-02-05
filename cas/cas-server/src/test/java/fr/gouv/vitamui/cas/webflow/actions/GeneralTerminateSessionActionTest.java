package fr.gouv.vitamui.cas.webflow.actions;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Tests the customized {@link GeneralTerminateSessionAction}.
 *
 *
 */
public final class GeneralTerminateSessionActionTest {

    private static final String LOGOUT_URL = "http://dev.vitamui.com:8080/cas/app1/callback";

    @Test
    public void test() throws Exception {
        final ServicesManager servicesManager = mock(ServicesManager.class);
        RegexRegisteredService registeredService = new RegexRegisteredService();
        registeredService.setLogoutUrl(new URL(LOGOUT_URL));
        when(servicesManager.getAllServices()).thenReturn(Arrays.asList(registeredService));

        final LogoutManager logoutManager = mock(LogoutManager.class);

        final GeneralTerminateSessionAction action = new GeneralTerminateSessionAction(null, null, null, null);
        action.setServicesManager(servicesManager);
        action.setLogoutManager(logoutManager);

        action.performGeneralLogout("tgtId");
    }
}
