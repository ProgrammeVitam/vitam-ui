package fr.gouv.vitamui.iam.internal.server.idp.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderBuilder;
import fr.gouv.vitamui.iam.common.utils.Saml2ClientBuilder;

/**
 * Tests {@link Saml2ClientBuilder} and {@link SpMetadataGenerator}.
 *
 *
 */
public final class SpMetadataGeneratorTest {

    private static final String CAS_URL = "http://cas/login";

    private Saml2ClientBuilder builder;

    @Before
    public void setUp() {
        builder = new Saml2ClientBuilder();
        builder.setCasLoginUrl(CAS_URL);
    }

    @Test
    public void testSimpleProvider() {
        assertFalse(builder.buildSaml2Client(new IdentityProviderDto()).isPresent());
    }

    @Test
    public void testRealProvider() throws Exception {
        final IdentityProviderDto provider = new IdentityProviderBuilder("google", "idp0", true, false, null,
                new ClassPathResource("test-idp/sp-test-keystore.jks"), "password", "password", new ClassPathResource("test-idp/idp-test-metadata.xml"),
                "clientId", false, "mailAttribute").build();
        final SpMetadataGenerator generator = new SpMetadataGenerator();
        generator.setSaml2ClientBuilder(builder);
        final String metadata = generator.generate(provider);
        assertTrue(metadata.contains("entityID=\"http://cas/login/idp0\""));
        assertTrue(metadata.contains(
                "<md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"http://cas/login?client_name=idp0\" index=\"0\"/>"));
        assertTrue(metadata.contains(
                "<md:KeyDescriptor use=\"signing\"><ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:X509Data><ds:X509Certificate>MIIDdzCCAl+gAwIBAgIESvhWIzANBgkqhkiG9w0BAQsFADBsMRAwDgYDVQQGEwdVbmtub3duMRAw"));
    }
}
