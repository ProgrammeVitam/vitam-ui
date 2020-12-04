package fr.gouv.vitamui.identity.rest;

import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.identity.config.IdentityApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.SubrogationController;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import fr.gouv.vitamui.ui.commons.service.SubrogationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = { UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class })
@WebMvcTest(controllers = { SubrogationController.class })
@Import(value = { IdentityApplicationProperties.class, SecurityConfig.class, ServerIdentityConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class SubrogationControllerTest extends UIControllerTest<SubrogationDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    private final String PREFIX = "/subrogations";

    @MockBean
    private SubrogationService service;
    @MockBean
    private BuildProperties buildProperties;


    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationControllerTest.class);

    @Test
    public void testGetSubrogationById() {
        super.testGetEntityById();
    }

    @Test
    public void testCreateSubrogation() {
        super.testCreateEntity();
    }

    @Test
    public void testUpdateSubrogation() {
        super.testUpdateEntityNotSupported();
    }

    @Test
    public void testDeleteSubrogation() {
        super.testDeleteEntity();
    }

    @Test
    public void testAccept() {
        super.performPatch("/surrogate/accept/1");
    }

    @Test
    public void testDecline() {
        super.performDelete("/surrogate/decline/1");
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<SubrogationDto> getDtoClass() {
        return SubrogationDto.class;
    }

    @Override
    protected SubrogationDto buildDto() {
        return new SubrogationDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.create(any(), any(SubrogationDto.class))).thenReturn(new SubrogationDto());
        Mockito.when(service.getOne(any(), any(), any())).thenReturn(new SubrogationDto());
    }
}
