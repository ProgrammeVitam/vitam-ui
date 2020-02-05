package org.apereo.cas.config.pm;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.gouv.vitamui.cas.pm.IamRestPasswordManagementService;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;

/**
 * Overrides the configuration class from the CAS server, using the CasExternalRestClient.
 *
 *
 */
@Configuration("restPasswordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestPasswordManagementConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    // customisation JLE:
    @Autowired
    private CasExternalRestClient casExternalRestClient;

    @Autowired
    private ProvidersService providersService;

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        // customisation JLE:
        return new IamRestPasswordManagementService(casExternalRestClient, casProperties.getAuthn().getPm(), providersService, identityProviderHelper);
    }
}
