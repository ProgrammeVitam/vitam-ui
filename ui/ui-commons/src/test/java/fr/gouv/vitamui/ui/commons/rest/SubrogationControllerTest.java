package fr.gouv.vitamui.ui.commons.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.config.UIPropertiesImpl;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import fr.gouv.vitamui.ui.commons.service.SubrogationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = {UICommonsAutoConfiguration.class, UICommonsAutoSpringMockConfiguration.class})
@WebMvcTest(controllers = {SubrogationController.class})
@Import(value = {SecurityConfig.class, ServerIdentityConfiguration.class})
public class SubrogationControllerTest extends UIControllerTest<SubrogationDto> {

    @TestConfiguration
    static class UserConfiguration {

        @Bean
        public UIProperties uiProperties() {
            final UIPropertiesImpl properties = new UIPropertiesImpl();
            properties.setIamExternalClient(new RestClientConfiguration());
            return properties;
        }


    }

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationControllerTest.class);

    @Value("${ui-prefix}")
    protected String apiUrl;

    @Value("${controller.subrogation.enabled}")
    private boolean subrogationEnabled;

    private final String PREFIX = "/subrogations";

    @MockBean
    private SubrogationService service;

    @MockBean
    private BuildProperties buildProperties;

    @Test
    public void testSubrogationControllerEnabled() {
        assertThat(subrogationEnabled).isTrue();
    }

    @Test
    public void testDeleteSubrogation() {
        super.testDeleteEntity();
    }

    @Test
    public void testCreate() {
        super.testCreateEntity();
    }

    @Test
    public void testGetItemById() {
        super.testGetEntityById();
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
