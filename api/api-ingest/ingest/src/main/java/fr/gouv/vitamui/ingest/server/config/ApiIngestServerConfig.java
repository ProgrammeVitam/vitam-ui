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

package fr.gouv.vitamui.ingest.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.v2.AccessExternalClientV2;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.config.Jackson2CompatibilityConfig;
import fr.gouv.vitamui.commons.rest.config.Jackson2ObjectMapperFactory;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.access.TransferAcknowledgmentService;
import fr.gouv.vitamui.commons.vitam.api.access.TransferRequestService;
import fr.gouv.vitamui.commons.vitam.api.access.UnitCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamIngestConfig;
import fr.gouv.vitamui.iam.openapiclient.CustomersApi;
import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import fr.gouv.vitamui.iam.openapiclient.IamApiClientsFactory;
import fr.gouv.vitamui.iam.openapiclient.UsersApi;
import fr.gouv.vitamui.iam.security.config.ExternalParametersCommonConfig;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.InternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalParametersService;
import fr.gouv.vitamui.iam.security.service.IamClientUserAuthenticationService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import fr.gouv.vitamui.ingest.server.security.WebSecurityConfig;
import fr.gouv.vitamui.ingest.server.service.IngestAccessContractService;
import fr.gouv.vitamui.ingest.server.service.IngestGeneratorODTFile;
import fr.gouv.vitamui.ingest.server.service.IngestService;
import fr.gouv.vitamui.security.openapiclient.ContextsApi;
import fr.gouv.vitamui.security.openapiclient.SecurityApiClientsFactory;
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
        VitamIngestConfig.class,
        VitamAdministrationConfig.class,
        Jackson2CompatibilityConfig.class,
        ExternalParametersCommonConfig.class,
    }
)
public class ApiIngestServerConfig extends AbstractContextConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperFactory.create();
    }

    @Bean
    public SecurityApiClientsFactory securityApiClientsFactory(
        final ApiIngestApplicationProperties apiIngestApplicationProperties,
        final RestClient.Builder restClientBuilder
    ) {
        return new SecurityApiClientsFactory(apiIngestApplicationProperties.getSecurityClient(), restClientBuilder);
    }

    @Bean
    public ContextsApi contextsApi(final SecurityApiClientsFactory securityApiClientsFactory) {
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
    public IngestService ingestService(
        final SecurityService securityService,
        final LogbookService logbookService,
        final ObjectMapper objectMapper,
        final IngestExternalClient ingestExternalClient,
        final CustomersApi customersApi,
        final IngestGeneratorODTFile ingestGeneratorODTFile,
        final ExternalParametersService externalParametersService,
        final IngestAccessContractService ingestAccessContractService,
        final UsersApi usersApi
    ) {
        return new IngestService(
            securityService,
            logbookService,
            objectMapper,
            ingestExternalClient,
            customersApi,
            usersApi,
            ingestGeneratorODTFile,
            externalParametersService,
            ingestAccessContractService
        );
    }

    @Bean
    public SecurityService securityService() {
        return new SecurityService();
    }

    @Bean
    public IamApiClientsFactory iamApiClientsFactory(
        final ApiIngestApplicationProperties ingestApplicationProperties,
        final RestClient.Builder restClientBuilder
    ) {
        return new IamApiClientsFactory(ingestApplicationProperties.getIamClient(), restClientBuilder);
    }

    @Bean
    public UsersApi usersApi(final IamApiClientsFactory iamApiClientsFactory) {
        return iamApiClientsFactory.getUsersApi();
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
    public CustomersApi customersApi(final IamApiClientsFactory iamApiClientsFactory) {
        return iamApiClientsFactory.getCustomersApi();
    }

    @Bean
    public IngestGeneratorODTFile ingestGeneratorODTFile() {
        return new IngestGeneratorODTFile();
    }

    @Bean
    public ExternalParametersApi externalParametersApi(final IamApiClientsFactory iamApiClientsFactory) {
        return iamApiClientsFactory.getExternalParametersApi();
    }

    @Bean
    public ConfigurationService configurationService(
        final AdminExternalClient adminExternalClient,
        final ObjectMapper objectMapper
    ) {
        return new ConfigurationService(adminExternalClient, objectMapper);
    }
}
