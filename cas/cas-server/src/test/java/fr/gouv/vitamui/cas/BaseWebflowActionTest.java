package fr.gouv.vitamui.cas;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockParameterMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;

/**
 * A base webflow action test.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public abstract class BaseWebflowActionTest {

    protected RequestContext context;

    protected MockParameterMap requestParameters;

    protected MutableAttributeMap<Object> flowParameters;

    protected HttpSession session;

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    @Before
    public void setUp() {
        context = mock(RequestContext.class);

        requestParameters = new MockParameterMap();
        when(context.getRequestParameters()).thenReturn(requestParameters);

        flowParameters = new LocalAttributeMap<>();
        when(context.getFlowScope()).thenReturn(flowParameters);
        flowParameters.put("service", mock(WebApplicationService.class));

        val flow = mock(Flow.class);
        when(flow.getVariable("credential")).thenReturn(mock(FlowVariable.class));

        when(context.getActiveFlow()).thenReturn(flow);
        when(context.getRequestScope()).thenReturn(flowParameters);
        when(context.getConversationScope()).thenReturn(flowParameters);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(context.getExternalContext()).thenReturn(servletExternalContext);
        request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        response = mock(HttpServletResponse.class);
        when(servletExternalContext.getNativeResponse()).thenReturn(response);

        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        final FlowExecutionContext flowContext = mock(FlowExecutionContext.class);
        when(context.getFlowExecutionContext()).thenReturn(flowContext);
        final FlowSession flowSession = mock(FlowSession.class);
        when(flowContext.getActiveSession()).thenReturn(flowSession);
        when(flowSession.getScope()).thenReturn(new LocalAttributeMap<>());

        RequestContextHolder.setRequestContext(context);
    }
}
