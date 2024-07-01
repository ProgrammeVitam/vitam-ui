package fr.gouv.vitamui.pastis.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.pastis.service.ArchivalProfileUnitService;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = ArchivalProfileUnitController.class)
public class ArchivalProfileUnitControllerTest extends UIPastisRestControllerTest<ArchivalProfileUnitDto> {

    @Value("${ui-pastis.prefix}")
    protected String apiUrl;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchivalProfileUnitController.class);

    private static final String PREFIX = RestApi.ARCHIVAL_PROFILE;

    @MockBean
    private ArchivalProfileUnitService service;

    @Test
    public void testGetAll() {
        super.testGetAllEntity();
    }

    @Test
    public void testGetBy() {
        super.testGetEntityById();
    }

    @Test
    public void testGetAllPaginatedManagementContract() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.X_TENANT_ID_HEADER, "1");
        super.performGet("/", ImmutableMap.of("page", 1, "size", 20, "orderBy", "id"));
    }

    @Test
    public void testCreate() {
        super.testCreateEntity(status().isOk());
    }

    @Test
    public void testUpdate() {
        super.testUpdateEntity();
    }

    @Override
    protected Class<ArchivalProfileUnitDto> getDtoClass() {
        return ArchivalProfileUnitDto.class;
    }

    @Override
    protected ArchivalProfileUnitDto buildDto() {
        final ArchivalProfileUnitDto dto = new ArchivalProfileUnitDto();
        return dto;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.update(any(), any(ArchivalProfileUnitDto.class))).thenReturn(new ArchivalProfileUnitDto());
        Mockito.when(service.create(any(), any(ArchivalProfileUnitDto.class))).thenReturn(new ArchivalProfileUnitDto());
        Mockito.when(service.getOne(any(), any())).thenReturn(new ArchivalProfileUnitDto());
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }
}
