package fr.gouv.vitamui.ingest;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.ingest.service.IngestService;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { SecurityConfig.class, ServerIdentityConfiguration.class, SwaggerConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=ui-ingest-application" })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private IngestService ingestService;


}
