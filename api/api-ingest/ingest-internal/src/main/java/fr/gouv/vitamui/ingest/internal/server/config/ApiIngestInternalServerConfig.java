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
package fr.gouv.vitamui.ingest.internal.server.config;

import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamIngestConfig;
import fr.gouv.vitamui.commons.vitam.api.ingest.IngestService;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.InternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.InternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.ingest.internal.server.security.WebSecurityConfig;
import fr.gouv.vitamui.ingest.internal.server.service.IngestInternalService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@Import({RestExceptionHandler.class, SwaggerConfiguration.class, WebSecurityConfig.class, VitamAccessConfig.class, VitamIngestConfig.class,
    VitamAdministrationConfig.class})
public class ApiIngestInternalServerConfig extends AbstractContextConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "clients.iam-internal")
    public RestClientConfiguration IamInternalRestClientConfiguration() {
        return new RestClientConfiguration();
    }

    @Bean
    public IamInternalRestClientFactory iamInternalRestClientFactory(
        final RestClientConfiguration IamInternalRestClientConfiguration,
        final RestTemplateBuilder restTemplateBuilder) {
        return new IamInternalRestClientFactory(IamInternalRestClientConfiguration, restTemplateBuilder);
    }

    @Bean
    public InternalApiAuthenticationProvider internalApiAuthenticationProvider(final InternalAuthentificationService internalAuthentificationService) {
        return new InternalApiAuthenticationProvider(internalAuthentificationService);
    }

    @Bean
    public UserInternalRestClient userInternalRestClient(final IamInternalRestClientFactory iamInternalRestClientFactory) {
        return iamInternalRestClientFactory.getUserInternalRestClient();
    }

    @Bean
    public InternalAuthentificationService internalAuthentificationService(final UserInternalRestClient userInternalRestClient) {
        return new InternalAuthentificationService(userInternalRestClient);
    }

    @Bean
    public InternalSecurityService securityService() {
        return new InternalSecurityService();
    }

    @Bean
    public IngestInternalService ingestInternalService(
            final InternalSecurityService internalSecurityService,
            LogbookService logbookService,
            ObjectMapper objectMapper,
            IngestService ingestService) {
        return new IngestInternalService(internalSecurityService, logbookService, objectMapper, ingestService);
    }
}
