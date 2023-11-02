package fr.gouv.vitamui.cas.x509;

import org.apereo.cas.util.crypto.CertUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link CertificateParser}.
 */
public class CertificateParserTest {

    private static final String ISSUER_DN = "EMAILADDRESS=admin@email.fr, CN=admin, OU=IT, O=AdminCompany, L=Paris, ST=Some-State, C=FR";
    private static final String SUBJECT_DN = "UID=348606, OID.2.16.840.1.113730.3.1.3=246635, C=FR, ST=Some-State, L=Paris, OU=IT, O=BobCompany, CN=bob, EMAILADDRESS=bob@email.fr";
    private static final String SUBJECT_ALTERNATE_NAME = "DNSName=www.site.fr, RFC822Name=altsubject@email.fr";

    private static X509Certificate cert;

    @BeforeClass
    public static void beforeClass() throws IOException {
        cert = CertUtils.readCertificate(new FileInputStream("src/test/resources/client.crt"));
    }

    @Test
    public void testIssuerDnNoParsingExpansion() throws CertificateParsingException {
        assertEquals(ISSUER_DN, CertificateParser.extract(cert, new X509AttributeMapping(X509CertificateAttributes.ISSUER_DN.toString(), null, null)));
    }

    @Test
    public void testIssuerDnParsingNoExpansion() throws CertificateParsingException {
        assertEquals("AdminCompany", CertificateParser.extract(cert, new X509AttributeMapping(X509CertificateAttributes.ISSUER_DN.toString(), ".*O=(.*), L=.*", null)));
    }

    @Test
    public void testIssuerDnParsingExpansion() throws CertificateParsingException {
        assertEquals("AdminCompany@email.fr", CertificateParser.extract(cert, new X509AttributeMapping(X509CertificateAttributes.ISSUER_DN.toString(), ".*O=(.*), L=.*", "{0}@email.fr")));
    }

    @Test
    public void testSubjectDnNoParsingExpansion() throws CertificateParsingException {
        assertEquals(SUBJECT_DN, CertificateParser.extract(cert, new X509AttributeMapping(X509CertificateAttributes.SUBJECT_DN.toString(), null, null)));
    }

    @Test
    public void testSubjectAlternateNameNoParsingExpansion() throws CertificateParsingException {
        final String extractSubjAltName = CertificateParser.extract(cert, new X509AttributeMapping(X509CertificateAttributes.SUBJECT_ALTERNATE_NAME.toString(), null, null));
        assertEquals(SUBJECT_ALTERNATE_NAME, extractSubjAltName);
    }

    @Test
    public void testSubjectAlternateNameParsingNoExpansion() throws CertificateParsingException {
        final String extractedMail = CertificateParser.extract(cert, new X509AttributeMapping(X509CertificateAttributes.SUBJECT_ALTERNATE_NAME.toString(), ".*RFC822Name=(.*)", null));
        assertEquals("altsubject@email.fr", extractedMail);
    }


}
