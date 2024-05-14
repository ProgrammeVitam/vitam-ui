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

package fr.gouv.vitamui.pastis.server.config;

import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.pastis.common.service.JsonFromPUA;
import fr.gouv.vitamui.pastis.common.service.PuaFromJSON;
import fr.gouv.vitamui.pastis.common.service.PuaPastisValidator;
import fr.gouv.vitamui.referential.internal.client.ProfileInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.ReferentialInternalRestClientFactory;
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
@Import({ RestExceptionHandler.class, SwaggerConfiguration.class, HttpMessageConvertersAutoConfiguration.class })
public class ApiPastisServerConfig extends AbstractContextConfiguration {

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(
        final ApiPastisApplicationProperties apiArchiveExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new SecurityRestClientFactory(
            apiArchiveExternalApplicationProperties.getSecurityClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
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
        final UserInternalRestClient userInternalRestClient
    ) {
        return new ExternalAuthentificationService(contextRestClient, userInternalRestClient);
    }

    @Bean
    public ReferentialInternalRestClientFactory referentialInternalRestClientFactory(
        final ApiPastisApplicationProperties apiArchiveExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new ReferentialInternalRestClientFactory(
            apiArchiveExternalApplicationProperties.getReferentialInternalClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public ProfileInternalRestClient profileInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getProfileInternalRestClient();
    }

    @Bean
    public IamInternalRestClientFactory iamInternalRestClientFactory(
        final ApiPastisApplicationProperties apiArchiveExternalApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new IamInternalRestClientFactory(
            apiArchiveExternalApplicationProperties.getIamInternalClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public UserInternalRestClient userInternalRestClient(
        final IamInternalRestClientFactory iamInternalRestClientFactory
    ) {
        return iamInternalRestClientFactory.getUserInternalRestClient();
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
    public JsonFromPUA jsonFromPUA() {
        return new JsonFromPUA();
    }

    @Bean
    public PuaFromJSON puaFromJSON() {
        return new PuaFromJSON(puaPastisValidator());
    }

    @Bean
    public PuaPastisValidator puaPastisValidator() {
        return new PuaPastisValidator();
    }
}
