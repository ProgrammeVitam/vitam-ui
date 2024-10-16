package fr.gouv.vitamui.pastis.server.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileType;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileVersion;
import fr.gouv.vitamui.pastis.common.exception.TechnicalException;
import fr.gouv.vitamui.pastis.common.rest.RestApi;
import fr.gouv.vitamui.pastis.server.security.WebSecurityConfig;
import fr.gouv.vitamui.pastis.server.service.PastisService;
import fr.gouv.vitamui.referential.internal.client.ProfileInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PastisController.class)
@Import(value = { WebSecurityConfig.class, RestExceptionHandler.class })
public class PastisControllerTest extends ControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PastisControllerTest.class);

    @MockBean
    private PastisService service;

    @MockBean
    private ExternalSecurityService externalSecurityService;

    @MockBean
    private ProfileInternalRestClient profileInternalRestClient;

    private PastisController controller;

    @Before
    public void setUp() throws Exception {
        controller = new PastisController(externalSecurityService, service, profileInternalRestClient);
    }

    @Test
    public void testGetArchiveProfile_should_return_ok() throws IOException, TechnicalException {
        UriComponentsBuilder uriBuilder = getUriBuilder("/archiveprofile");

        Mockito.when(service.getArchiveProfile(any(ElementProperties.class), any(ProfileVersion.class))).thenReturn("");

        InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream("rng/ProfileJson.json");
        ObjectMapper objectMapper = new ObjectMapper();
        ElementProperties dto = objectMapper.readValue(jsonInputStream, ElementProperties.class);

        super.performPost(uriBuilder, asJsonString(dto), status().isOk());
    }

    @Test
    public void testCreate() throws TechnicalException, NoSuchAlgorithmException {
        Mockito.when(
            service.createProfile(any(ProfileType.class), any(ProfileVersion.class), any(Boolean.class))
        ).thenReturn(new ProfileResponse());
        super.performGet("/profile", ImmutableMap.of("type", "PUA"), status().isOk());
    }

    @Override
    protected String getRessourcePrefix() {
        return RestApi.PASTIS;
    }

    @Override
    protected String[] getServices() {
        return new String[] {
            ServicesData.ROLE_GET_ARCHIVE_PROFILES,
            ServicesData.ROLE_GET_ARCHIVE_PROFILES_UNIT,
            ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES,
            ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES_UNIT,
            ServicesData.ROLE_CREATE_ARCHIVE_PROFILES,
            ServicesData.ROLE_CREATE_ARCHIVE_PROFILES_UNIT,
            ServicesData.ROLE_GET_PROFILES,
            ServicesData.ROLE_UPDATE_PROFILES,
            ServicesData.ROLE_CREATE_PROFILES,
            ServicesData.ROLE_GET_PASTIS,
            ServicesData.ROLE_DELETE_PASTIS,
        };
    }
}
