package fr.gouv.vitamui.cas.x509;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests {@link CustomRequestHeaderX509CertificateExtractor}.
 */
public class CustomRequestHeaderX509CertificateExtractorTest {

    private static final String HEADER = "x-ssl-cert";

    private static String apacheHeader;

    @BeforeClass
    public static void readCertificate() throws IOException {
        // Apache forwards the PEM on a single line, line breaks replaced by spaces.
        apacheHeader = Files.readString(Path.of("src/test/resources/client.crt")).trim().replace("\n", " ");
    }

    @Test
    public void readsTheHeaderSetByATrustedProxy() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of("10.0.0.1"));

        final var certificates = extractor.extract(request("10.0.0.1"));

        assertNotNull(certificates);
        assertEquals(1, certificates.length);
    }

    @Test
    public void acceptsATrustedProxyRange() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of(" 10.0.0.0/24 "));

        assertNotNull(extractor.extract(request("10.0.0.42")));
    }

    @Test
    public void ignoresTheHeaderOfAnotherClient() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of("10.0.0.1"));

        assertNull(extractor.extract(request("192.168.1.10")));
    }

    @Test
    public void ignoresTheHeaderWhenNoProxyIsTrusted() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of());

        assertNull(extractor.extract(request("127.0.0.1")));
    }

    private static MockHttpServletRequest request(final String remoteAddr) {
        final var request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        request.addHeader(HEADER, apacheHeader);
        return request;
    }
}
