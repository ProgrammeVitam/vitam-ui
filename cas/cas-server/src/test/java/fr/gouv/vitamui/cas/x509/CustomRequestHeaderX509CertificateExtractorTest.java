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
    public void shouldReadTheHeaderSetByATrustedProxy() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of("10.0.0.1"));

        final var certificates = extractor.extract(request("10.0.0.1"));

        assertNotNull(certificates);
        assertEquals(1, certificates.length);
    }

    @Test
    public void shouldAcceptATrustedProxyRange() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of(" 10.0.0.0/24 "));

        assertNotNull(extractor.extract(request("10.0.0.42")));
    }

    @Test
    public void shouldIgnoreTheHeaderOfAnotherClient() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of("10.0.0.1"));

        assertNull(extractor.extract(request("192.168.1.10")));
    }

    @Test
    public void shouldIgnoreTheHeaderWhenNoProxyIsTrusted() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of());

        assertNull(extractor.extract(request("127.0.0.1")));
    }

    @Test
    public void shouldIgnoreTheForwardedAddressOfATrustedProxy() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of("10.0.0.1"));
        final var request = request("192.168.1.10");
        request.setRemoteAddr("10.0.0.1");

        assertNull(extractor.extract(request));
    }

    @Test
    public void shouldIgnoreTheHeaderWhenTheConnectionAddressIsUnknown() {
        final var extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, List.of("10.0.0.1"));
        final var request = request("10.0.0.1");
        request.removeAttribute(ConnectionPeerAddressValve.PEER_ADDRESS_ATTRIBUTE);
        request.setRemoteAddr("10.0.0.1");

        assertNull(extractor.extract(request));
    }

    private static MockHttpServletRequest request(final String connectionAddress) {
        final var request = new MockHttpServletRequest();
        request.setAttribute(ConnectionPeerAddressValve.PEER_ADDRESS_ATTRIBUTE, connectionAddress);
        request.addHeader(HEADER, apacheHeader);
        return request;
    }
}
