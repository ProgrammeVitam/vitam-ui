package fr.gouv.vitamui.cas.x509;

import org.apereo.cas.util.crypto.CertUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests {@link CertificateParser}.
 */
public class CertificateParserTest {

    private static final String ISSUER_DN = "EMAILADDRESS=admin@vitam, CN=admin, O=Vitam, ST=Some-State, C=FR";
    private static final String SUBJECT_DN = "EMAILADDRESS=bob@email, CN=bob, OU=IT, O=MyCompany, ST=Some-State, C=FR";

    private static X509Certificate cert;

    @BeforeClass
    public static void beforeClass() throws IOException {
        cert = CertUtils.readCertificate(new FileInputStream("src/test/resources/client.crt"));
    }

    @Test
    public void testIssuerDnNoParsingExpansion() throws CertificateParsingException {
        assertEquals(
            ISSUER_DN,
            CertificateParser.extract(
                cert,
                new X509AttributeMapping(X509CertificateAttributes.ISSUER_DN.toString(), null, null)
            )
        );
    }

    @Test
    public void testIssuerDnParsingNoExpansion() throws CertificateParsingException {
        assertEquals(
            "Vitam",
            CertificateParser.extract(
                cert,
                new X509AttributeMapping(X509CertificateAttributes.ISSUER_DN.toString(), ".*O=(.*), ST=.*", null)
            )
        );
    }

    @Test
    public void testIssuerDnParsingExpansion() throws CertificateParsingException {
        assertEquals(
            "Vitam@email",
            CertificateParser.extract(
                cert,
                new X509AttributeMapping(X509CertificateAttributes.ISSUER_DN.toString(), ".*O=(.*), ST=.*", "{0}@email")
            )
        );
    }

    @Test
    public void testSubjectDnNoParsingExpansion() throws CertificateParsingException {
        assertEquals(
            SUBJECT_DN,
            CertificateParser.extract(
                cert,
                new X509AttributeMapping(X509CertificateAttributes.SUBJECT_DN.toString(), null, null)
            )
        );
    }

    @Test
    public void testSubjectAlternateNameNoParsingExpansion() {
        try {
            CertificateParser.extract(
                cert,
                new X509AttributeMapping(X509CertificateAttributes.SUBJECT_ALTERNATE_NAME.toString(), null, null)
            );
            fail();
        } catch (final CertificateParsingException e) {
            assertEquals("Cannot find X509 value for: SUBJECT_ALTERNATE_NAME", e.getMessage());
        }
    }
}
