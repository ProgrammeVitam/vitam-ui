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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.config.Jackson2CompatibilityConfig;
import fr.gouv.vitamui.commons.rest.config.Jackson2ObjectMapperFactory;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamProfileCommonService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.iam.openapiclient.CustomersApi;
import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import fr.gouv.vitamui.iam.openapiclient.IamApiClientsFactoryVitamui;
import fr.gouv.vitamui.iam.openapiclient.UsersApi;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.InternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.IamClientUserAuthenticationService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import fr.gouv.vitamui.pastis.common.service.JsonFromPUA;
import fr.gouv.vitamui.pastis.common.service.PuaFromJSON;
import fr.gouv.vitamui.pastis.common.service.PuaPastisValidator;
import fr.gouv.vitamui.pastis.server.security.WebSecurityConfig;
import fr.gouv.vitamui.security.openapiclient.ContextsApi;
import fr.gouv.vitamui.security.openapiclient.SecurityApiClientsFactoryVitamui;
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
        Jackson2CompatibilityConfig.class,
    }
)
public class ApiPastisServerConfig extends AbstractContextConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperFactory.create();
    }

    @Bean
    public SecurityApiClientsFactoryVitamui securityApiClientsFactory(
        final ApiPastisApplicationProperties apiPastisApplicationProperties,
        final RestClient.Builder restClientBuilder
    ) {
        return new SecurityApiClientsFactoryVitamui(
            apiPastisApplicationProperties.getSecurityClient(),
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
    public SecurityService externalSecurityService() {
        return new SecurityService();
    }

    @Bean
    public IamApiClientsFactoryVitamui iamApiClientsFactory(
        final ApiPastisApplicationProperties apiPastisApplicationProperties,
        final RestClient.Builder restClientBuilder
    ) {
        return new IamApiClientsFactoryVitamui(apiPastisApplicationProperties.getIamClient(), restClientBuilder);
    }

    @Bean
    public UsersApi usersApi(final IamApiClientsFactoryVitamui iamApiClientsFactory) {
        return iamApiClientsFactory.getUsersApi();
    }

    @Bean
    public CustomersApi customersApi(final IamApiClientsFactoryVitamui iamApiClientsFactory) {
        return iamApiClientsFactory.getCustomersApi();
    }

    @Bean
    public ExternalParametersApi externalParametersApi(final IamApiClientsFactoryVitamui iamApiClientsFactory) {
        return iamApiClientsFactory.getExternalParametersApi();
    }

    @Bean
    public ConfigurationService configurationService(
        final AdminExternalClient adminExternalClient,
        final ObjectMapper objectMapper
    ) {
        return new ConfigurationService(adminExternalClient, objectMapper);
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

    @Bean
    public VitamProfileCommonService getVitamProfileService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper
    ) {
        return new VitamProfileCommonService(adminClient, objectMapper);
    }
}
