package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.application.service.ApplicationService;
import fr.gouv.vitamui.iam.server.common.rest.ApiIamControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ApplicationController}.
 *
 *
 */
@WebMvcTest(controllers = { ApplicationController.class })
public final class ApplicationControllerTest extends ApiIamControllerTest<ApplicationDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationControllerTest.class);

    @Autowired
    ApplicationController applicationController;

    @MockitoBean
    private ApplicationService applicationService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
    }

    @Test
    public void testGetAll() {
        List<ApplicationDto> apps = Arrays.asList(buildDto());

        when(applicationService.getAll(any(), any())).thenReturn(apps);

        try {
            applicationController.getAll(Optional.empty());
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    @Test
    public void testGetAllApplications() {
        LOGGER.debug("testGetAllEntity");
        super.testGetAllEntity();
    }

    @Test
    public void testCreateNotSupported() {
        super.testCreateEntityNotSupported();
    }

    @Test
    public void testUpdateNotSupported() {
        super.testUpdateEntityNotSupported();
    }

    @Test
    public void testPatchNotSupported() {
        super.testPatchEntityNotSupported();
    }

    @Override
    protected ApplicationDto buildDto() {
        final ApplicationDto app = new ApplicationDto();
        app.setId("1");
        app.setIdentifier("id");
        app.setServiceId("serviceId");
        app.setIcon("icon");
        app.setName("name");
        app.setCategory("category");
        app.setPosition(0);
        app.setHasCustomerList(false);
        app.setHasTenantList(false);
        app.setHasHighlight(false);
        app.setUrl("url");
        app.setTooltip("tooltip");
        return app;
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_APPLICATIONS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_APPLICATIONS };
    }

    @Override
    protected Class<ApplicationDto> getDtoClass() {
        return ApplicationDto.class;
    }
}
