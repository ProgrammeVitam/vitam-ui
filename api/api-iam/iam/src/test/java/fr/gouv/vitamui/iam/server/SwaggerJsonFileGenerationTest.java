package fr.gouv.vitamui.iam.server;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.application.service.ApplicationService;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.externalparamprofile.service.ExternalParamProfileService;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.subrogation.service.SubrogationService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { SwaggerConfiguration.class })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private CasService casService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserInfoService userInfoService;

    @MockBean
    private SubrogationService subrogationService;

    @MockBean
    private IamLogbookService iamLogbookService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private IdentityProviderService identityProviderService;

    @MockBean
    private IdentityProviderRepository identityProviderRepository;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private OwnerService ownerService;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ApiAuthenticationProvider apiAuthenticationProvider;

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
    private SecurityService securityService;

    @MockBean
    private MongoTransactionManager mongoTransactionManager;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private ExternalParametersService externalParametersService;

    @MockBean
    private AccessContractCommonService accessContractCommonService;

    @MockBean
    private ExternalParamProfileService externalParamProfileService;
}
