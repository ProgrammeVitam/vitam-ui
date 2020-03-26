package org.apereo.cas.config;

import fr.gouv.vitamui.cas.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration(value = "surrogateRestAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SurrogateRestAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    // customisation:
    @Autowired
    private CasExternalRestClient casExternalRestClient;

    @Autowired
    private Utils utils;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        // customisation:
        return new IamSurrogateRestAuthenticationService(casExternalRestClient, servicesManager.getObject(), utils);
    }
}
