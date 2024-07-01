package fr.gouv.vitamui.pastis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.model.administration.ProfileFormat;
import fr.gouv.vitam.common.model.administration.ProfileStatus;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.external.client.ProfileExternalRestClient;
import fr.gouv.vitamui.referential.external.client.ProfileExternalWebClient;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static fr.gouv.vitamui.pastis.service.UIPastisServiceTest.commonService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProfileServiceTest extends AbstractCrudService<ProfileDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileService.class);

    @Mock
    private ProfileExternalWebClient webClient;

    @Mock
    private ProfileExternalRestClient restClient;

    private ProfileService service;

    @Before
    public void setup() {
        service = new ProfileService(restClient, webClient, commonService);
    }

    @Override
    public BaseCrudRestClient getClient() {
        return restClient;
    }

    protected ProfileDto buidDto(String id) {
        final ProfileDto dto = new ProfileDto();
        dto.setTenant(0);
        dto.setId(id);
        dto.setName("ProfileName");
        dto.setFormat(ProfileFormat.RNG);
        dto.setStatus(ProfileStatus.ACTIVE);
        return dto;
    }

    protected AbstractCrudService<ProfileDto> getService() {
        return service;
    }

    @Test
    public void testGetAll() {
        super.getAll(null, Optional.empty());
    }

    @Test
    public void testCheck() {
        ProfileDto dto = (ProfileDto) buidDto("id");

        Mockito.when(restClient.check(isNull(), any(ProfileDto.class))).thenReturn(true);
        final boolean check = service.check(null, dto);
        assertThat(check).isEqualTo(true);
    }

    @Test
    public void testUpdate() {
        super.update(null, buidDto("id"));
    }

    @Test
    public void testDelete() {
        super.delete(null, "id");
    }

    @Test
    public void testExport() throws IOException {
        File file = new File("src/test/resources/data/valid_pua.json");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
            file.getName(),
            file.getName(),
            "Application/json",
            IOUtils.toByteArray(input)
        );

        String response = "{\"httpCode\":\"201\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response);

        Mockito.when(webClient.importProfiles(any(ExternalHttpContext.class), any(MultipartFile.class))).thenReturn(
            ResponseEntity.ok().body(jsonResponse)
        );

        assertThatCode(() -> {
            service.importProfiles(null, multipartFile);
        }).doesNotThrowAnyException();
    }
}
