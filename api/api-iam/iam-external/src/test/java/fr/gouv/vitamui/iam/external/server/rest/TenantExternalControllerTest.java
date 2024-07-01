/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

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
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
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
        final QueryDto criteria = QueryDto.criteria()
            .addCriterion("name", "tenantName<s></s>", CriterionOperator.EQUALS);
        super.performHead(
            CommonConstants.PATH_CHECK,
            ImmutableMap.of("criteria", criteria.toJson()),
            status().isPreconditionFailed()
        );
    }

    @Test
    public void testCheckExistByBadCriteriaForbiddenFieldTypeThenReturnBadRequest() {
        Mockito.when(tenantExternalService.checkExists(any(String.class))).thenReturn(true);
        final QueryDto criteria = QueryDto.criteria()
            .addCriterion("name", "tenantName<![CDATA[", CriterionOperator.EQUALS);
        super.performHead(
            CommonConstants.PATH_CHECK,
            ImmutableMap.of("criteria", criteria.toJson()),
            status().isBadRequest()
        );
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
    protected void preparedServices() {}

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
