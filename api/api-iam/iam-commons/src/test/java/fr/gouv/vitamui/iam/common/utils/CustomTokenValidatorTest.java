/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.iam.common.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.oidc.config.OidcConfiguration;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test {@link CustomTokenValidator}.
 */
class CustomTokenValidatorTest {

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "123456789012345678901234567890ab";
    private static final String SUBJECT = "jleleu";
    private static final String ISSUER = "http://oidcprovider";

    private OidcConfiguration configuration;
    private JwtGenerator generator;
    private Map<String, Object> claims;
    private Nonce nonce;
    private CustomTokenValidator validator;

    @BeforeEach
    public void setUp() {
        configuration = mock(OidcConfiguration.class);
        final OIDCProviderMetadata metadata = mock(OIDCProviderMetadata.class);
        when(metadata.getIssuer()).thenReturn(new Issuer(ISSUER));
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

        validator = new CustomTokenValidator(configuration, metadata);
    }

    @Test
    void testRegularValidation() throws Exception {
        final String idToken = generator.generate(claims);

        final IDTokenClaimsSet claimsSet = validator.validateIdToken(SignedJWT.parse(idToken), nonce);
        checkClaims(claimsSet);
    }

    @Test
    void testAgentConnectValidation() throws Exception {
        when(configuration.getCustomParam("acr_values")).thenReturn("eidas1");
        claims.put("acr", "eidas1");
        final String idToken = generator.generate(claims);

        final IDTokenClaimsSet claimsSet = validator.validateIdToken(SignedJWT.parse(idToken), nonce);
        checkClaims(claimsSet);
    }

    @Test
    void testAgentConnectValidationFailure() throws Exception {
        when(configuration.getCustomParam("acr_values")).thenReturn("eidas1");
        final String idToken = generator.generate(claims);

        try {
            validator.validateIdToken(SignedJWT.parse(idToken), nonce);
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
