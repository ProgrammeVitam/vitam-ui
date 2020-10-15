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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.HttpPoolConfiguration;
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
import reactor.netty.http.client.HttpClient;

/**
 * A factory using Spring WebFlux {@link WebClient} to create each domain specific REST client.
 * The http connection is configured by the RestClientConfiguration object.
 * The factory handles SSL via x509 certificates.
 *
 *
 */

public class BaseWebClientFactory implements WebClientFactory {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseWebClientFactory.class);

    private final WebClient webClient;

    private final String baseUrl;

    /**
     * This method don't use WebBuilder configured by spring boot
     * @param restClientConfiguration
     */
    @Deprecated
    public BaseWebClientFactory(final RestClientConfiguration restClientConfiguration) {
        this(restClientConfiguration, null, WebClient.builder());
    }

    /**
     * This method don't use WebBuilder configured by spring boot
     * @param restClientConfiguration
     * @param httpPoolConfig
     */
    @Deprecated
    public BaseWebClientFactory(final RestClientConfiguration restClientConfiguration, final HttpPoolConfiguration httpPoolConfig) {
        this(restClientConfiguration, httpPoolConfig, WebClient.builder());
    }

    public BaseWebClientFactory(final RestClientConfiguration restClientConfiguration, final WebClient.Builder webClientBuilder) {
        this(restClientConfiguration, null, webClientBuilder);
    }

    public BaseWebClientFactory(final RestClientConfiguration restClientConfig, final HttpPoolConfiguration httpPoolConfig, final WebClient.Builder webClientBuilder) {
        Assert.notNull(restClientConfig, "Rest client configuration must be specified");
        final boolean useSSL = restClientConfig.isSecure();
        baseUrl = RestUtils.getScheme(useSSL) + restClientConfig.getServerHost() + ":" + restClientConfig.getServerPort();

        final ClientHttpConnector httpConnector = createClientHttpConnector(restClientConfig);

        webClient = webClientBuilder.baseUrl(baseUrl).clientConnector(httpConnector).build();
    }

    private ClientHttpConnector createClientHttpConnector(final RestClientConfiguration restClientConfig) {
        try {
            final boolean useSSL = restClientConfig.isSecure();
            final SslContext sslContext = useSSL ? buildSSLContext(restClientConfig) : null;

            HttpClient httpClient = null;

            if (useSSL) {
                // secure must precede tcpConfiguration in order for sslContext configuration to be applied.
                httpClient = HttpClient.create().secure(sslSpec -> sslSpec.sslContext(sslContext))
                        .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, restClientConfig.getConnectTimeOut() * 1000)
                                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(restClientConfig.getReadTimeOut()))
                                        .addHandlerLast(new WriteTimeoutHandler(restClientConfig.getWriteTimeOut()))));
            }
            else {
                httpClient = HttpClient.create()
                        .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, restClientConfig.getConnectTimeOut() * 1000)
                                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(restClientConfig.getReadTimeOut()))
                                        .addHandlerLast(new WriteTimeoutHandler(restClientConfig.getWriteTimeOut()))));
            }

            return new ReactorClientHttpConnector(httpClient);
        }
        catch (final Exception e) {
            throw new ApplicationServerException(e);
        }

    }

    /*
     * Create an SSLContext that uses client.p12 as the client certificate
     * and the truststore.jks as the trust material (trusted CA certificates).
     * Then create SSLConnectionSocketFactory to register with the HTTPS protocol.
     */
    private SslContext buildSSLContext(final RestClientConfiguration restClientConfig) {
        if (restClientConfig == null || restClientConfig.getSslConfiguration() == null) {
            throw new ApplicationServerException("SSL Configuration is not defined. Unable to configure the SSLConnection");
        }

        final SSLConfiguration.CertificateStoreConfiguration ks = restClientConfig.getSslConfiguration().getKeystore();
        final SSLConfiguration.CertificateStoreConfiguration ts = restClientConfig.getSslConfiguration().getTruststore();

        try {

            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            sslContextBuilder = sslContextBuilder.clientAuth(ClientAuth.NONE);

            if (restClientConfig.isNoClientAuthentication()) {
                LOGGER.warn("By deactivating the authentication client we deprive ourselves of two-way authentication.");

            } else {
                if (ks != null) {
                    sslContextBuilder = sslContextBuilder.keyManager(createKeyManagerFactory(ks.getType(), ks.getKeyPath(), ks.getKeyPassword().toCharArray()));
                }

            }

            if (restClientConfig.getSslConfiguration().isHostnameVerification()) {
                final TrustManagerFactory tmfactory = createTrustManagerFactory(ts.getType(), ts.getKeyPath(), ts.getKeyPassword().toCharArray());
                sslContextBuilder = sslContextBuilder.sslProvider(SslProvider.JDK).trustManager(tmfactory);
            }
            else {
                return sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            }

            return sslContextBuilder.build();
        }
        catch (GeneralSecurityException | IOException e) {
            LOGGER.warn("Unable to build the Registry<ConnectionSocketFactory>.", e);
            throw new ApplicationServerException(e);
        }
    }

    private KeyManagerFactory createKeyManagerFactory(final String type, final String filename, final char[] password)
            throws IOException, GeneralSecurityException {

        final KeyStore keyStore = loadPkcs(StringUtils.isEmpty(type) ? KeyStore.getDefaultType() : type, filename, password);

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);

        return keyManagerFactory;
    }

    private KeyStore loadPkcs(final String type, final String filename, final char[] password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance(type);
        final File key = ResourceUtils.getFile(filename);
        try (InputStream in = new FileInputStream(key)) {
            keyStore.load(in, password);
        }
        return keyStore;
    }

    private TrustManagerFactory createTrustManagerFactory(final String type, final String filename, final char[] password)
            throws GeneralSecurityException, IOException {

        final KeyStore trustStore = loadPkcs(StringUtils.isEmpty(type) ? KeyStore.getDefaultType() : type, filename, password);

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        return trustManagerFactory;
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
