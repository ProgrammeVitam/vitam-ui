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

package fr.gouv.vitamui.collect.config;

import fr.gouv.vitamui.collect.external.client.CollectExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectExternalRestClientFactory;
import fr.gouv.vitamui.collect.external.client.CollectExternalWebClient;
import fr.gouv.vitamui.collect.external.client.CollectExternalWebClientFactory;
import fr.gouv.vitamui.collect.external.client.CollectStreamingExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectStreamingExternalRestClientFactory;
import fr.gouv.vitamui.collect.external.client.CollectTransactionExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectTransactionExternalRestClientFactory;
import fr.gouv.vitamui.collect.external.client.GetorixDepositExternalRestClient;
import fr.gouv.vitamui.collect.external.client.GetorixDepositExternalRestClientFactory;
import fr.gouv.vitamui.collect.external.client.SearchCriteriaHistoryExternalRestClient;
import fr.gouv.vitamui.collect.external.client.SearchCriteriaHistoryExternalRestClientCollectFactory;
import fr.gouv.vitamui.collect.external.client.UpdateUnitsMetadataExternalRestClient;
import fr.gouv.vitamui.collect.external.client.UpdateUnitsMetadataExternalRestClientFactory;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties
@Import(value = {SecurityConfig.class, SwaggerConfiguration.class, RestExceptionHandler.class})
public class CollectContextConfiguration extends AbstractContextConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IamExternalWebClientFactory iamWebClientFactory(final UIProperties uiProperties) {
        return new IamExternalWebClientFactory(uiProperties.getIamExternalClient());
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public CollectExternalRestClientFactory collectExternalRestClientFactory(
        final CollectApplicationProperties uiProperties, RestTemplateBuilder restTemplateBuilder) {
        return new CollectExternalRestClientFactory(uiProperties.getCollectExternalClient(), restTemplateBuilder);
    }

    @Bean
    public CollectExternalRestClient collectExternalRestClient(
        final CollectExternalRestClientFactory collectExternalRestClientFactory) {
        return collectExternalRestClientFactory.getCollectExternalRestClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public CollectTransactionExternalRestClientFactory collectTransactionExternalRestClientFactory(
        final CollectApplicationProperties uiProperties, RestTemplateBuilder restTemplateBuilder) {
        return new CollectTransactionExternalRestClientFactory(uiProperties.getCollectExternalClient(),
            restTemplateBuilder);
    }

    @Bean
    public CollectTransactionExternalRestClient collectTransactionExternalRestClient(
        final CollectTransactionExternalRestClientFactory collectTransactionExternalRestClientFactory) {
        return collectTransactionExternalRestClientFactory.getCollectExternalRestClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public CollectStreamingExternalRestClientFactory collectStreamingExternalRestClientFactory(
        final CollectApplicationProperties uiProperties) {
        return new CollectStreamingExternalRestClientFactory(uiProperties.getCollectExternalClient());
    }

    @Bean
    public CollectStreamingExternalRestClient collectStreamingExternalRestClient(
        final CollectStreamingExternalRestClientFactory collectStreamingExternalRestClientFactory) {
        return collectStreamingExternalRestClientFactory.getCollectStreamingExternalRestClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public CollectExternalWebClientFactory collectExternalWebClientFactory(
        final CollectApplicationProperties uiProperties, final WebClient.Builder webClientBuilder) {
        return new CollectExternalWebClientFactory(uiProperties.getCollectExternalClient(),
            webClientBuilder);
    }

    @Bean
    public CollectExternalWebClient collectExternalWebClient(
        final CollectExternalWebClientFactory collectExternalWebClientFactory) {
        return collectExternalWebClientFactory.getCollectExternalWebClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public SearchCriteriaHistoryExternalRestClientCollectFactory searchCriteriaHistoryExternalRestClientFactory(
        final CollectApplicationProperties uiProperties, RestTemplateBuilder restTemplateBuilder) {
        return new SearchCriteriaHistoryExternalRestClientCollectFactory(uiProperties.getCollectExternalClient(),
            restTemplateBuilder);
    }

    @Bean
    public SearchCriteriaHistoryExternalRestClient searchCriteriaHistoryExternalRestClient(
        SearchCriteriaHistoryExternalRestClientCollectFactory factory) {
        return factory.getSearchCriteriaHistoryExternalRestClient();
    }


    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public UpdateUnitsMetadataExternalRestClientFactory updateUnitsMetadataExternalRestClientFactory(
        final CollectApplicationProperties uiProperties, RestTemplateBuilder restTemplateBuilder) {
        return new UpdateUnitsMetadataExternalRestClientFactory(uiProperties.getUpdateUnitsMetadataExternalClient(), restTemplateBuilder);
    }

    @Bean
    public UpdateUnitsMetadataExternalRestClient updateUnitsMetadataExternalRestClient(
        final UpdateUnitsMetadataExternalRestClientFactory updateUnitsMetadataExternalRestClientFactory) {
        return updateUnitsMetadataExternalRestClientFactory.getUpdateUnitsMetadataExternalRestClient();
    }
    /*
     * Getorix
     */

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public GetorixDepositExternalRestClientFactory getorixDepositExternalRestClientFactory(
        final CollectApplicationProperties uiProperties, RestTemplateBuilder restTemplateBuilder) {
        return new GetorixDepositExternalRestClientFactory(uiProperties.getCollectExternalClient(),
            restTemplateBuilder);
    }

    @Bean
    public GetorixDepositExternalRestClient getorixDepositExternalRestClient(
        GetorixDepositExternalRestClientFactory factory) {
        return factory.getGetorixDepositExternalRestClient();
    }
}
