package fr.gouv.vitamui.cas.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;

import lombok.val;

/**
 * Tests {@link IamSurrogateAuthenticationService}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class IamSurrogateAuthenticationServiceTest {

    private static final String SURROGATE = "surrogate";

    private static final String SU_ID = "id";

    private static final String SU_EMAIL = "superUser";

    private IamSurrogateAuthenticationService service;

    private CasExternalRestClient casExternalRestClient;

    @Before
    public void setUp() {
        casExternalRestClient = mock(CasExternalRestClient.class);

        val utils = new Utils(null, 0, null, null);
        service = new IamSurrogateAuthenticationService(casExternalRestClient, mock(ServicesManager.class), utils);
    }

    @Test
    public void testCanAuthenticateOk() {
        when(casExternalRestClient.getSubrogationsBySuperUserId(any(ExternalHttpContext.class), eq(SU_ID))).thenReturn(Arrays.asList(surrogation()));

        assertTrue(service.canAuthenticateAsInternal(SURROGATE, principal(), null));
    }

    @Test
    public void testCanAuthenticateCannotSurrogate() {
        val subrogation = surrogation();
        subrogation.setSurrogate("anotherUser");
        when(casExternalRestClient.getSubrogationsBySuperUserId(any(ExternalHttpContext.class), eq(SU_ID))).thenReturn(Arrays.asList(subrogation));

        assertFalse(service.canAuthenticateAsInternal(SURROGATE, principal(), null));
    }

    @Test
    public void testCanAuthenticateNotAccepted() {
        val subrogation = surrogation();
        subrogation.setStatus(SubrogationStatusEnum.CREATED);
        when(casExternalRestClient.getSubrogationsBySuperUserId(any(ExternalHttpContext.class), eq(SU_ID))).thenReturn(Arrays.asList(subrogation));

        assertFalse(service.canAuthenticateAsInternal(SURROGATE, principal(), null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAccounts() {
        when(casExternalRestClient.getSubrogationsBySuperUserEmail(any(ExternalHttpContext.class), eq(SU_EMAIL))).thenReturn(Arrays.asList(surrogation()));

        service.getEligibleAccountsForSurrogateToProxy(SU_EMAIL);
    }

    private Principal principal() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(UserPrincipalResolver.SUPER_USER_ID_ATTRIBUTE, Arrays.asList(SU_ID));

        val factory = new DefaultPrincipalFactory();
        return factory.createPrincipal("x", attributes);
    }

    private SubrogationDto surrogation() {
        val subrogation = new SubrogationDto();
        subrogation.setSurrogate(SURROGATE);
        subrogation.setSuperUser(SU_EMAIL);
        subrogation.setStatus(SubrogationStatusEnum.ACCEPTED);
        return subrogation;
    }
}
