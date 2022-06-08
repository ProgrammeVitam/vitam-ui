package fr.gouv.vitamui.referential.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.service.ManagementContractService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { ManagementContractController.class})
class ManagementContractControllerTest extends fr.gouv.vitamui.referential.rest.UiReferentialRestControllerTest<ManagementContractDto> {

    @Value("${ui-referential.prefix}")
    protected  String apiUrl;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ManagementContractControllerTest.class);

    private static final String PREFIX = "/management-contract";

    @MockBean
    private ManagementContractService service;

    @Test
    void getAll() {
    }

    @Test
    public void testGetAllPaginatedManagementContract() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.X_TENANT_ID_HEADER, "1");
        super.performGet("/", ImmutableMap.of("page", 1, "size", 20, "orderBy", "id" ));
    }

    @Test
    void getById() {
        super.testGetEntityById();
    }

    @Test
    void check() {
    }

    @Test
    void create() {
        super.testCreateEntity();
    }

    @Test
    void patch() {
        super.testPatchEntity();
    }

    @Test
    void findHistoryById() {
        Mockito.when(service.findHistoryById(any(ExternalHttpContext.class), any(String.class))).thenReturn(new LogbookOperationsResponseDto());
        super.performGet("/1/history");
    }

    @Override
    protected Class<ManagementContractDto> getDtoClass() {
        return ManagementContractDto.class;
    }

    @Override
    protected ManagementContractDto buildDto() {
        final ManagementContractDto dto = new ManagementContractDto();
        return dto;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.update(any(), any(ManagementContractDto.class))).thenReturn(new ManagementContractDto());
        Mockito.when(service.create(any(), any(ManagementContractDto.class))).thenReturn(new ManagementContractDto());
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }
}
