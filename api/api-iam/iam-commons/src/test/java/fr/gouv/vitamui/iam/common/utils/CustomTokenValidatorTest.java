package fr.gouv.vitamui.iam.common.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.oidc.config.OidcConfiguration;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test {@link CustomTokenValidator}.
 */
public class CustomTokenValidatorTest {

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "123456789012345678901234567890ab";
    private static final String SUBJECT = "jleleu";
    private static final String ISSUER = "http://oidcprovider";

    private OidcConfiguration configuration;
    private JwtGenerator generator;
    private Map<String, Object> claims;
    private Nonce nonce;
    private CustomTokenValidator validator;

    @Before
    public void setUp() {
        configuration = mock(OidcConfiguration.class);
        final OIDCProviderMetadata metadata = mock(OIDCProviderMetadata.class);
        when(metadata.getIssuer()).thenReturn(new Issuer(ISSUER));
        when(configuration.findProviderMetadata()).thenReturn(metadata);
        when(configuration.getClientId()).thenReturn(CLIENT_ID);
        when(configuration.getSecret()).thenReturn(CLIENT_SECRET);
        when(metadata.getIDTokenJWSAlgs()).thenReturn(Arrays.asList(JWSAlgorithm.HS256));

        generator = new JwtGenerator(new SecretSignatureConfiguration(CLIENT_SECRET, JWSAlgorithm.HS256));
        claims = new HashMap<>();
        claims.put("iss", ISSUER);
        claims.put("sub", SUBJECT);
        claims.put("aud", CLIENT_ID);
        final long now = new Date().getTime() / 1000;
        claims.put("exp", now + 1000);
        claims.put("iat", now);
        nonce = new Nonce();
        claims.put("nonce", nonce.toString());

        validator = new CustomTokenValidator(configuration);
    }

    @Test
    public void testRegularValidation() throws Exception {
        final String idToken = generator.generate(claims);

        final IDTokenClaimsSet claimsSet = validator.validate(SignedJWT.parse(idToken), nonce);
        checkClaims(claimsSet);
    }

    @Test
    public void testAgentConnectValidation() throws Exception {
        when(configuration.getCustomParam("acr_values")).thenReturn("eidas1");
        claims.put("acr", "eidas1");
        final String idToken = generator.generate(claims);

        final IDTokenClaimsSet claimsSet = validator.validate(SignedJWT.parse(idToken), nonce);
        checkClaims(claimsSet);
    }

    @Test
    public void testAgentConnectValidationFailure() throws Exception {
        when(configuration.getCustomParam("acr_values")).thenReturn("eidas1");
        final String idToken = generator.generate(claims);

        try {
            validator.validate(SignedJWT.parse(idToken), nonce);
            fail("should fail");
        } catch (final BadJWTException e) {
            assertEquals(
                "[AGENTCONNECT] Bad acr claim in the ID token: it must match the provided value in the acr_values custom param",
                e.getMessage()
            );
        }
    }

    private void checkClaims(final IDTokenClaimsSet claimsSet) {
        assertEquals(SUBJECT, claimsSet.getSubject().toString());
        assertEquals(ISSUER, claimsSet.getIssuer().toString());
        assertEquals(CLIENT_ID, claimsSet.getAudience().get(0).toString());
        assertNotNull(claimsSet.getExpirationTime());
        assertNotNull(claimsSet.getIssueTime());
        assertEquals(nonce, claimsSet.getNonce());
    }
}
