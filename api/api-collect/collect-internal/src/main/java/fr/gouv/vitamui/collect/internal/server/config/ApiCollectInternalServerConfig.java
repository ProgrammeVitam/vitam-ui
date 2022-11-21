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

package fr.gouv.vitamui.collect.internal.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.collect.internal.server.dao.SearchCriteriaHistoryRepository;
import fr.gouv.vitamui.collect.internal.server.security.WebSecurityConfig;
import fr.gouv.vitamui.collect.internal.server.service.TransactionArchiveUnitInternalService;
import fr.gouv.vitamui.collect.internal.server.service.ProjectInternalService;
import fr.gouv.vitamui.collect.internal.server.service.ProjectObjectGroupInternalService;
import fr.gouv.vitamui.collect.internal.server.service.SearchCriteriaHistoryInternalService;
import fr.gouv.vitamui.collect.internal.server.service.converters.SearchCriteriaHistoryConverter;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.collect.internal.server.service.TransactionInternalService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamCollectConfig;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.InternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.InternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RestExceptionHandler.class, SwaggerConfiguration.class, WebSecurityConfig.class, VitamAccessConfig.class,
    VitamCollectConfig.class, VitamAdministrationConfig.class})
public class ApiCollectInternalServerConfig extends AbstractContextConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "clients.iam-internal")
    public RestClientConfiguration iamInternalRestClientConfiguration() {
        return new RestClientConfiguration();
    }

    @Bean
    public IamInternalRestClientFactory iamInternalRestClientFactory(
        final RestClientConfiguration iamInternalRestClientConfiguration,
        final RestTemplateBuilder restTemplateBuilder) {
        return new IamInternalRestClientFactory(iamInternalRestClientConfiguration, restTemplateBuilder);
    }

    @Bean
    public InternalApiAuthenticationProvider internalApiAuthenticationProvider(
        final InternalAuthentificationService internalAuthentificationService) {
        return new InternalApiAuthenticationProvider(internalAuthentificationService);
    }

    @Bean
    public UserInternalRestClient userInternalRestClient(
        final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getUserInternalRestClient();
    }

    @Bean
    public InternalAuthentificationService internalAuthentificationService(
        final UserInternalRestClient userInternalRestClient) {
        return new InternalAuthentificationService(userInternalRestClient);
    }

    @Bean
    public InternalSecurityService securityService() {
        return new InternalSecurityService();
    }

    @Bean
    public ProjectInternalService collectInternalService(final CollectService collectService,
        ObjectMapper objectMapper) {
        return new ProjectInternalService(collectService, objectMapper);
    }

    @Bean
    public TransactionInternalService transactionInternalService(final CollectService collectService,
        ObjectMapper objectMapper) {
        return new TransactionInternalService(collectService, objectMapper);
    }


    @Bean
    public TransactionArchiveUnitInternalService projectArchiveUnitInternalService(final CollectService collectService,
        AgencyService agencyService, ObjectMapper objectMapper) {
        return new TransactionArchiveUnitInternalService(collectService, agencyService, objectMapper);
    }

    @Bean
    public ProjectObjectGroupInternalService projectObjectGroupInternalService(final CollectService collectService,
        ObjectMapper objectMapper) {
        return new ProjectObjectGroupInternalService(collectService, objectMapper);
    }

    @Bean
    public SearchCriteriaHistoryInternalService searchCriteriaHistoryInternalService(
        final CustomSequenceRepository sequenceRepository,
        final SearchCriteriaHistoryRepository searchCriteriaHistoryRepository,
        final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter,
        final InternalSecurityService internalSecurityService) {
        return new SearchCriteriaHistoryInternalService(sequenceRepository, searchCriteriaHistoryRepository,
            searchCriteriaHistoryConverter, internalSecurityService);
    }

}
