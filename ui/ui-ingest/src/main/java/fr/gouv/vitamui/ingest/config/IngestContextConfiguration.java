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
package fr.gouv.vitamui.ingest.config;

import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.ingest.external.client.IngestExternalRestClient;
import fr.gouv.vitamui.ingest.external.client.IngestExternalRestClientFactory;
import fr.gouv.vitamui.ingest.external.client.IngestExternalWebClient;
import fr.gouv.vitamui.ingest.external.client.IngestExternalWebClientFactory;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties
@Import(value = {SecurityConfig.class, SwaggerConfiguration.class, RestExceptionHandler.class})
public class IngestContextConfiguration extends AbstractContextConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IamExternalWebClientFactory iamWebClientFactory(final UIProperties uiProperties) {
        return new IamExternalWebClientFactory(uiProperties.getIamExternalClient());
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IngestExternalRestClientFactory ingestExternalRestClientFactory(final IngestApplicationProperties uiProperties,
        RestTemplateBuilder restTemplateBuilder) {
        return new IngestExternalRestClientFactory(uiProperties.getIngestExternalClient(), restTemplateBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IngestExternalWebClientFactory ingestExternalWebClientFactory(final IngestApplicationProperties uiProperties,
        RestTemplateBuilder restTemplateBuilder) {
        return new IngestExternalWebClientFactory(uiProperties.getIngestExternalClient());
    }


    @Bean
    public IngestExternalRestClient ingestExternalRestClient(
        final IngestExternalRestClientFactory ingestExternalRestClientFactory) {
        return ingestExternalRestClientFactory.getIngestExternalRestClient();
    }

    @Bean
    public IngestExternalWebClient ingestExternalWebClient(
        final IngestExternalWebClientFactory ingestExternalWebClientFactory) {
        return ingestExternalWebClientFactory.getIngestExternalWebClient();
    }

}
