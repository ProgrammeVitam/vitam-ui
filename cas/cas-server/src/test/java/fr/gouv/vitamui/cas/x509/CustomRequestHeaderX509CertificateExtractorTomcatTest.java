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

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteIpValve;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link CustomRequestHeaderX509CertificateExtractor} behind a real Tomcat, with the {@link RemoteIpValve} that
 * {@code server.tomcat.remoteip.*} installs: the trusted proxy must be the peer of the connection, whatever
 * {@code X-Forwarded-For} claims.
 */
public class CustomRequestHeaderX509CertificateExtractorTomcatTest {

    private static final String HEADER = "x-ssl-cert";

    private static final String LOOPBACK = "127.0.0.1";

    @Rule
    public final TemporaryFolder baseDir = new TemporaryFolder();

    private Tomcat tomcat;

    private CustomRequestHeaderX509CertificateExtractor extractor;

    private String certificateHeader;

    @Before
    public void setUp() throws IOException {
        certificateHeader = Files.readString(Path.of("src/test/resources/client.crt")).trim().replace("\n", " ");
    }

    @After
    public void tearDown() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    public void shouldReadTheHeaderFromATrustedProxyThatForwardsTheClientAddress() throws Exception {
        start(List.of(LOOPBACK));

        assertEquals("certificate", get("203.0.113.5"));
    }

    @Test
    public void shouldIgnoreTheHeaderWhenOnlyTheForwardedAddressIsTrusted() throws Exception {
        start(List.of("10.0.0.1"));

        assertEquals("none", get("10.0.0.1"));
    }

    private void start(final List<String> trustedProxies) throws LifecycleException {
        extractor = new CustomRequestHeaderX509CertificateExtractor(HEADER, false, trustedProxies);
        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getRoot().getAbsolutePath());
        tomcat.setHostname(LOOPBACK);
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", LOOPBACK);

        // The same valve as with the server.tomcat.remoteip.* properties of application.properties.
        final var remoteIpValve = new RemoteIpValve();
        remoteIpValve.setRemoteIpHeader("X-FORWARDED-FOR");
        remoteIpValve.setProtocolHeader("X-Forwarded-Proto");
        remoteIpValve.setPortHeader("X-Forwarded-Port");
        tomcat.getEngine().getPipeline().addValve(remoteIpValve);
        tomcat.getEngine().getPipeline().addValve(new ConnectionPeerAddressValve());

        final var context = tomcat.addContext("", null);
        Tomcat.addServlet(
            context,
            "x509",
            new HttpServlet() {
                @Override
                protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
                    throws IOException {
                    response.getWriter().write(extractor.extract(request) == null ? "none" : "certificate");
                }
            }
        );
        context.addServletMappingDecoded("/", "x509");
        tomcat.start();
    }

    private String get(final String forwardedFor) throws IOException, InterruptedException {
        final var port = tomcat.getConnector().getLocalPort();
        final var request = HttpRequest.newBuilder(URI.create("http://" + LOOPBACK + ":" + port + "/"))
            .header("X-Forwarded-For", forwardedFor)
            .header(HEADER, certificateHeader)
            .build();
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }
    }
}
