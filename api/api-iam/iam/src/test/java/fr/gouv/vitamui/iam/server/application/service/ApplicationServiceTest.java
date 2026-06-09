package fr.gouv.vitamui.iam.server.application.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.IdentifierNameDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.server.application.dao.ApplicationRepository;
import fr.gouv.vitamui.iam.server.application.domain.Application;
import fr.gouv.vitamui.iam.server.common.ApiIamConstants;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationServiceTest {

    private ApplicationService applicationService;

    private final ApplicationConverter applicationConverter = new ApplicationConverter();

    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    private final SequenceGeneratorService sequenceGeneratorService = mock(SequenceGeneratorService.class);

    private final SecurityService securityService = mock(SecurityService.class);

    private final ExternalIdentifierConfiguration externalIdentifierConfiguration = mock(
        ExternalIdentifierConfiguration.class
    );

    @BeforeEach
    public void setup() {
        applicationService = new ApplicationService(
            sequenceGeneratorService,
            applicationRepository,
            applicationConverter,
            securityService,
            externalIdentifierConfiguration
        );

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    @Test
    void testGetAllFilteredByUser() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = Arrays.asList(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(true);

        final List<ApplicationDto> result = applicationService.getAllFilteredByUser(Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(apps.size(), result.size(), "Applications size should be returned.");
    }

    @Test
    void testGetAllFilteredByUserShouldFilteredForUser() {
        final Application app = IamServerUtilsTest.buildApplication();
        final Application app2 = IamServerUtilsTest.buildApplication("id2", "url2");
        final List<Application> apps = Arrays.asList(app, app2);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(true);

        final List<ApplicationDto> result = applicationService.getAllFilteredByUser(Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(1, result.size(), "Applications size should be returned.");
    }

    @Test
    void testGetAllFilteredByUserForNullUserThenThrowException() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = List.of(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        Mockito.when(securityService.getUser()).thenReturn(null);

        try {
            applicationService.getAllFilteredByUser(Optional.empty());
            Assertions.fail("Should Throw Exception");
        } catch (UnAuthorizedException ignored) {}
    }

    @Test
    void testGetAllFilteredByUserForUserWithoutPermission() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = List.of(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(false);

        final List<ApplicationDto> result = applicationService.getAllFilteredByUser(Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(0, result.size(), "Applications size should be returned.");
    }

    @Test
    public void testListNamesShouldReturnAllApps() {
        final Application app = IamServerUtilsTest.buildApplication();
        final Application app2 = IamServerUtilsTest.buildApplication("id2", "url2");
        final List<Application> apps = Arrays.asList(app, app2);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(false);

        final List<IdentifierNameDto> result = applicationService.listNames();
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(2, result.size(), "Applications size should be returned.");
    }

    private void wireInternalSecurityServerCalls(boolean withApplications) {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(ApiIamConstants.ADMIN_LEVEL);
        if (withApplications) user.setTenantsByApp(getTenantInformationByApp());
        else {
            user.setTenantsByApp(new ArrayList<>());
        }

        Mockito.when(securityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(securityService.getUser()).thenReturn(user);
        Mockito.when(securityService.getLevel()).thenReturn(ApiIamConstants.ADMIN_LEVEL);
    }

    private List<TenantInformationDto> getTenantInformationByApp() {
        TenantInformationDto tenantForApp = new TenantInformationDto();
        tenantForApp.setName(CommonConstants.APPLICATION_ID);
        tenantForApp.setTenants(new HashSet<>());

        return List.of(tenantForApp);
    }
}
