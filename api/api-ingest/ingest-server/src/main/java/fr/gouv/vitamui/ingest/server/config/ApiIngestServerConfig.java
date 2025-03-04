/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.ingest.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamIngestConfig;
import fr.gouv.vitamui.iam.external.client.CustomerExternalRestClient;
import fr.gouv.vitamui.iam.external.client.ExternalParametersExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.UserExternalRestClient;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.ingest.server.security.WebSecurityConfig;
import fr.gouv.vitamui.ingest.server.service.IngestAccessContractService;
import fr.gouv.vitamui.ingest.server.service.IngestExternalParametersService;
import fr.gouv.vitamui.ingest.server.service.IngestExternalService;
import fr.gouv.vitamui.ingest.server.service.IngestGeneratorODTFile;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Arrays;

@Configuration
@Import(
    {
        RestExceptionHandler.class,
        SwaggerConfiguration.class,
        WebSecurityConfig.class,
        VitamAccessConfig.class,
        VitamIngestConfig.class,
        VitamAdministrationConfig.class,
        HttpMessageConvertersAutoConfiguration.class,
    }
)
public class ApiIngestServerConfig extends AbstractContextConfiguration {

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(
        final ApiIngestApplicationProperties apiIngestApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new SecurityRestClientFactory(apiIngestApplicationProperties.getSecurityClient(), restTemplateBuilder);
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
    }

    @Bean
    public MappingJackson2HttpMessageConverter customizedJacksonMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(
            Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                MediaType.APPLICATION_OCTET_STREAM
            )
        );
        return converter;
    }

    @Bean
    public ExternalApiAuthenticationProvider apiAuthenticationProvider(
        final ExternalAuthentificationService externalAuthentificationService
    ) {
        return new ExternalApiAuthenticationProvider(externalAuthentificationService);
    }

    @Bean
    public ExternalSecurityService externalSecurityService() {
        return new ExternalSecurityService();
    }

    @Bean
    public ExternalAuthentificationService externalAuthentificationService(
        final ContextRestClient contextRestClient,
        final UserExternalRestClient userExternalRestClient
    ) {
        return new ExternalAuthentificationService(contextRestClient, userExternalRestClient);
    }

    @Bean
    public IamExternalRestClientFactory iamExternalRestClientFactory(
        final ApiIngestApplicationProperties apiIngestApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new IamExternalRestClientFactory(
            apiIngestApplicationProperties.getIamExternalClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public UserExternalRestClient userInternalRestClient(
        final IamExternalRestClientFactory iamExternalRestClientFactory
    ) {
        return iamExternalRestClientFactory.getUserExternalRestClient();
    }

    @Bean
    public CustomerExternalRestClient customerExternalRestClient(
        final IamExternalRestClientFactory iamExternalRestClientFactory
    ) {
        return iamExternalRestClientFactory.getCustomerExternalRestClient();
    }

    @Bean
    public IngestGeneratorODTFile ingestGeneratorODTFile() {
        return new IngestGeneratorODTFile();
    }

    @Bean
    public ExternalParametersExternalRestClient externalParametersExternalRestClient(
        final IamExternalRestClientFactory iamExternalRestClientFactory
    ) {
        return iamExternalRestClientFactory.getExternalParametersExternalRestClient();
    }

    @Bean
    public IngestExternalService ingestInternalService(
        final ExternalSecurityService externalSecurityService,
        final LogbookService logbookService,
        final ObjectMapper objectMapper,
        final IngestExternalClient ingestExternalClient,
        final CustomerExternalRestClient customerExternalRestClient,
        final IngestGeneratorODTFile ingestGeneratorODTFile,
        final IngestExternalParametersService ingestExternalParametersService,
        final IngestAccessContractService ingestAccessContractService,
        final UserExternalRestClient userExternalRestClient
    ) {
        return new IngestExternalService(
            externalSecurityService,
            logbookService,
            objectMapper,
            ingestExternalClient,
            customerExternalRestClient,
            userExternalRestClient,
            ingestGeneratorODTFile,
            ingestExternalParametersService,
            ingestAccessContractService
        );
    }
}
