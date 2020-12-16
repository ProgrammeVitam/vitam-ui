package fr.gouv.vitamui.iam.internal.server;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.cas.service.CasInternalService;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.security.IamApiAuthenticationProvider;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { ServerIdentityConfiguration.class, SwaggerConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=iam-internal-application" })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private CasInternalService casService;

    @MockBean
    private UserInternalService userInternalService;

    @MockBean
    private SubrogationInternalService subrogationInternalService;

    @MockBean
    private IamLogbookService iamLogbookService;

    @MockBean
    private GroupInternalService groupInternalService;

    @MockBean
    private ProfileInternalService profileInternalService;

    @MockBean
    private IdentityProviderInternalService identityProviderInternalService;

    @MockBean
    private IdentityProviderRepository identityProviderRepository;

    @MockBean
    private CustomerInternalService customerInternalService;

    @MockBean
    private OwnerInternalService ownerInternalService;

    @MockBean
    private TenantInternalService tenantInternalService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private IamApiAuthenticationProvider iamApiAuthenticationProvider;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private AdminExternalClient adminExternalClient;

    @MockBean(name = "accessExternalClient")
    private AccessExternalClient accessExternalClient;

    @MockBean
    private EventService eventService;

    @MockBean
    private LogbookService logbookService;

    @MockBean
    private InternalSecurityService internalSecurityService;

    @MockBean
    private MongoTransactionManager mongoTransactionManager;

    @MockBean
    private ApplicationInternalService applicationInternalService;
    
    @MockBean
    private ExternalParametersInternalService externalParametersInternalService;

}
