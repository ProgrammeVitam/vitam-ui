package fr.gouv.vitamui.identity.rest;

import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.identity.service.ProviderService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { ProviderController.class })
public class ProviderControllerTest extends UiIdentityRestControllerTest<IdentityProviderDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    @MockBean
    private ProviderService service;

    @MockBean
    private BuildProperties buildProperties;


    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProviderControllerTest.class);

    private static final String PREFIX = "/providers";

    @Test
    public void testCreateProvider() {
        getLog().debug("test create entity class ={}", getDtoClass().getName());
        final MockMultipartFile keystore = new MockMultipartFile("keystore", "keystore", "application/x-java-keystore", "foo".getBytes());
        final MockMultipartFile idpMetadata = new MockMultipartFile("idpMetadata", "idpMetadata", MediaType.APPLICATION_XML_VALUE, "<xml></xml>".getBytes());
        final MockMultipartFile provider = new MockMultipartFile("provider", "", "application/json", "{\"id\": \"1\"}".getBytes());
        performPostMultipart(StringUtils.EMPTY, Arrays.asList(keystore, idpMetadata, provider));
    }

    @Test
    public void testPatchProviderKeystore() {
        getLog().debug("test patch keystore entity class ={}", getDtoClass().getName());
        final MockMultipartFile keystore = new MockMultipartFile("keystore", "keystore", "application/x-java-keystore", "foo".getBytes());
        final MockMultipartFile provider = new MockMultipartFile("provider", "", "application/json", "{\"id\": \"1\"}".getBytes());
        performPatchMultipart("/1/keystore", Arrays.asList(keystore, provider));
    }

    @Test
    public void testPatchProviderIdpMetadata() {
        getLog().debug("test patch idpMetadata entity class ={}", getDtoClass().getName());
        final MockMultipartFile idpMetadata = new MockMultipartFile("idpMetadata", "idpMetadata", MediaType.APPLICATION_XML_VALUE, "<xml></xml>".getBytes());
        final MockMultipartFile provider = new MockMultipartFile("provider", "", "application/json", "{\"id\": \"1\"}".getBytes());
        performPatchMultipart("/1/idpMetadata", Arrays.asList(idpMetadata, provider));
    }

    @Test
    public void testPatchProvider() {
        super.testPatchEntity();
    }

    @Test
    public void testGetPatchProviderById() {
        super.testGetEntityById();
    }

    @Test
    public void testFindByCustomerId() {
        super.performGet(StringUtils.EMPTY, ImmutableMap.of("customerId", "1"));
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<IdentityProviderDto> getDtoClass() {
        return IdentityProviderDto.class;
    }

    @Override
    protected IdentityProviderDto buildDto() {
        final IdentityProviderDto dto = new IdentityProviderDto();
        dto.setCustomerId("customerId");
        dto.setEnabled(true);
        dto.setInternal(true);
        dto.setName("name");
        dto.setPatterns(Arrays.asList("@vitamui.com", ".*@vitamui.com"));
        return dto;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        try {
            Mockito.when(service.create(any(), any(), any(), any(String.class))).thenReturn(new IdentityProviderDto());
        }
        catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        Mockito.when(service.update(any(), any(IdentityProviderDto.class))).thenReturn(new IdentityProviderDto());
        Mockito.when(service.patch(any(), any(), any())).thenReturn(new IdentityProviderDto());
    }
}
