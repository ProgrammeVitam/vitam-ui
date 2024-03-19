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
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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

package fr.gouv.vitamui.collect.external.server.config;

import fr.gouv.vitamui.collect.common.rest.ArchiveUnitClient;
import fr.gouv.vitamui.collect.internal.client.CollectInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.CollectInternalRestClientFactory;
import fr.gouv.vitamui.collect.internal.client.CollectInternalWebClient;
import fr.gouv.vitamui.collect.internal.client.CollectInternalWebClientFactory;
import fr.gouv.vitamui.collect.internal.client.CollectStreamingInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.CollectStreamingInternalRestClientFactory;
import fr.gouv.vitamui.collect.internal.client.CollectTransactionInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.SearchCriteriaHistoryInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.UpdateUnitsMetadataInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.UpdateUnitsMetadataInternalRestClientFactory;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Import({RestExceptionHandler.class, SwaggerConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
public class ApiCollectExternalServerConfig extends AbstractContextConfiguration {

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(
        final ApiCollectExternalApplicationProperties apiCollectExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new SecurityRestClientFactory(apiCollectExternalApplicationProperties.getSecurityClient(),
            restTemplateBuilder);
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
    }

    @Bean
    public ExternalApiAuthenticationProvider apiAuthenticationProvider(
        final ExternalAuthentificationService externalAuthentificationService) {
        return new ExternalApiAuthenticationProvider(externalAuthentificationService);
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
    public IamInternalRestClientFactory iamInternalRestClientFactory(
        final ApiCollectExternalApplicationProperties apiCollectExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new IamInternalRestClientFactory(apiCollectExternalApplicationProperties.getIamInternalClient(),
            restTemplateBuilder);
    }

    @Bean
    public UserInternalRestClient userInternalRestClient(
        final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getUserInternalRestClient();
    }

    @Bean
    public CollectInternalRestClientFactory collectInternalRestClientFactory(
        final ApiCollectExternalApplicationProperties apiCollectExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new CollectInternalRestClientFactory(apiCollectExternalApplicationProperties.getCollectInternalClient(),
            restTemplateBuilder);
    }

    @Bean
    public CollectInternalRestClient collectInternalRestClient(final CollectInternalRestClientFactory factory) {
        return factory.getCollectInternalRestClient();
    }

    @Bean
    public CollectTransactionInternalRestClient collectTransactionInternalRestClient(final CollectInternalRestClientFactory factory) {
        return factory.getCollectTransactionInternalRestClient();
    }



    @Bean
    public CollectStreamingInternalRestClientFactory collectStreamingInternalRestClientFactory(
        final ApiCollectExternalApplicationProperties apiCollectExternalApplicationProperties) {
        return new CollectStreamingInternalRestClientFactory(
            apiCollectExternalApplicationProperties.getCollectInternalClient());
    }

    @Bean
    public CollectStreamingInternalRestClient collectStreamingInternalRestClient(
        final CollectStreamingInternalRestClientFactory factory) {
        return factory.getCollectStreamingInternalRestClient();
    }

    @Bean
    public CollectInternalWebClientFactory collectInternalWebClientFactory(
        final ApiCollectExternalApplicationProperties apiCollectExternalApplicationProperties,
        final WebClient.Builder webClientBuilder) {
        return new CollectInternalWebClientFactory(
            apiCollectExternalApplicationProperties.getCollectInternalClient(), webClientBuilder);
    }


    @Bean
    public CollectInternalWebClient collectInternalWebClient(
        final CollectInternalWebClientFactory collectInternalWebClientFactory) {
        return collectInternalWebClientFactory.getCollectInternalWebClient();
    }

    @Bean
    public SearchCriteriaHistoryInternalRestClient searchCriteriaHistoryInternalRestClient(
        final CollectInternalRestClientFactory collectInternalRestClientFactory) {
        return collectInternalRestClientFactory.getSearchCriteriaHistoryInternalRestClient();
    }


    @Bean
    public UpdateUnitsMetadataInternalRestClientFactory updateUnitsMetadataInternalRestClientFactory(
        final ApiCollectExternalApplicationProperties apiCollectExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new UpdateUnitsMetadataInternalRestClientFactory(apiCollectExternalApplicationProperties.getCollectInternalClient(),
            restTemplateBuilder);
    }

    @Bean
    public UpdateUnitsMetadataInternalRestClient updateUnitsMetadataInternalRestClient(
        final UpdateUnitsMetadataInternalRestClientFactory factory) {
        return factory.getUpdateUnitsMetadataInternalRestClient();
    }

    @Bean
    public ArchiveUnitClient getArchiveUnitClient(
        final CollectInternalRestClientFactory collectInternalRestClientFactory) {
        return collectInternalRestClientFactory.getArchiveUnitClient();
    }
}
