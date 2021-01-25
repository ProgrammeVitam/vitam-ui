/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.external.server.config;

import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.IamInternalWebClientFactory;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.internal.client.*;
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
@Import({RestExceptionHandler.class, SwaggerConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
public class ApiReferentialServerConfig extends AbstractContextConfiguration {

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
    public SecurityRestClientFactory securityRestClientFactory(final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
                                                               final RestTemplateBuilder restTemplateBuilder) {
        return new SecurityRestClientFactory(apiReferentialApplicationProperties.getSecurityClient(), restTemplateBuilder);
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
    public IamInternalRestClientFactory iamInternalRestClientFactory(final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
                                                                     final RestTemplateBuilder restTemplateBuilder) {
        return new IamInternalRestClientFactory(apiReferentialApplicationProperties.getIamInternalClient(), restTemplateBuilder);

    }

    @Bean
    public IamInternalWebClientFactory internalWebClientFactory(final ApiReferentialApplicationProperties apiReferentialApplicationProperties) {
        return new IamInternalWebClientFactory(apiReferentialApplicationProperties.getIamInternalClient());

    }

    @Bean
    public UserInternalRestClient userInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getUserInternalRestClient();
    }

    @Bean
    public ReferentialInternalRestClientFactory referentialInternalRestClientFactory(final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
                                                                                     final RestTemplateBuilder restTemplateBuilder) {
        return new ReferentialInternalRestClientFactory(apiReferentialApplicationProperties.getReferentialInternalClient(), restTemplateBuilder);
    }
    
    @Bean
    public ReferentialInternalWebClientFactory referentialInternalWebClientFactory(final ApiReferentialApplicationProperties apiReferentialApplicationProperties) {
    	return new ReferentialInternalWebClientFactory(apiReferentialApplicationProperties.getReferentialInternalClient());
    }

    @Bean
    public AccessContractInternalRestClient accessContractInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getAccessContractInternalRestClient();
    }
    
    @Bean
    public IngestContractInternalRestClient ingestContractInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getIngestContractInternalRestClient();
    }

    @Bean
    public AgencyInternalRestClient agencyInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getAgencyInternalRestClient();
    }

    @Bean
    public FileFormatInternalRestClient fileFormatInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getFileFormatInternalRestClient();
    }

    @Bean
    public ContextInternalRestClient contextInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getContextInternalRestClient();
    }

    @Bean
    public SecurityProfileInternalRestClient securityProfileInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getSecurityProfileInternalRestClient();
    }

    @Bean
    public OntologyInternalRestClient ontologyInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getOntologyInternalRestClient();
    }

    @Bean
    public OperationInternalRestClient operationInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getOperationInternalRestClient();
    }

    @Bean
    public AccessionRegisterInternalRestClient accessionRegisterInternalRestClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getAccessionRegisterInternalRestClient();
    }

    @Bean
    public UnitInternalRestClient unitInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getUnitInternalRestClient();
    }

    @Bean
    public ManagementContractInternalRestClient managementContractInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getManagementContractInternalRestClient();
    }

    @Bean
    public ProfileInternalRestClient profileInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getProfileInternalRestClient();
    }

    @Bean
    public RuleInternalRestClient ruleInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getRuleInternalRestClient();
    }
    
    @Bean
    public AgencyInternalWebClient agencyInternalWebClient(final ReferentialInternalWebClientFactory referentialInternalWebClientFactory) {
    	return referentialInternalWebClientFactory.getAgencyInternalWebClient();
    }
    
    @Bean
    public FileFormatInternalWebClient fileFormatInternalWebClient(final ReferentialInternalWebClientFactory referentialInternalWebClientFactory) {
    	return referentialInternalWebClientFactory.getFileFormatInternalWebClient();
    }
    
    @Bean
    public OntologyInternalWebClient ontologyInternalWebClient(final ReferentialInternalWebClientFactory referentialInternalWebClientFactory) {
    	return referentialInternalWebClientFactory.getOntologyInternalWebClient();
    }
}
