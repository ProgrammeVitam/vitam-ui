package fr.gouv.vitamui.commons.rest.client;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The local server presents a certificate issued for "localhost" only.
 */
class ClientFactoryTlsTest {

    private static final String PASSWORD = "azerty";

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

        assertThatThrownBy(() -> restClient.get().uri("/").retrieve().body(String.class)).isNotNull();
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

        assertThatThrownBy(() -> webClient.get().uri("/").retrieve().bodyToMono(String.class).block()).isNotNull();
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

        assertThatThrownBy(() -> webClient.get().uri("/").retrieve().bodyToMono(String.class).block()).isNotNull();
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
