package fr.gouv.vitamui.cas.delegation;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.openapiclient.IdentityProvidersApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ProvidersService}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ProvidersServiceTest.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class ProvidersServiceTest {

    private static final String PROVIDER_ID = "company";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String CUSTOMER_ID = "customerId";

    private ProvidersService service;

    private IdentityProvidersApi identityProvidersApi;

    private SAML2Client saml2Client;

    private IdentityProviderDto provider;

    private IdentityProviderHelper identityProviderHelper;

    private Pac4jClientBuilder builder;

    @Before
    public void setUp() {
        final var clients = new Clients();
        builder = mock(Pac4jClientBuilder.class);
        identityProvidersApi = mock(IdentityProvidersApi.class);
        service = new ProvidersService(clients, identityProvidersApi, builder);

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
            identityProvidersApi.getAll(
                eq(null),
                eq(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)
            )
        ).thenReturn(Collections.singletonList(provider));

        service.loadData();

        final var missingProvider = identityProviderHelper.findByUserIdentifierAndCustomerId(
            service.getProviders(),
            "user1@vitamui.com",
            CUSTOMER_ID
        );
        assertFalse(missingProvider.isPresent());

        final var userProvider = identityProviderHelper.findByUserIdentifierAndCustomerId(
            service.getProviders(),
            "user1@company.com",
            CUSTOMER_ID
        );
        assertTrue(userProvider.isPresent());
        assertEquals(PROVIDER_ID, userProvider.get().getId());
        assertEquals(saml2Client, ((Pac4jClientIdentityProviderDto) userProvider.get()).getClient());
    }

    @Test
    public void testProvidersAreOrderedByTheTextualSortOfTheirIdentifier() {
        final var internalProvider = buildProvider("internalProvider", "2", true);
        final var externalProvider = buildProvider("externalProvider", "10", false);
        when(builder.buildClient(any())).thenReturn(Optional.empty());
        when(identityProvidersApi.getAll(eq(null), any()))
            .thenReturn(new ArrayList<>(List.of(internalProvider, externalProvider)));

        service.loadData();

        assertEquals(
            List.of("externalProvider", "internalProvider"),
            service.getProviders().stream().map(IdentityProviderDto::getId).toList()
        );

        final var resolved = identityProviderHelper.findByUserIdentifierAndCustomerId(
            service.getProviders(),
            "user1@company.com",
            CUSTOMER_ID
        );
        assertTrue(resolved.isPresent());
        assertEquals("externalProvider", resolved.get().getId());
        assertFalse(resolved.get().getInternal());
    }

    private IdentityProviderDto buildProvider(final String id, final String identifier, final boolean internal) {
        final var newProvider = new IdentityProviderDto();
        newProvider.setId(id);
        newProvider.setIdentifier(identifier);
        newProvider.setInternal(internal);
        newProvider.setPatterns(List.of(".*@company.com"));
        newProvider.setCustomerId(CUSTOMER_ID);
        return newProvider;
    }

    @Test
    public void testReloadDoesNotThrowException() {
        service.reloadData();
    }

    @Test
    public void testStartupSucceedsWhenTheIamIsUnreachable() {
        when(identityProvidersApi.getAll(eq(null), any())).thenThrow(new RuntimeException(ERROR_MESSAGE));

        service.afterPropertiesSet();

        assertTrue(service.getProviders().isEmpty());
    }

    @Test
    public void testProvidersBecomeAvailableWithoutRestartingOnceTheIamAnswers() {
        when(identityProvidersApi.getAll(eq(null), any())).thenThrow(new RuntimeException(ERROR_MESSAGE));
        service.afterPropertiesSet();
        assertTrue(service.getProviders().isEmpty());

        reset(identityProvidersApi);
        when(builder.buildClient(any())).thenReturn(Optional.empty());
        when(identityProvidersApi.getAll(eq(null), any())).thenReturn(
            new ArrayList<>(List.of(buildProvider(PROVIDER_ID, "1", true)))
        );

        service.reloadData();

        assertEquals(1, service.getProviders().size());
        assertEquals(PROVIDER_ID, service.getProviders().getFirst().getId());
    }

    @Test
    public void testNoProviderResponse() {
        when(
            identityProvidersApi.getAll(
                eq(null),
                eq(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)
            )
        ).thenReturn(null);
        try {
            service.loadData();
            fail("should fail");
        } catch (final NullPointerException e) {
            assertEquals(
                "Cannot invoke \"java.util.List.sort(java.util.Comparator)\" because \"temporaryProviders\" is null",
                e.getMessage()
            );
        }
    }

    @Test
    public void testBadProviderResponse() {
        when(
            identityProvidersApi.getAll(
                eq(null),
                eq(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)
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
