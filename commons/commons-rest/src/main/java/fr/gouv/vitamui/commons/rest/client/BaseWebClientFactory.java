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

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.rest.client.configuration.HttpPoolConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.ProxyProperties;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * A factory using Spring WebFlux {@link WebClient} to create each domain specific REST client.
 *
 * Migrated to Spring Boot 4.0 / Spring Framework 7.0 / Reactor Netty 2.x (Netty 4.2.x):
 * - Add spring-boot-starter-webclient dependency (WebClient now its own starter in Boot 4)
 * - Removed deprecated TcpClient / tcpConfiguration() — fully removed in this line
 * - Removed deprecated constructors that bypassed Spring Boot's WebClient.Builder
 * - HttpClient configured via its direct fluent API only
 * - WebClientCustomizer and friends moved to org.springframework.boot.webclient package
 */
public class BaseWebClientFactory implements WebClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebClientFactory.class);

    private final WebClient webClient;
    private String baseUrl;

    public BaseWebClientFactory(
        final RestClientConfiguration restClientConfiguration,
        final WebClient.Builder webClientBuilder
    ) {
        this(restClientConfiguration, null, webClientBuilder, null, true);
    }

    public BaseWebClientFactory(
        final RestClientConfiguration restClientConfiguration,
        final WebClient.Builder webClientBuilder,
        final String baseUrl
    ) {
        this(restClientConfiguration, null, webClientBuilder, baseUrl, true);
    }

    public BaseWebClientFactory(
        final RestClientConfiguration restClientConfiguration,
        final HttpPoolConfiguration httpPoolConfig,
        final WebClient.Builder webClientBuilder
    ) {
        this(restClientConfiguration, httpPoolConfig, webClientBuilder, null, true);
    }

    public BaseWebClientFactory(
        final RestClientConfiguration restClientConfiguration,
        final HttpPoolConfiguration httpPoolConfig,
        final WebClient.Builder webClientBuilder,
        final boolean useBaseUrl
    ) {
        this(restClientConfiguration, httpPoolConfig, webClientBuilder, null, useBaseUrl);
    }

    public BaseWebClientFactory(
        final RestClientConfiguration restClientConfiguration,
        final HttpPoolConfiguration httpPoolConfig,
        final WebClient.Builder webClientBuilder,
        final String webClientBaseUrl
    ) {
        this(restClientConfiguration, httpPoolConfig, webClientBuilder, webClientBaseUrl, true);
    }

    private BaseWebClientFactory(
        final RestClientConfiguration restClientConfig,
        final HttpPoolConfiguration httpPoolConfig,
        final WebClient.Builder webClientBuilder,
        final String webClientBaseUrl,
        final boolean useBaseUrl
    ) {
        Assert.notNull(restClientConfig, "Rest client configuration must be specified");

        webClientBuilder.clientConnector(createClientHttpConnector(restClientConfig));

        if (useBaseUrl) {
            this.baseUrl = StringUtils.isBlank(webClientBaseUrl)
                ? buildBaseUrl(restClientConfig, restClientConfig.isSecure())
                : webClientBaseUrl;
            webClientBuilder.baseUrl(this.baseUrl);
        }

        this.webClient = webClientBuilder
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs(
                        configurer -> configurer.defaultCodecs().maxInMemorySize(restClientConfig.getMaxInMemorySize())
                    )
                    .build()
            )
            .build();
    }

    private String buildBaseUrl(final RestClientConfiguration restClientConfig, final boolean useSSL) {
        return RestUtils.getScheme(useSSL) + restClientConfig.getServerHost() + ":" + restClientConfig.getServerPort();
    }

    /**
     * Build the HttpClient using Reactor Netty's modern fluent API.
     * tcpConfiguration() has been fully removed in Reactor Netty 2.x (Boot 4 / Netty 4.2.x).
     * All options (.option, .doOnConnected, .proxy) are applied directly on HttpClient.
     */
    private ClientHttpConnector createClientHttpConnector(final RestClientConfiguration restClientConfig) {
        try {
            HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, restClientConfig.getConnectTimeOut() * 1000)
                .doOnConnected(
                    connection ->
                        connection
                            .addHandlerLast(new ReadTimeoutHandler(restClientConfig.getReadTimeOut()))
                            .addHandlerLast(new WriteTimeoutHandler(restClientConfig.getWriteTimeOut()))
                );

            if (restClientConfig.isSecure()) {
                final SslContext sslContext = buildSSLContext(restClientConfig);
                // .secure() must come before .proxy() — Netty applies SSL at the transport layer first
                httpClient = httpClient.secure(sslSpec -> sslSpec.sslContext(sslContext));
            }

            final ProxyProperties proxyProperties = restClientConfig.getProxy();
            if (Objects.nonNull(proxyProperties) && proxyProperties.isEnabled()) {
                httpClient = httpClient.proxy(proxy -> buildProxySpec(proxyProperties, proxy));
            }

            return new ReactorClientHttpConnector(httpClient);
        } catch (final Exception e) {
            throw new ApplicationServerException(e);
        }
    }

    private ProxyProvider.Builder buildProxySpec(
        final ProxyProperties proxyProperties,
        final ProxyProvider.TypeSpec proxySpec
    ) {
        return proxySpec
            .type(proxyProperties.getType())
            .host(proxyProperties.getHost())
            .port(proxyProperties.getPort())
            .username(proxyProperties.getUsername())
            .password(user -> proxyProperties.getPassword());
    }

    private SslContext buildSSLContext(final RestClientConfiguration restClientConfig) {
        if (restClientConfig.getSslConfiguration() == null) {
            throw new ApplicationServerException(
                "SSL Configuration is not defined. Unable to configure the SSLConnection"
            );
        }

        final SSLConfiguration.CertificateStoreConfiguration ks = restClientConfig.getSslConfiguration().getKeystore();
        final SSLConfiguration.CertificateStoreConfiguration ts = restClientConfig
            .getSslConfiguration()
            .getTruststore();

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient().clientAuth(ClientAuth.NONE);

            if (restClientConfig.isNoClientAuthentication()) {
                LOGGER.warn("Client authentication is disabled — mutual TLS will not be used.");
            } else if (ks != null) {
                sslContextBuilder = sslContextBuilder.keyManager(
                    createKeyManagerFactory(ks.getType(), ks.getKeyPath(), ks.getKeyPassword().toCharArray())
                );
            }

            if (restClientConfig.getSslConfiguration().isHostnameVerification()) {
                final TrustManagerFactory tmf = createTrustManagerFactory(
                    ts.getType(),
                    ts.getKeyPath(),
                    ts.getKeyPassword().toCharArray()
                );
                return sslContextBuilder.sslProvider(SslProvider.JDK).trustManager(tmf).build();
            } else {
                return sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            }
        } catch (final GeneralSecurityException | IOException e) {
            LOGGER.warn("Unable to build SslContext.", e);
            throw new ApplicationServerException(e);
        }
    }

    private KeyManagerFactory createKeyManagerFactory(final String type, final String filename, final char[] password)
        throws IOException, GeneralSecurityException {
        final KeyStore keyStore = loadKeyStore(
            StringUtils.isEmpty(type) ? KeyStore.getDefaultType() : type,
            filename,
            password
        );
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);
        return kmf;
    }

    private TrustManagerFactory createTrustManagerFactory(
        final String type,
        final String filename,
        final char[] password
    ) throws GeneralSecurityException, IOException {
        final KeyStore trustStore = loadKeyStore(
            StringUtils.isEmpty(type) ? KeyStore.getDefaultType() : type,
            filename,
            password
        );
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf;
    }

    private KeyStore loadKeyStore(final String type, final String filename, final char[] password)
        throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance(type);
        final File key = ResourceUtils.getFile(filename);
        try (final InputStream in = new FileInputStream(key)) {
            keyStore.load(in, password);
        }
        return keyStore;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public WebClient getWebClient() {
        return webClient;
    }
}
