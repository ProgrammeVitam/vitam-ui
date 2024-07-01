package fr.gouv.vitamui.pastis.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileNotice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.rest.RestApi;
import fr.gouv.vitamui.pastis.service.PastisTransformationService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = PastisController.class)
public class PastisControllerTest extends UIPastisRestControllerTest {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(PastisController.class);

    @Value("${ui-pastis.prefix}")
    protected String apiUrl;

    private static final String PREFIX = RestApi.PASTIS;

    @MockBean
    private PastisTransformationService service;

    @Mock
    private PastisController controller;

    @Test
    public void testCreatePofile() {
        super.performGet("/profile", ImmutableMap.of("type", "PUA"), status().isOk());
    }

    @Test
    public void testGetArchiveUnitProfile() {
        ProfileNotice dto = new ProfileNotice();
        UriComponentsBuilder uriBuilder = getUriBuilder("/getarchiveunitprofile");
        super.performPost(uriBuilder, asJsonString(dto), status().isOk());
    }

    @Test
    public void testGetArchiveProfile() {
        ElementProperties dto = new ElementProperties();
        UriComponentsBuilder uriBuilder = getUriBuilder("/archiveprofile");
        super.performPost(uriBuilder, asJsonString(dto), status().isOk());
    }

    @Test
    public void testLoadProfileFromFile() throws IOException {
        File file = new File("src/test/resources/data/valid_pua.json");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
            file.getName(),
            file.getName(),
            "Application/json",
            IOUtils.toByteArray(input)
        );
        UriComponentsBuilder uriBuilder = getUriBuilder("/profile");

        Mockito.when(service.loadProfileFromFile(any(MultipartFile.class), any(ExternalHttpContext.class))).thenReturn(
            ResponseEntity.ok().body(null)
        );

        assertThatCode(() -> {
            controller.loadProfileFromFile(multipartFile);
        }).doesNotThrowAnyException();
    }

    @Test
    public void testLoadProfile() {
        Notice dto = new Notice();
        UriComponentsBuilder uriBuilder = getUriBuilder("/edit");
        super.performPost(uriBuilder, asJsonString(dto), status().isOk());
    }

    @Override
    protected Class getDtoClass() {
        return ProfileResponse.class;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        try {
            Mockito.when(service.createProfile(eq("PUA"), any())).thenReturn(ResponseEntity.ok(new ProfileResponse()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }
}
