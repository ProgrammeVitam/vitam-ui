package fr.gouv.vitamui.security.server;

import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.security.server.certificate.service.CertificateCrudService;
import fr.gouv.vitamui.security.server.context.service.ContextService;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { SwaggerConfiguration.class })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private CertificateCrudService certificateCrudService;

    @MockBean
    private ContextService contextService;
}
