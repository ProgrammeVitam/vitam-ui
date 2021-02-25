package fr.gouv.vitamui.iam.internal.server.application.service;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.CommonConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.query.Query;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.internal.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.internal.server.application.dao.ApplicationRepository;
import fr.gouv.vitamui.iam.internal.server.application.domain.Application;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

public class ApplicationInternalServiceTest {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationInternalServiceTest.class);

    private ApplicationInternalService applicationService;

    private final ApplicationConverter applicationConverter = new ApplicationConverter();

    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);


    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    @Before
    public void setup() {
        applicationService = new ApplicationInternalService(sequenceRepository, applicationRepository, applicationConverter, internalSecurityService);

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
    }

    @Test
    public void testGetAll() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = Arrays.asList(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(true);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);

        final List<ApplicationDto> result = applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assert.assertNotNull("Applications should be returned.", result);
        Assert.assertEquals("Applications size should be returned.", apps.size(), result.size());
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
        Assert.assertNotNull("Applications should be returned.", result);
        Assert.assertEquals("Applications size should be returned.", 1, result.size());
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
        Assert.assertNotNull("Applications should be returned.", result);
        Assert.assertEquals("Applications size should be returned.", 2, result.size());
    }

    @Test
    public void testGetAllForNullUserThenThrowException() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = Arrays.asList(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        Mockito.when(internalSecurityService.getUser()).thenReturn(null);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);
        try {
            applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
            fail("Should Throw Exception");
        } catch (UnAuthorizedException e) {
        }
    }

    @Test
    public void testGetAllForUserWithoutPermission() {
        final Application app = IamServerUtilsTest.buildApplication();
        final List<Application> apps = Arrays.asList(app);
        when(applicationRepository.findAll(any(Query.class))).thenReturn(apps);

        wireInternalSecurityServerCalls(false);

        final QueryDto criteria = QueryDto.criteria("identifier", "cont", CriterionOperator.CONTAINSIGNORECASE);

        final List<ApplicationDto> result = applicationService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assert.assertNotNull("Applications should be returned.", result);
        Assert.assertEquals("Applications size should be returned.", 0, result.size());
    }

    private void wireInternalSecurityServerCalls(boolean withApplications) {
        final AuthUserDto user = IamServerUtilsTest.buildAuthUserDto();
        user.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);
        if (withApplications)
            user.setTenantsByApp(getTenantInformationByApp());
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

        return Arrays.asList(tenantForApp);
    }
}
