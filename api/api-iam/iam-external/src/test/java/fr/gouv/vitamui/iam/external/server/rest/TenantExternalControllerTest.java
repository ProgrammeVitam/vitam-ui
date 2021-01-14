package fr.gouv.vitamui.iam.external.server.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.external.server.service.TenantExternalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { TenantExternalController.class })
public class TenantExternalControllerTest extends ApiIamControllerTest<TenantDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantExternalControllerTest.class);

    @MockBean
    private TenantExternalService tenantExternalService;

    @Test
    public void testGetAllTenants() {
        LOGGER.debug("testGetAllEntity");
        super.testGetAllEntityWithCriteria();
    }

    @Test
    public void testUpdateTenant() {
        super.testUpdateEntity();
    }

    @Test
    public void testPatchTenant() {
        LOGGER.debug("testPatchTenant");
        super.testPatchEntity();
    }

    @Test
    public void testGetTenant() {
        super.testGetEntityById();
    }

    @Test
    public void testCheckExistByName() {
        Mockito.when(tenantExternalService.checkExists(any(String.class))).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria().addCriterion("name", "tenantName", CriterionOperator.EQUALS);
        super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()));
    }

    @Test
    public void testCheckExistByBadCriteriaScriptThenReturnBadRequest() {
        Mockito.when(tenantExternalService.checkExists(any(String.class))).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria().addCriterion("name", "tenantName<s></s>", CriterionOperator.EQUALS);
        super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()), status().isBadRequest());
    }

    @Test
    public void testCheckExistByBadCriteriaForbiddenFieldTypeThenReturnBadRequest() {
        Mockito.when(tenantExternalService.checkExists(any(String.class))).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria().addCriterion("name", "tenantName<![CDATA[", CriterionOperator.EQUALS);
        super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()), status().isBadRequest());
    }

    @Test
    public void testGetPaginatedTenant() {
        LOGGER.debug("testGetPaginatedTenant");
        super.testGetPaginatedEntities();
    }

    @Override
    protected TenantDto buildDto() {
        return IamDtoBuilder.buildTenantDto("id", "name", 100, "ownerId", "customerId");
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
    }

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_TENANTS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_TENANTS };
    }

    @Override
    protected Class<TenantDto> getDtoClass() {
        return TenantDto.class;
    }

}
