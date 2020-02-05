package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.gouv.vitamui.cas.authentication.IamSurrogateRestAuthenticationService;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;

/**
 * Overrides the configuration class from the CAS server, using the CasExternalRestClient.
 *
 *
 */
@Configuration("surrogateRestAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateRestAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    // customisation JLE:
    @Autowired
    private CasExternalRestClient casExternalRestClient;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        // customisation JLE:
        /*final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
        LOGGER.debug("Using REST endpoint [{}] with method [{}] to locate surrogate accounts",
            su.getRest().getUrl(), su.getRest().getMethod());*/
        return new IamSurrogateRestAuthenticationService(casExternalRestClient, servicesManager);
    }
}
