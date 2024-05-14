package fr.gouv.vitamui.cas.provider;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
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
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ProvidersService}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class ProvidersServiceTest {

    private static final String PROVIDER_ID = "company";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String CUSTOMER_ID = "customerId";

    private ProvidersService service;

    private IdentityProviderExternalRestClient restClient;

    private SAML2Client saml2Client;

    private IdentityProviderDto provider;

    private IdentityProviderHelper identityProviderHelper;

    @Before
    public void setUp() {
        val clients = new Clients();
        val builder = mock(Pac4jClientBuilder.class);
        restClient = mock(IdentityProviderExternalRestClient.class);
        val utils = new Utils(null, 0, null, null, "");
        service = new ProvidersService(clients, restClient, builder, utils);

        provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setInternal(true);
        provider.setPatterns(List.of(".*@company.com"));
        provider.setCustomerId(CUSTOMER_ID);

        saml2Client = new SAML2Client();
        saml2Client.setName("testSAML2Client");
        when(builder.buildClient(provider)).thenReturn(Optional.of(saml2Client));

        identityProviderHelper = new IdentityProviderHelper();
    }

    @Test
    public void testGetProviders() {
        when(
            restClient.getAll(
                any(ExternalHttpContext.class),
                eq(Optional.empty()),
                eq(Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA))
            )
        ).thenReturn(Arrays.asList(provider));

        service.loadData();

        val missingProvider = identityProviderHelper.findByUserIdentifierAndCustomerId(
            service.getProviders(),
            "user1@vitamui.com",
            CUSTOMER_ID
        );
        assertFalse(missingProvider.isPresent());

        val userProvider = identityProviderHelper.findByUserIdentifierAndCustomerId(
            service.getProviders(),
            "user1@company.com",
            CUSTOMER_ID
        );
        assertTrue(userProvider.isPresent());
        assertEquals(PROVIDER_ID, userProvider.get().getId());
        assertEquals(saml2Client, ((Pac4jClientIdentityProviderDto) userProvider.get()).getClient());
    }

    @Test
    public void testReloadDoesNotThrowException() {
        service.reloadData();
    }

    @Test
    public void testNoProviderResponse() {
        when(
            restClient.getAll(
                any(ExternalHttpContext.class),
                eq(Optional.empty()),
                eq(Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA))
            )
        ).thenReturn(null);
        try {
            service.loadData();
            fail("should fail");
        } catch (final NullPointerException e) {
            assertNull(e.getMessage());
        }
    }

    @Test
    public void testBadProviderResponse() {
        when(
            restClient.getAll(
                any(ExternalHttpContext.class),
                eq(Optional.empty()),
                eq(Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA))
            )
        ).thenThrow(new RuntimeException(ERROR_MESSAGE));

        try {
            service.loadData();
            fail("should fail");
        } catch (final RuntimeException e) {
            assertEquals(ERROR_MESSAGE, e.getMessage());
        }
    }
}
