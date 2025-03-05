package fr.gouv.vitamui.iam.external.server.utils;

import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.external.server.application.service.ApplicationService;
import fr.gouv.vitamui.iam.external.server.cas.service.CasService;
import fr.gouv.vitamui.iam.external.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.external.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.external.server.externalparamprofile.service.ExternalParamProfileService;
import fr.gouv.vitamui.iam.external.server.group.service.GroupService;
import fr.gouv.vitamui.iam.external.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.external.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.external.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.external.server.subrogation.service.SubrogationService;
import fr.gouv.vitamui.iam.external.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.external.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.external.server.user.service.UserService;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { SwaggerConfiguration.class })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private CasService casService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private IdentityProviderService identityProviderService;

    @MockBean
    private OwnerService ownerService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private SubrogationService subrogationService;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserInfoService userInfoService;

    @MockBean
    private LogbookService logbookService;

    @MockBean
    private ExternalApiAuthenticationProvider externalApiAuthenticationProvider;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private ExternalParametersService externalParametersService;

    @MockBean
    private ExternalParamProfileService externalParamProfileService;
}
