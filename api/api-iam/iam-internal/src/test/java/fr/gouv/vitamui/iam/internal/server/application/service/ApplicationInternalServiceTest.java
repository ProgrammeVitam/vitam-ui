package fr.gouv.vitamui.iam.internal.server.application.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.internal.server.application.dao.ApplicationRepository;
import fr.gouv.vitamui.iam.internal.server.application.domain.Application;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
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

public class ApplicationInternalServiceTest {

    private ApplicationInternalService applicationService;

    private final ApplicationConverter applicationConverter = new ApplicationConverter();

    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    private final SequenceGeneratorService sequenceGeneratorService = mock(SequenceGeneratorService.class);

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private final ExternalIdentifierConfiguration externalIdentifierConfiguration = mock(
        ExternalIdentifierConfiguration.class
    );

    @BeforeEach
    public void setup() {
        applicationService = new ApplicationInternalService(
            sequenceGeneratorService,
            applicationRepository,
            applicationConverter,
            internalSecurityService,
            externalIdentifierConfiguration
        );

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    @Test
    public void testGetAll() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = Arrays.asList(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(true);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);

        final List<ApplicationDto> result = applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(apps.size(), result.size(), "Applications size should be returned.");
    }

    @Test
    public void testGetAllFilteredForUser() {
        final Application app = IamServerUtilsTest.buildApplication();
        final Application app2 = IamServerUtilsTest.buildApplication("id2", "url2");
        final List<Application> apps = Arrays.asList(app, app2);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(true);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);

        final List<ApplicationDto> result = applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(1, result.size(), "Applications size should be returned.");
    }

    @Test
    public void testGetAllWithoutFilter() {
        final Application app = IamServerUtilsTest.buildApplication();
        final Application app2 = IamServerUtilsTest.buildApplication("id2", "url2");
        final List<Application> apps = Arrays.asList(app, app2);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(true);

        final QueryDto criteria = QueryDto.criteria("filterApp", false, CriterionOperator.EQUALS);

        final List<ApplicationDto> result = applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(2, result.size(), "Applications size should be returned.");
    }

    @Test
    public void testGetAllForNullUserThenThrowException() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = List.of(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        Mockito.when(internalSecurityService.getUser()).thenReturn(null);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);
        try {
            applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
            Assertions.fail("Should Throw Exception");
        } catch (UnAuthorizedException ignored) {}
    }

    @Test
    public void testGetAllForUserWithoutPermission() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = Arrays.asList(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(false);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);

        final List<ApplicationDto> result = applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assertions.assertNotNull(result, "Applications should be returned.");
        Assertions.assertEquals(0, result.size(), "Applications size should be returned.");
    }

    private void wireInternalSecurityServerCalls(boolean withApplications) {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);
        if (withApplications) user.setTenantsByApp(getTenantInformationByApp());
        else {
            user.setTenantsByApp(new ArrayList<>());
        }

        Mockito.when(internalSecurityService.userIsRootLevel()).thenCallRealMethod();
        Mockito.when(internalSecurityService.getUser()).thenReturn(user);
        Mockito.when(internalSecurityService.getLevel()).thenReturn(ApiIamInternalConstants.ADMIN_LEVEL);
    }

    private List<TenantInformationDto> getTenantInformationByApp() {
        TenantInformationDto tenantForApp = new TenantInformationDto();
        tenantForApp.setName(CommonConstants.APPLICATION_ID);
        tenantForApp.setTenants(new HashSet<>());

        return List.of(tenantForApp);
    }
}
