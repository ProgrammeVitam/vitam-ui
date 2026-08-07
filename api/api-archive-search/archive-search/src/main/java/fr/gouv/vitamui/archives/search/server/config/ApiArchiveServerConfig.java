/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
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

package fr.gouv.vitamui.archives.search.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.v2.AccessExternalClientV2;
import fr.gouv.vitamui.archives.search.common.dto.converter.UpdateArchiveUnitDtoToUpdateMultiQueryConverter;
import fr.gouv.vitamui.archives.search.common.service.ArchiveUnitService;
import fr.gouv.vitamui.archives.search.server.converter.RuleOperationsConverter;
import fr.gouv.vitamui.archives.search.server.searchcriteria.converter.SearchCriteriaHistoryConverter;
import fr.gouv.vitamui.archives.search.server.searchcriteria.dao.SearchCriteriaHistoryRepository;
import fr.gouv.vitamui.archives.search.server.searchcriteria.service.SearchCriteriaHistoryService;
import fr.gouv.vitamui.archives.search.server.security.WebSecurityConfig;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchUnitServiceImpl;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.api.converter.JsonPatchDtoToUpdateMultiQueryConverter;
import fr.gouv.vitamui.commons.api.converter.UpdateMultiQueriesToBulkCommandDto;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.config.Jackson2CompatibilityConfig;
import fr.gouv.vitamui.commons.rest.config.Jackson2ObjectMapperFactory;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import fr.gouv.vitamui.commons.vitam.api.access.TransferAcknowledgmentService;
import fr.gouv.vitamui.commons.vitam.api.access.TransferRequestService;
import fr.gouv.vitamui.commons.vitam.api.access.UnitCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import fr.gouv.vitamui.iam.openapiclient.IamApiClientsFactoryVitamui;
import fr.gouv.vitamui.iam.openapiclient.UsersApi;
import fr.gouv.vitamui.iam.security.config.ExternalParametersCommonConfig;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.InternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalParametersService;
import fr.gouv.vitamui.iam.security.service.IamClientUserAuthenticationService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import fr.gouv.vitamui.security.openapiclient.ContextsApi;
import fr.gouv.vitamui.security.openapiclient.SecurityApiClientsFactoryVitamui;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

@Configuration
@Import(
    {
        RestExceptionHandler.class,
        HttpMessageConvertersAutoConfiguration.class,
        WebSecurityConfig.class,
        VitamAccessConfig.class,
        VitamAdministrationConfig.class,
        MongoDbConfig.class,
        ConverterConfig.class,
        Jackson2CompatibilityConfig.class,
        ExternalParametersCommonConfig.class,
    }
)
public class ApiArchiveServerConfig extends AbstractContextConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperFactory.create();
    }

    @Bean
    public SequenceGeneratorService sequenceGeneratorService(final CustomSequenceRepository sequenceRepository) {
        return new SequenceGeneratorService(sequenceRepository);
    }

    @Bean
    public SecurityApiClientsFactoryVitamui securityApiClientsFactory(
        final ApiArchiveApplicationProperties apiArchiveApplicationProperties,
        final RestClient.Builder restClientBuilder
    ) {
        return new SecurityApiClientsFactoryVitamui(
            apiArchiveApplicationProperties.getSecurityClient(),
            restClientBuilder
        );
    }

    @Bean
    public ContextsApi contextsApi(final SecurityApiClientsFactoryVitamui securityApiClientsFactory) {
        return securityApiClientsFactory.getContextsApi();
    }

    @Bean
    public UserAuthenticationService authentificationService(final UsersApi usersApi) {
        return new IamClientUserAuthenticationService(usersApi);
    }

    @Bean
    public ApiAuthenticationProvider apiAuthenticationProvider(
        UserAuthenticationService userAuthenticationService,
        ContextsApi contextsApi
    ) {
        return new ApiAuthenticationProvider(
            new InternalApiAuthenticationProvider(userAuthenticationService),
            new ExternalApiAuthenticationProvider(contextsApi, userAuthenticationService)
        );
    }

    @Bean
    public SecurityService securityService() {
        return new SecurityService();
    }

    @Bean
    public SignedDownloadTokenService signedDownloadTokenService(
        ObjectMapper objectMapper,
        SecurityService securityService,
        ExternalParametersService externalParametersService,
        @Value("${download.signed-url.secret}") String secret,
        @Value("${download.signed-url.ttl-seconds:60}") long ttlSeconds,
        @Value("${download.signed-url.clock-skew-seconds:5}") long clockSkewSeconds
    ) {
        return new SignedDownloadTokenService(objectMapper, secret, ttlSeconds, clockSkewSeconds, claims -> {
            if (claims.getSubject() == null) {
                claims.setSubject(securityService.getUser().getId());
            }
            if (claims.getTenantId() == null) {
                claims.setTenantId(securityService.getTenantIdentifier());
            }
            if (claims.getAccessContractId() == null) {
                claims.setAccessContractId(externalParametersService.retrieveAccessContractFromExternalParam());
            }
            if (claims.getApplicationSessionId() == null) {
                claims.setApplicationSessionId(securityService.getApplicationId());
            }
        });
    }

    @Bean
    public IamApiClientsFactoryVitamui iamApiClientsFactory(
        final ApiArchiveApplicationProperties apiArchiveApplicationProperties,
        final RestClient.Builder restClientBuilder
    ) {
        return new IamApiClientsFactoryVitamui(apiArchiveApplicationProperties.getIamClient(), restClientBuilder);
    }

    @Bean
    public UsersApi usersApi(final IamApiClientsFactoryVitamui iamApiClientsFactory) {
        return iamApiClientsFactory.getUsersApi();
    }

    @Bean
    public ExternalParametersApi externalParametersApi(final IamApiClientsFactoryVitamui iamApiClientsFactory) {
        return iamApiClientsFactory.getExternalParametersApi();
    }

    @Bean
    public UnitCommonService unitCommonService(final AccessExternalClient client) {
        return new UnitCommonService(client);
    }

    @Bean
    public ExportDipV2Service exportDipV2Service(final AccessExternalClientV2 accessExternalClientV2) {
        return new ExportDipV2Service(accessExternalClientV2);
    }

    @Bean
    public TransferRequestService transferRequestService(final AccessExternalClient accessExternalClient) {
        return new TransferRequestService(accessExternalClient);
    }

    @Bean
    public TransferAcknowledgmentService transferAcknowledgmentService(
        final AccessExternalClient accessExternalClient
    ) {
        return new TransferAcknowledgmentService(accessExternalClient);
    }

    @Bean
    public SearchCriteriaHistoryService searchCriteriaHistoryService(
        final SequenceGeneratorService sequenceGeneratorService,
        final SearchCriteriaHistoryRepository searchCriteriaHistoryRepository,
        final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter,
        final SecurityService securityService
    ) {
        return new SearchCriteriaHistoryService(
            sequenceGeneratorService,
            searchCriteriaHistoryRepository,
            searchCriteriaHistoryConverter,
            securityService
        );
    }

    @Bean
    public RuleOperationsConverter ruleOperationsConverter() {
        return new RuleOperationsConverter();
    }

    @Bean
    public ArchiveUnitService archiveUnitService(
        final AccessExternalClient accessExternalClient,
        final UpdateArchiveUnitDtoToUpdateMultiQueryConverter updateArchiveUnitDtoToUpdateMultiQueryConverter,
        final ExternalParametersService externalParametersService,
        final JsonPatchDtoToUpdateMultiQueryConverter jsonPatchDtoToUpdateMultiQueryConverter,
        final UpdateMultiQueriesToBulkCommandDto updateMultiQueriesToBulkCommandDto
    ) {
        return new ArchiveSearchUnitServiceImpl(
            accessExternalClient,
            updateArchiveUnitDtoToUpdateMultiQueryConverter,
            externalParametersService,
            jsonPatchDtoToUpdateMultiQueryConverter,
            updateMultiQueriesToBulkCommandDto
        );
    }

    @Bean
    public ConfigurationService configurationService(
        final AdminExternalClient adminExternalClient,
        final ObjectMapper objectMapper
    ) {
        return new ConfigurationService(adminExternalClient, objectMapper);
    }
}
