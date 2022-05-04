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
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.customersadmin.configs;

import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CustomerMgtConfig {

    protected static final String GENERIC_CERTIFICATE = "generic-it";

    @Value("${generic-cert}")
    private String genericCert;

    @Value("${mongo.security.uri}")
    private String mongoSecurityUri;

    @Value("${iam-client.ssl.truststore.password}")
    protected String iamTruststorePassword;

    @Value("${certs-folder}")
    protected String certsFolder;

    @Value("${iam-client.host}")
    protected String iamServerHost;

    @Value("${iam-client.port}")
    protected Integer iamServerPort;

    @Value("${iam-client.ssl.keystore.path}")
    private String iamKeystoreFilePath;

    @Value("${iam-client.ssl.truststore.path}")
    protected String iamTrustStoreFilePath;

    @Value("${iam-client.ssl.keystore.password}")
    protected String iamKeystorePassword;

    @Autowired
    protected WebClient.Builder webClientBuilder;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Autowired
    SSLConfiguration sSLConfiguration;


    @Bean
    @DependsOn("restClientConfiguration")
    public IamExternalWebClientFactory getIamWebClientFactory(
        @Autowired RestClientConfiguration restClientConfiguration) {
        final IamExternalWebClientFactory restClientFactory =
            new IamExternalWebClientFactory(restClientConfiguration, webClientBuilder);
        return restClientFactory;
    }

    @Bean
    public RestClientConfiguration restClientConfiguration(@Autowired SSLConfiguration sSLConfiguration) {
        return getRestClientConfiguration(iamServerHost, iamServerPort, true, sSLConfiguration);
    }

    protected RestClientConfiguration getRestClientConfiguration(final String host, final int port,
        final boolean secure, final SSLConfiguration sslConfig) {
        final RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setServerHost(host);
        restClientConfiguration.setServerPort(port);
        restClientConfiguration.setSecure(secure);
        if (sslConfig != null) {
            restClientConfiguration.setSslConfiguration(sslConfig);
        }

        return restClientConfiguration;
    }

    @Bean
    public SSLConfiguration sSLConfiguration() {

        final String keystorePath =
            getClass().getClassLoader().getResource(certsFolder + GENERIC_CERTIFICATE + ".jks").getPath();
        final String trustStorePath = getClass().getClassLoader().getResource(iamTrustStoreFilePath).getPath();
        final SSLConfiguration.CertificateStoreConfiguration keyStore =
            new SSLConfiguration.CertificateStoreConfiguration();
        keyStore.setKeyPath(keystorePath);
        keyStore.setKeyPassword(iamKeystorePassword);
        keyStore.setType("JKS");
        final SSLConfiguration.CertificateStoreConfiguration trustStore =
            new SSLConfiguration.CertificateStoreConfiguration();
        trustStore.setKeyPath(trustStorePath);
        trustStore.setKeyPassword(iamTruststorePassword);
        trustStore.setType("JKS");

        final SSLConfiguration sslConfig = new SSLConfiguration();
        sslConfig.setKeystore(keyStore);
        sslConfig.setTruststore(trustStore);

        return sslConfig;
    }


    @Bean
    @DependsOn("restClientConfiguration")
    public IamExternalRestClientFactory iamRestClientFactory(
        @Autowired RestClientConfiguration restClientConfiguration) {
        final IamExternalRestClientFactory restClientFactory = new IamExternalRestClientFactory(
            restClientConfiguration, restTemplateBuilder);
        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        //interceptors.add(new RegisterRestQueryInterceptor());
        //restClientFactory.setRestClientInterceptor(interceptors);
        return restClientFactory;
    }



}
