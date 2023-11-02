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
package fr.gouv.vitamui.cas.config;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.web.CustomCorsProcessor;
import fr.gouv.vitamui.cas.web.CustomOidcCasClientRedirectActionBuilder;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceCorsConfigurationSource;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Client;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Web customizations.
 */
@Configuration
public class WebConfig {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OAuth20CasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder(
        @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
        final OAuth20RequestParameterResolver oauthRequestParameterResolver,
        @Qualifier("oidcRequestSupport")
        final OidcRequestSupport oidcRequestSupport,
        @Qualifier("oauthCasClient")
        final Client oauthCasClient) {
        val builder = new CustomOidcCasClientRedirectActionBuilder(oidcRequestSupport, oauthRequestParameterResolver);
        val casClient = (CasClient) oauthCasClient;
        casClient.setRedirectionActionBuilder((webContext, sessionStore) -> builder.build(casClient, webContext));
        return builder;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CorsConfigurationSource corsHttpWebRequestConfigurationSource(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(ArgumentExtractor.BEAN_NAME)
        final ArgumentExtractor argumentExtractor,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new RegisteredServiceCorsConfigurationSource(casProperties, servicesManager, argumentExtractor);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public FilterRegistrationBean<CorsFilter> casCorsFilter(
        final CasConfigurationProperties casProperties,
        @Qualifier("corsHttpWebRequestConfigurationSource") final CorsConfigurationSource corsHttpWebRequestConfigurationSource,
        final IdentityProviderHelper identityProviderHelper,
        final ProvidersService providersService) {
        val filter = new CorsFilter(corsHttpWebRequestConfigurationSource);
        // CUSTO:
        filter.setCorsProcessor(new CustomCorsProcessor(providersService, identityProviderHelper));
        val bean = new FilterRegistrationBean<>(filter);
        bean.setName("casCorsFilter");
        bean.setAsyncSupported(true);
        bean.setOrder(0);
        bean.setEnabled(casProperties.getHttpWebRequest().getCors().isEnabled());
        return bean;
    }
}
