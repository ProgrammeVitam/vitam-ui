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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.HttpPoolConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.commons.rest.util.RestUtils;

/**
 * A rest client factory to create each domain specific REST client. The http connection is configured by the
 * RestClientConfiguration object. The factory implements a connection pool configured by the HttpPoolConfiguration
 * object and handles SSL via x509 certificates.
 *
 *
 */

public class BaseRestClientFactory implements RestClientFactory {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseRestClientFactory.class);

    private final RestTemplate restTemplate;

    private final String baseUrl;

    protected int connectTimeout = 500000;

    protected int connectionRequestTimeout = 500000;

    protected int socketTimeout = 500000;

    public BaseRestClientFactory(final RestClientConfiguration restClientConfiguration, final RestTemplateBuilder restTemplateBuilder) {
        this(restClientConfiguration, null, restTemplateBuilder);
    }

    public BaseRestClientFactory(final RestClientConfiguration restClientConfig, final HttpPoolConfiguration httpPoolConfig,
            final RestTemplateBuilder restTemplateBuilder) {
        Assert.notNull(restClientConfig, "Rest client configuration must be specified");

        final boolean useSSL = restClientConfig.isSecure();
        baseUrl = RestUtils.getScheme(useSSL) + restClientConfig.getServerHost() + ":" + restClientConfig.getServerPort();

        HttpPoolConfiguration myPoolConfig = httpPoolConfig;
        // configure the pool from the restClientConfig if the value of poolMaxTotal is positive
        if(restClientConfig.getPoolMaxTotal() >= 0) {
            myPoolConfig = new HttpPoolConfiguration();
            myPoolConfig.setMaxTotal(restClientConfig.getPoolMaxTotal());
            myPoolConfig.setMaxPerRoute(restClientConfig.getPoolMaxPerRoute());
        }

        final Registry<ConnectionSocketFactory> csfRegistry = useSSL ? buildRegistry(restClientConfig.getSslConfiguration()) : null;
        final PoolingHttpClientConnectionManager connectionManager = buildConnectionManager(myPoolConfig, csfRegistry);
        final RequestConfig requestConfig = buildRequestConfig();
        final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

        restTemplate = restTemplateBuilder.errorHandler(new ErrorHandler()).build();
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new CustomHttpComponentsClientHttpRequestFactory(httpClient, UUID.randomUUID().toString())));
    }

    /*
     * Create an SSLContext that uses client.p12 as the client certificate
     * and the truststore.jks as the trust material (trusted CA certificates).
     * Then create SSLConnectionSocketFactory to register with the HTTPS protocol.
     */
    private Registry<ConnectionSocketFactory> buildRegistry(final SSLConfiguration sslConfiguration) {
        if (sslConfiguration == null) {
            throw new ApplicationServerException("SSL Configuration is not defined. Unable to configure the SSLConnection");
        }

        final SSLConfiguration.CertificateStoreConfiguration ks = sslConfiguration.getKeystore();
        final SSLConfiguration.CertificateStoreConfiguration ts = sslConfiguration.getTruststore();

        SSLContext sslContext = null;
        try {

            final SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

            if (ks != null) {
                final KeyStore keyStore = loadPkcs(ks.getType(), ks.getKeyPath(), ks.getKeyPassword().toCharArray());
                sslContextBuilder.loadKeyMaterial(keyStore, ks.getKeyPassword().toCharArray());
            }

            sslContext = sslContextBuilder.loadTrustMaterial(new File(ts.getKeyPath()), ts.getKeyPassword().toCharArray()).setProtocol("TLS")
                    .setSecureRandom(new java.security.SecureRandom()).build();
        }
        catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException e) {
            LOGGER.error("Unable to build the Registry<ConnectionSocketFactory>.", e);
            LOGGER.error("KeyPath: " + sslConfiguration.getKeystore().getKeyPath());

            throw new ApplicationServerException(e);
        }

        final HostnameVerifier hostnameVerifier = sslConfiguration.isHostnameVerification() ? null : TrustAllHostnameVerifier.INSTANCE;
        final SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        return RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslFactory).build();
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

    /*
     * Create a ClientConnectionPoolManager that maintains a pool of HttpClientConnections and is able to service connection
     * requests from multiple execution threads. Connections are pooled on a per route basis. A request for a route which
     * already the manager has persistent connections for available in the pool will be services by leasing a connection
     * from the pool rather than creating a brand new connection.
     */
    private PoolingHttpClientConnectionManager buildConnectionManager(final HttpPoolConfiguration poolConfig,
            final Registry<ConnectionSocketFactory> socketFactoryRegistry) {

        final PoolingHttpClientConnectionManager connectionManager = (socketFactoryRegistry != null)
                ? new PoolingHttpClientConnectionManager(socketFactoryRegistry)
                : new PoolingHttpClientConnectionManager();

        if (poolConfig != null) {
            connectionManager.setMaxTotal(poolConfig.getMaxTotal());
            // Default max per route is used in case it's not set for a specific route
            connectionManager.setDefaultMaxPerRoute(poolConfig.getMaxPerRoute());

            for (final HttpPoolConfiguration.HostConfiguration hostConfig : poolConfig.getHostConfigurations()) {
                final HttpHost host = new HttpHost(hostConfig.getHost(), hostConfig.getPort(), hostConfig.getScheme());
                // Max per route for a specific hosts route
                connectionManager.setMaxPerRoute(new HttpRoute(host), hostConfig.getMaxPerRoute());
            }
        }
        return connectionManager;
    }

    private RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout)
                .build();
    }

    @Override
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setRestClientInterceptor(final List<ClientHttpRequestInterceptor> interceptors) {
        restTemplate.setInterceptors(interceptors);
    }

}
