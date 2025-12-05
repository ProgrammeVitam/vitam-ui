package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.surrogation.IamSurrogateAuthenticationService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import fr.gouv.vitamui.iam.openapiclient.domain.SubrogationDto;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link IamSurrogateAuthenticationService}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = IamSurrogateAuthenticationServiceTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class IamSurrogateAuthenticationServiceTest {

    private static final String SURROGATE = "surrogate";
    private static final String SURROGATE_CUSTOMER_ID = "surrogate_customer_id";
    private static final String SU_ID = "id";
    private static final String SU_EMAIL = "superUser";
    private static final String SU_CUSTOMER_ID = "superUserCustomerId";

    private IamSurrogateAuthenticationService service;
    private CasApi casApi;

    @Before
    public void setUp() {
        casApi = mock(CasApi.class);
        service = new IamSurrogateAuthenticationService(casApi, mock(ServicesManager.class));
    }

    @After
    public void after() {
        RequestContextHolder.setRequestContext(null);
    }

    @Test
    public void testCanAuthenticateOk() {
        givenSubrogationInRequestContext();

        when(casApi.getSubrogationsBySuperUserIdOrEmailAndCustomerId(eq(SU_ID), eq(null), eq(null))).thenReturn(
            List.of(surrogation())
        );

        assertTrue(service.canImpersonateInternal(SURROGATE, principal(), Optional.empty()));
    }

    @Test
    public void testCanAuthenticateCannotSurrogate() {
        givenSubrogationInRequestContext();

        final var subrogation = surrogation();
        subrogation.setSurrogate("anotherUser");
        when(casApi.getSubrogationsBySuperUserIdOrEmailAndCustomerId(eq(SU_ID), eq(null), eq(null))).thenReturn(
            List.of(subrogation)
        );

        assertFalse(service.canImpersonateInternal(SURROGATE, principal(), Optional.empty()));
    }

    @Test
    public void testCanAuthenticateNotAccepted() {
        givenSubrogationInRequestContext();

        final var subrogation = surrogation();
        subrogation.setStatus(SubrogationStatusEnum.CREATED);
        when(casApi.getSubrogationsBySuperUserIdOrEmailAndCustomerId(eq(SU_ID), eq(null), eq(null))).thenReturn(
            List.of(subrogation)
        );

        assertFalse(service.canImpersonateInternal(SURROGATE, principal(), Optional.empty()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAccounts() {
        givenSubrogationInRequestContext();

        when(
            casApi.getSubrogationsBySuperUserIdOrEmailAndCustomerId(eq(null), eq(SU_EMAIL), eq(SU_CUSTOMER_ID))
        ).thenReturn(List.of(surrogation()));

        service.getImpersonationAccounts(SU_EMAIL);
    }

    private Principal principal() {
        final var factory = new DefaultPrincipalFactory();
        try {
            return factory.createPrincipal(SU_ID);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private SubrogationDto surrogation() {
        final var subrogation = new SubrogationDto();
        subrogation.setSurrogate(SURROGATE);
        subrogation.setSurrogateCustomerId(SURROGATE_CUSTOMER_ID);
        subrogation.setSuperUser(SU_EMAIL);
        subrogation.setSuperUserCustomerId(SU_CUSTOMER_ID);
        subrogation.setStatus(SubrogationStatusEnum.ACCEPTED);
        return subrogation;
    }

    private static void givenSubrogationInRequestContext() {
        RequestContext requestContext = mock(RequestContext.class);
        LocalAttributeMap<Object> flowParameters = new LocalAttributeMap<>();
        flowParameters.put(Constants.FLOW_SURROGATE_EMAIL, SURROGATE);
        flowParameters.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, SURROGATE_CUSTOMER_ID);
        flowParameters.put(Constants.FLOW_LOGIN_EMAIL, SU_EMAIL);
        flowParameters.put(Constants.FLOW_LOGIN_CUSTOMER_ID, SU_CUSTOMER_ID);
        doReturn(flowParameters).when(requestContext).getFlowScope();
        RequestContextHolder.setRequestContext(requestContext);
    }
}
