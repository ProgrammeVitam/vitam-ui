package fr.gouv.vitamui.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import fr.gouv.vitamui.identity.domain.dto.ProviderPatchType;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class ProviderServiceTest extends UIIdentityServiceTest<IdentityProviderDto> {

    private ProviderService service;

    @SuppressWarnings("unused")
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProviderServiceTest.class);

    @Mock
    private IdentityProviderExternalRestClient client;

    @Before
    public void setup() {
        service = new ProviderService(factory);
        Mockito.when(factory.getIdentityProviderExternalRestClient()).thenReturn(client);
    }

    @Test
    public void testCreate() {
        super.createEntite();
    }

    @Test
    public void testUpdate() {
        super.updateEntite();
    }

    @Test
    public void testPatchJson() {
        Mockito.when(getClient().patch(any(), any())).thenReturn(buildDto(null));
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", ID);
        service.patch(null, partialDto, null, null, ID, ProviderPatchType.JSON);
    }

    @Test
    public void testPatchKeyStore() throws UnsupportedEncodingException {
        Mockito.when(getClient().patch(any(), any())).thenReturn(buildDto(null));
        final Map<String, Object> partialDto = new HashMap<>();
        final MultipartFile keystore = new MockMultipartFile("keystore", "14260".getBytes("UTF-8"));
        partialDto.put("id", ID);
        partialDto.put("keystorePassword", "keyspwd");
        service.patch(null, partialDto, keystore, null, ID, ProviderPatchType.KEYSTORE);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testPatchKeyStoreInvalid() throws UnsupportedEncodingException {
        Mockito.when(getClient().patch(any(), any())).thenReturn(buildDto(null));
        final Map<String, Object> partialDto = new HashMap<>();
        final MultipartFile keystore = new MockMultipartFile("keystore", "14260".getBytes("UTF-8"));
        partialDto.put("id", ID);
        service.patch(null, partialDto, keystore, null, ID, ProviderPatchType.KEYSTORE);
    }

    @Test
    public void testPatchIdpMetadata() throws UnsupportedEncodingException {
        Mockito.when(getClient().patch(any(), any())).thenReturn(buildDto(null));
        final Map<String, Object> partialDto = new HashMap<>();
        final MultipartFile idpMetadata = new MockMultipartFile("idpMetadata", "<xml>".getBytes("UTF-8"));
        partialDto.put("id", ID);
        service.patch(null, partialDto, null, idpMetadata, ID, ProviderPatchType.IDPMETADATA);
    }

    @Test
    public void testGetEntityById() {
        final IdentityProviderDto mock = new IdentityProviderDto();
        mock.setPatterns(Arrays.asList(".*@vitamui.com", ".*@google.com"));
        Mockito.when(getClient().getOne(any(), any(), any(), any())).thenReturn(mock);
        final IdentityProviderDto dto = service.getOne(null, "12", Optional.empty());
        assertThat(dto).isNotNull();
        assertThat(dto.getPatterns()).isEqualTo(Arrays.asList("vitamui.com", "google.com"));
    }

    @Test
    public void testConvertDtoFromApi() {
        final IdentityProviderDto ref = buildDto("1");
        ref.setPatterns(Arrays.asList(".*@vitamui.com"));
        final IdentityProviderDto res = service.convertDtoFromApi(ref);
        assertThat(res.getId()).isEqualTo("1");
        assertThat(res.getPatterns()).isEqualTo(Arrays.asList("vitamui.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithIdEmpty() {
        super.updateWithIdEmpty();
    }

    @Override
    public IdentityProviderExternalRestClient getClient() {
        return client;
    }

    @Override
    protected IdentityProviderDto buildDto(final String id) {
        final IdentityProviderDto dto = new IdentityProviderDto();
        dto.setId(id);
        dto.setEnabled(true);
        dto.setInternal(true);
        dto.setPatterns(Arrays.asList(".*@vitamui.com"));
        dto.setKeystoreBase64("test");
        dto.setKeystorePassword("test");
        dto.setPrivateKeyPassword("test");
        dto.setIdpMetadata("test");
        dto.setName("name");
        dto.setCustomerId("customerId");
        dto.setName("name");
        return dto;
    }

    @Override
    protected AbstractCrudService<IdentityProviderDto> getService() {
        return service;
    }
}
