package fr.gouv.vitamui.iam.external.server.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.ApplicationExternalService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { ApplicationExternalController.class })
public class ApplicationExternalControllerTest extends ApiIamControllerTest<ApplicationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationExternalControllerTest.class);

    @MockBean
    private ApplicationExternalService service;

    private final ApplicationExternalController mockedController = MvcUriComponentsBuilder.on(ApplicationExternalController.class);

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
        app.setUrl("url");
        app.setIdentifier("id");
        app.setId("1");
        return app;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
    }

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
