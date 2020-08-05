package fr.gouv.vitamui.cas.provider;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Saml2ClientBuilder;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link ProvidersService}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class ProvidersServiceTest {

    private static final String PROVIDER_ID = "company";
    private static final String ERROR_MESSAGE = "errorMessage";

    private ProvidersService service;

    private IdentityProviderExternalRestClient restClient;

    private SAML2Client saml2Client;

    private IdentityProviderDto provider;

    private IdentityProviderHelper identityProviderHelper;

    @Before
    public void setUp() {
        service = new ProvidersService();
        val clients = new Clients();
        service.setClients(clients);
        val builder = mock(Saml2ClientBuilder.class);
        service.setSaml2ClientBuilder(builder);
        restClient = mock(IdentityProviderExternalRestClient.class);
        service.setIdentityProviderExternalRestClient(restClient);
        val utils = new Utils(null, 0, null, null);
        service.setUtils(utils);

        provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setInternal(true);
        provider.setPatterns(Arrays.asList(".*@company.com"));

        saml2Client = new SAML2Client();
        saml2Client.setName("testSAML2Client");
        when(builder.buildSaml2Client(provider)).thenReturn(Optional.of(saml2Client));

        identityProviderHelper = new IdentityProviderHelper();
    }

    @Test
    public void testGetProviders() {

        when(restClient.getAll(any(ExternalHttpContext.class), eq(Optional.empty()), eq(Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)))).thenReturn(Arrays.asList(provider));

        service.loadData();

        val missingProvider = identityProviderHelper.findByUserIdentifier(service.getProviders(), "jerome@vitamui.com");
        assertFalse(missingProvider.isPresent());

        val userProvider = identityProviderHelper.findByUserIdentifier(service.getProviders(), "jerome@company.com");
        assertTrue(userProvider.isPresent());
        assertEquals(PROVIDER_ID, userProvider.get().getId());
        assertEquals(saml2Client, ((SamlIdentityProviderDto) userProvider.get()).getSaml2Client());
    }

    @Test
    public void testReloadDoesNotThrowException() {

        service.reloadData();
    }

    @Test
    public void testNoProviderResponse() {

        when(restClient.getAll(any(ExternalHttpContext.class), eq(Optional.empty()), eq(Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)))).thenReturn(null);
        try {
            service.loadData();
            fail("should fail");
        } catch (final NullPointerException e) {
            assertNull(e.getMessage());
        }
    }

    @Test
    public void testBadProviderResponse() {

        when(restClient.getAll(any(ExternalHttpContext.class), eq(Optional.empty()), eq(Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)))).thenThrow(new RuntimeException(ERROR_MESSAGE));

        try {
            service.loadData();
            fail("should fail");
        } catch (final RuntimeException e) {
            assertEquals(ERROR_MESSAGE, e.getMessage());
        }
    }
}
