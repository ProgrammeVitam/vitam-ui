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
package fr.gouv.vitamui.iam.external.server.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.reactive.function.client.WebClient;

import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.logbook.LogbookInternalRestClient;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.CasInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.CustomerInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.CustomerInternalWebClient;
import fr.gouv.vitamui.iam.internal.client.ExternalParametersInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.GroupInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.IamInternalWebClientFactory;
import fr.gouv.vitamui.iam.internal.client.IdentityProviderInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.OwnerInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.ProfileInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.SubrogationInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.TenantInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
@Import({ RestExceptionHandler.class, SwaggerConfiguration.class, HttpMessageConvertersAutoConfiguration.class })
public class ApiIamServerConfig extends AbstractContextConfiguration {

    @Bean
    public MultipartResolver multipartResolver() {
        final MultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        return commonsMultipartResolver;
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public FilterRegistrationBean multipartFilterRegistrationBean() {
        final MultipartFilter multipartFilter = new MultipartFilter();
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(multipartFilter);
        filterRegistrationBean.addInitParameter("multipartResolverBeanName", "commonsMultipartResolver");
        return filterRegistrationBean;
    }

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(final ApiIamApplicationProperties apiIamApplicationProperties,
            final RestTemplateBuilder restTemplateBuilder) {
        return new SecurityRestClientFactory(apiIamApplicationProperties.getSecurityClient(), restTemplateBuilder);
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
    }

    @Bean
    public ExternalSecurityService externalSecurityService() {
        return new ExternalSecurityService();
    }

    @Bean
    public ExternalAuthentificationService externalAuthentificationService(final ContextRestClient contextRestClient,
            final UserInternalRestClient userInternalRestClient) {
        return new ExternalAuthentificationService(contextRestClient, userInternalRestClient);
    }

    @Bean
    public ExternalApiAuthenticationProvider apiAuthenticationProvider(final ExternalAuthentificationService externalAuthentificationService) {
        return new ExternalApiAuthenticationProvider(externalAuthentificationService);
    }

    @Bean
    public IamInternalRestClientFactory iamInternalRestClientFactory(final ApiIamApplicationProperties apiIamApplicationProperties,
            final RestTemplateBuilder restTemplateBuilder) {
        return new IamInternalRestClientFactory(apiIamApplicationProperties.getIamInternalClient(), restTemplateBuilder);

    }

    @Bean
    public IamInternalWebClientFactory internalWebClientFactory(final ApiIamApplicationProperties apiIamApplicationProperties, final WebClient.Builder webClientBuilder) {
        return new IamInternalWebClientFactory(apiIamApplicationProperties.getIamInternalClient(), webClientBuilder);

    }

    @Bean
    public CustomerInternalRestClient customerInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getCustomerInternalRestClient();
    }

    @Bean
    public CustomerInternalWebClient customerInternalV2RestClient(final IamInternalWebClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getCustomerInternalRestClient();
    }

    @Bean
    public IdentityProviderInternalRestClient identityProviderInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getIdentityProviderInternalRestClient();
    }

    @Bean
    public ProfileInternalRestClient profileInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getProfileInternalRestClient();
    }

    @Bean
    public GroupInternalRestClient groupInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getProfileGroupInternalRestClient();
    }

    @Bean
    public TenantInternalRestClient tenantInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getTenantInternalRestClient();
    }

    @Bean
    public UserInternalRestClient userInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getUserInternalRestClient();
    }

    @Bean
    public OwnerInternalRestClient ownerInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getOwnerInternalRestClient();
    }

    @Bean
    public SubrogationInternalRestClient subrogationInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getSubrogationInternalRestClient();
    }

    @Bean
    public CasInternalRestClient casInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getCasInternalRestClient();
    }

    @Bean
    public LogbookInternalRestClient<InternalHttpContext> logbookInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getLogbookInternalRestClient();
    }

    @Bean
    public ApplicationInternalRestClient applicationInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getApplicationInternalRestClient();
    }
    
    @Bean
    public ExternalParametersInternalRestClient externalParametersInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getExternalParametersInternalRestClient();
    }

}
