package fr.gouv.vitamui.referential.external.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.ManagementContractExternalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers={ManagementContractExternalController.class})
public  class ManagementContractExternalControllerTest extends ApiReferentialControllerTest<ManagementContractDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ManagementContractExternalControllerTest.class);

    @MockBean
    private ManagementContractExternalService managementContractExternalService;

    @Test
    public void getAll() {
        super.testGetAllEntity();
    }

    @Override
    protected String[] getServices() {
        return new String[] {ServicesData.SERVICE_MANAGEMENT_CONTRACT};
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
}
