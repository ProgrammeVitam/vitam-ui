/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.rest.client;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * The local server presents a certificate issued for "localhost" only.
 */
class ClientFactoryTlsTest {

    private static final String PASSWORD = "azerty";

    // JDK hostname verification (WebClient)
    private static final String HOSTNAME_MISMATCH = "No subject alternative names matching IP address 127.0.0.1";

    // Apache HttpClient hostname verification (RestClient)
    private static final String REST_HOSTNAME_MISMATCH = "Certificate for <127.0.0.1> doesn't match";

    private static final String UNTRUSTED = "PKIX path building failed";

    private HttpsServer server;

    @BeforeEach
    void startServer() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:keystore_localhost.p12"))) {
            keyStore.load(in, PASSWORD.toCharArray());
        }
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, PASSWORD.toCharArray());
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        server.createContext("/", exchange -> {
            final byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void sslConfiguration_should_verify_hostname_by_default() {
        assertThat(new SSLConfiguration().isHostnameVerification()).isTrue();
    }

    @Test
    void restClient_should_reject_certificate_not_matching_host_by_default() throws Exception {
        final RestClient restClient = new BaseRestClientFactory(
            clientConfiguration("127.0.0.1", "classpath:truststore_localhost.p12", null),
            RestClient.builder()
        ).getRestClient();

        assertTlsFailure(() -> restClient.get().uri("/").retrieve().body(String.class), REST_HOSTNAME_MISMATCH);
    }

    @Test
    void restClient_should_accept_certificate_matching_host() throws Exception {
        final RestClient restClient = new BaseRestClientFactory(
            clientConfiguration("localhost", "classpath:truststore_localhost.p12", true),
            RestClient.builder()
        ).getRestClient();

        assertThat(restClient.get().uri("/").retrieve().body(String.class)).isEqualTo("ok");
    }

    @Test
    void webClient_should_reject_untrusted_certificate_when_hostname_verification_is_disabled() throws Exception {
        final WebClient webClient = new BaseWebClientFactory(
            clientConfiguration("localhost", "classpath:truststore_rest-app.jks", false),
            WebClient.builder()
        ).getWebClient();

        assertTlsFailure(() -> webClient.get().uri("/").retrieve().bodyToMono(String.class).block(), UNTRUSTED);
    }

    @Test
    void webClient_should_accept_trusted_certificate_not_matching_host_when_hostname_verification_is_disabled()
        throws Exception {
        final WebClient webClient = new BaseWebClientFactory(
            clientConfiguration("127.0.0.1", "classpath:truststore_localhost.p12", false),
            WebClient.builder()
        ).getWebClient();

        assertThat(webClient.get().uri("/").retrieve().bodyToMono(String.class).block()).isEqualTo("ok");
    }

    @Test
    void webClient_should_reject_certificate_not_matching_host_when_hostname_verification_is_enabled()
        throws Exception {
        final WebClient webClient = new BaseWebClientFactory(
            clientConfiguration("127.0.0.1", "classpath:truststore_localhost.p12", true),
            WebClient.builder()
        ).getWebClient();

        assertTlsFailure(() -> webClient.get().uri("/").retrieve().bodyToMono(String.class).block(), HOSTNAME_MISMATCH);
    }

    @Test
    void webClient_should_require_a_truststore() throws Exception {
        final RestClientConfiguration configuration = clientConfiguration(
            "localhost",
            "classpath:truststore_localhost.p12",
            false
        );
        configuration.getSslConfiguration().setTruststore(null);
        assertThatThrownBy(() -> new BaseWebClientFactory(configuration, WebClient.builder()))
            .isInstanceOf(ApplicationServerException.class)
            .hasStackTraceContaining("SSL truststore is not defined");
    }

    private static void assertTlsFailure(final ThrowingCallable call, final String expectedCause) {
        final Throwable thrown = catchThrowable(call);
        assertThat(thrown).isNotNull().hasStackTraceContaining(expectedCause);
        assertThat(causes(thrown)).anyMatch(SSLException.class::isInstance);
    }

    private static List<Throwable> causes(final Throwable throwable) {
        final List<Throwable> causes = new ArrayList<>();
        for (Throwable cause = throwable; cause != null && !causes.contains(cause); cause = cause.getCause()) {
            causes.add(cause);
        }
        return causes;
    }

    private RestClientConfiguration clientConfiguration(
        final String host,
        final String truststore,
        final Boolean hostnameVerification
    ) throws Exception {
        final SSLConfiguration.CertificateStoreConfiguration ts = new SSLConfiguration.CertificateStoreConfiguration()
            .setType(truststore.endsWith(".jks") ? "JKS" : "PKCS12")
            .setKeyPath(ResourceUtils.getFile(truststore).getAbsolutePath())
            .setKeyPassword(PASSWORD);
        final SSLConfiguration sslConfiguration = new SSLConfiguration().setTruststore(ts);
        if (hostnameVerification != null) {
            sslConfiguration.setHostnameVerification(hostnameVerification);
        }
        final RestClientConfiguration configuration = new RestClientConfiguration();
        configuration.setServerHost(host);
        configuration.setServerPort(server.getAddress().getPort());
        configuration.setSecure(true);
        configuration.setSslConfiguration(sslConfiguration);
        return configuration;
    }
}
