/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.config;

import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.pastis.client.PastisRestClientFactory;
import fr.gouv.vitamui.pastis.client.PastisTransformationRestClient;
import fr.gouv.vitamui.pastis.client.PastisTransformationWebClient;
import fr.gouv.vitamui.pastis.client.PastisWebClientFactory;
import fr.gouv.vitamui.referential.external.client.ArchivalProfileUnitExternalRestClient;
import fr.gouv.vitamui.referential.external.client.ArchivalProfileUnitExternalWebClient;
import fr.gouv.vitamui.referential.external.client.ProfileExternalRestClient;
import fr.gouv.vitamui.referential.external.client.ProfileExternalWebClient;
import fr.gouv.vitamui.referential.external.client.ReferentialExternalRestClientFactory;
import fr.gouv.vitamui.referential.external.client.ReferentialExternalWebClientFactory;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Arrays;

@Configuration
@Import(value = { SecurityConfig.class, SwaggerConfiguration.class, RestExceptionHandler.class })
public class PastisContextConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public PastisRestClientFactory pastisRestClientFactory(
        final PastisApplicationProperties uiProperties,
        RestTemplateBuilder restTemplateBuilder
    ) {
        return new PastisRestClientFactory(uiProperties.getPastisExternalClient(), restTemplateBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public PastisWebClientFactory iamWebClientFactory(final PastisApplicationProperties uiProperties) {
        return new PastisWebClientFactory(uiProperties.getPastisExternalClient());
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
    public ProfileExternalRestClient profileExternalRestClient(final ReferentialExternalRestClientFactory factory) {
        return factory.getProfileExternalRestClient();
    }

    @Bean
    public ArchivalProfileUnitExternalRestClient archivalProfileCrudRestClient(
        final ReferentialExternalRestClientFactory referentialExternalRestClientFactory
    ) {
        return referentialExternalRestClientFactory.getArchivalProfileExternalRestClient();
    }

    @Bean
    public ArchivalProfileUnitExternalWebClient archivalProfileWebRestClient(
        final ReferentialExternalWebClientFactory referentialExternalWebClientFactory
    ) {
        return referentialExternalWebClientFactory.getArchivalProfileExternalWebClient();
    }

    @Bean
    public ProfileExternalWebClient profileExternalWebClient(
        final ReferentialExternalWebClientFactory referentialExternalWebClientFactory
    ) {
        return referentialExternalWebClientFactory.getProfileExternalWebClient();
    }

    @Bean
    public PastisTransformationRestClient pastisTransformationRestClient(
        final PastisRestClientFactory pastisRestClientFactory
    ) {
        return pastisRestClientFactory.getPastisTransformationRestClient();
    }

    @Bean
    public PastisTransformationWebClient pastisTransformationWebClient(
        final PastisWebClientFactory pastisWebClientFactory
    ) {
        return pastisWebClientFactory.getPastisTransformationWebClient();
    }
}
