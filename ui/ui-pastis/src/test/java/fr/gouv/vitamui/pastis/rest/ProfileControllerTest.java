package fr.gouv.vitamui.pastis.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitam.common.model.administration.ProfileFormat;
import fr.gouv.vitam.common.model.administration.ProfileStatus;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.pastis.service.ProfileService;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
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
@WebMvcTest(controllers = ProfileController.class)
public class ProfileControllerTest extends UIPastisRestControllerTest<ProfileDto>{

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileController.class);

    @Value("${ui-pastis.prefix}")
    protected String apiUrl;

    private static final String PREFIX = RestApi.PROFILE;

    @Test
    public void testGetAllPaginatedManagementContract() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.X_TENANT_ID_HEADER, "1");
        super.performGet("/", ImmutableMap.of("page", 1, "size", 20, "orderBy", "id" ));
    }


    @Test
    public void testGetAll() {
        super.testGetAllEntity();
    }

    @Test
    public void testGetBy() {
        super.testGetEntityById();
    }

    @Test
    public void testCreate() {
        super.testCreateEntity(status().isOk());
    }

    @Test
    public void testUpdate() {
        super.testUpdateEntity();
    }

    @MockBean
    private ProfileService service;

    @Override
    protected Class<ProfileDto> getDtoClass() {
        return ProfileDto.class;
    }

    @Override
    protected ProfileDto buildDto() {
        final ProfileDto dto = new ProfileDto();
        dto.setTenant(0);
        dto.setId("id");
        dto.setName("ProfileName");
        dto.setFormat(ProfileFormat.RNG);
        dto.setStatus(ProfileStatus.ACTIVE);
        return dto;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.update(any(), any(ProfileDto.class))).thenReturn(new ProfileDto());
        Mockito.when(service.create(any(), any(ProfileDto.class))).thenReturn(new ProfileDto());
        Mockito.when(service.getOne(any(), any())).thenReturn(new ProfileDto());
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }
}
