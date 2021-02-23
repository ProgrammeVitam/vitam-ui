package fr.gouv.vitamui.iam.external.server.utils;

import fr.gouv.vitamui.iam.external.server.service.ExternalParametersExternalService;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.iam.external.server.service.ApplicationExternalService;
import fr.gouv.vitamui.iam.external.server.service.CasExternalService;
import fr.gouv.vitamui.iam.external.server.service.CustomerExternalService;
import fr.gouv.vitamui.iam.external.server.service.GroupExternalService;
import fr.gouv.vitamui.iam.external.server.service.IdentityProviderExternalService;
import fr.gouv.vitamui.iam.external.server.service.LogbookExternalService;
import fr.gouv.vitamui.iam.external.server.service.OwnerExternalService;
import fr.gouv.vitamui.iam.external.server.service.ProfileExternalService;
import fr.gouv.vitamui.iam.external.server.service.SubrogationExternalService;
import fr.gouv.vitamui.iam.external.server.service.TenantExternalService;
import fr.gouv.vitamui.iam.external.server.service.UserExternalService;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { ServerIdentityConfiguration.class, SwaggerConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=iam-external-application" })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private CasExternalService casExternalService;

    @MockBean
    private CustomerExternalService customerExternalService;

    @MockBean
    private GroupExternalService groupExternalService;

    @MockBean
    private IdentityProviderExternalService identityProviderExternalService;

    @MockBean
    private OwnerExternalService ownerExternalService;

    @MockBean
    private ProfileExternalService profileExternalService;

    @MockBean
    private SubrogationExternalService subrogationExternalService;

    @MockBean
    private TenantExternalService tenantExternalService;

    @MockBean
    private UserExternalService userExternalService;

    @MockBean
    private LogbookExternalService logbookExternalService;

    @MockBean
    private ExternalApiAuthenticationProvider externalApiAuthenticationProvider;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private ApplicationExternalService applicationExternalService;

    @MockBean
    private ExternalParametersExternalService externalParametersExternalService;

}
