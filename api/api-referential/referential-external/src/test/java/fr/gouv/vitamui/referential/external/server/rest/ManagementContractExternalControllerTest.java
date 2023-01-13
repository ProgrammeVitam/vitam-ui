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
 */

package fr.gouv.vitamui.referential.external.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.ManagementContractExternalService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers={ ManagementContractExternalController.class })
public  class ManagementContractExternalControllerTest extends ApiReferentialControllerTest<ManagementContractDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ManagementContractExternalControllerTest.class);

    @MockBean
    private ManagementContractExternalService managementContractExternalService;

     @Override
    protected String[] getServices() {
        return new String[] {
            ServicesData.SERVICE_MANAGEMENT_CONTRACT,
            ServicesData.ROLE_CREATE_MANAGEMENT_CONTRACT,
            ServicesData.ROLE_GET_MANAGEMENT_CONTRACT,
            ServicesData.ROLE_UPDATE_MANAGEMENT_CONTRACT,
        };
    }

    @Override
    protected Class<ManagementContractDto> getDtoClass() {
        return ManagementContractDto.class;
    }

    @Override
    protected ManagementContractDto buildDto() {
        return new ManagementContractDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.MANAGEMENT_CONTRACTS_URL;
    }

    @Test
    public void getAll() {
        super.testGetAllEntity();
    }

    @Test
    public void testManagementContract() {
        Assert.assertNotNull(managementContractExternalService);
    }



}
