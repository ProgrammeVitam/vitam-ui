package fr.gouv.vitamui.identity.rest;

import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.identity.service.TenantService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { TenantController.class })
public class TenantControllerTest extends UiIdentityRestControllerTest<TenantDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    @MockBean
    private TenantService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantControllerTest.class);

    private static final String PREFIX = "/tenants";

    @Test
    public void testCreateTenant() {
        super.testCreateEntity();
    }

    @Test
    public void testUpdateTenant() {
        super.testUpdateEntity();
    }

    @Test
    public void testGetAllTenants() {
        super.testGetAllEntityWithCriteria();
    }

    @Test
    public void testPatchTenant() {
        super.testPatchEntity();
    }

    @Test
    public void testCheckExistByName() {
        Mockito.when(service.checkExist(any(), any())).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria().addCriterion("name", "tenantName", CriterionOperator.EQUALS);
        super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()));
    }

    @Test
    public void testGetTenantById() {
        super.testGetEntityById();
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<TenantDto> getDtoClass() {
        return TenantDto.class;
    }

    @Override
    protected TenantDto buildDto() {
        final TenantDto tenant = new TenantDto();
        return tenant;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.create(any(), any(TenantDto.class))).thenReturn(new TenantDto());
        Mockito.when(service.update(any(), any(TenantDto.class))).thenReturn(new TenantDto());
    }
}
