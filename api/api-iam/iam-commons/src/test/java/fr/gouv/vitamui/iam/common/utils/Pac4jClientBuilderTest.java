package fr.gouv.vitamui.iam.common.utils;

import com.nimbusds.jose.JWSAlgorithm;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import org.junit.Ignore;
import org.junit.Test;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link Pac4jClientBuilder}.
 */
public class Pac4jClientBuilderTest {

    private static final String LOGIN_URL = "casLoginUrl";

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String DISCOVERY_URL = "https://accounts.google.com/.well-known/openid-configuration";
    private static final String SCOPE = "openid email";
    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;
    private static final Map CUSTOM_PARAMS = Map.of("prompt", "login");

    @Ignore
    @Test
    public void testOidcProviderCreationSuccessful() {
        final IdentityProviderDto provider = new IdentityProviderDto();
        provider.setClientId(CLIENT_ID);
        provider.setClientSecret(CLIENT_SECRET);
        provider.setDiscoveryUrl(DISCOVERY_URL);
        provider.setScope(SCOPE);
        provider.setPreferredJwsAlgorithm(ALGORITHM.toString());
        provider.setCustomParams(CUSTOM_PARAMS);
        provider.setUseState(false);
        provider.setUseNonce(false);
        provider.setUsePkce(true);

        final Pac4jClientBuilder builder = new Pac4jClientBuilder();
        builder.setCasLoginUrl(LOGIN_URL);

        final Optional<IndirectClient> optClient = builder.buildClient(provider);

        assertTrue(optClient.isPresent());
        final IndirectClient client = optClient.get();
        assertTrue(client instanceof OidcClient);
        final OidcConfiguration config = ((OidcClient) client).getConfiguration();
        assertEquals(CLIENT_ID, config.getClientId());
        assertEquals(CLIENT_SECRET, config.getSecret());
        assertEquals(DISCOVERY_URL, config.getDiscoveryURI());
        assertEquals(SCOPE, config.getScope());
        assertEquals(ALGORITHM, config.getPreferredJwsAlgorithm());
        assertEquals(CUSTOM_PARAMS, config.getCustomParams());
        assertFalse(config.isWithState());
        assertFalse(config.isUseNonce());
        assertFalse(config.isDisablePkce());
    }

    @Test
    public void testOidcProviderCreationFailure() {
        final IdentityProviderDto provider = new IdentityProviderDto();
        provider.setClientId(CLIENT_ID);
        provider.setClientSecret(CLIENT_SECRET);
        provider.setDiscoveryUrl("http://url");

        final Pac4jClientBuilder builder = new Pac4jClientBuilder();
        builder.setCasLoginUrl(LOGIN_URL);

        final Optional<IndirectClient> optClient = builder.buildClient(provider);

        assertTrue(optClient.isEmpty());
    }
}
