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

package fr.gouv.vitamui.iam.server.config;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.BaseVitamuiRestClientFactory;
import fr.gouv.vitamui.commons.rest.client.VitamuiRestClientFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.user.password.reset.ResetPasswordNotifier;
import fr.gouv.vitamui.iam.server.user.password.reset.ResetPasswordService;
import fr.gouv.vitamui.iam.server.user.password.reset.ResetPasswordValidationService;
import fr.gouv.vitamui.iam.server.user.password.reset.ValidationFailureHandler;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ResetPasswordConfiguration {

    @Bean
    @Qualifier("casRestClientFactory")
    public VitamuiRestClientFactory userEmailService(
        final RestClient.Builder restClientBuilder,
        final RestClientConfiguration casClientProperties
    ) {
        return new BaseVitamuiRestClientFactory(casClientProperties, restClientBuilder);
    }

    @Bean
    public ResetPasswordNotifier<UserDto> resetPasswordNotifier(
        @Qualifier("casRestClientFactory") VitamuiRestClientFactory restClientFactory,
        UserInfoService userInfoService,
        @Value("${cas.reset.password.url}") String url
    ) {
        return new ResetPasswordService(restClientFactory, userInfoService, url);
    }

    @Bean
    @Qualifier("strictResetPasswordNotifier")
    public ResetPasswordNotifier<UserDto> strictResetPasswordNotifier(
        ResetPasswordNotifier<UserDto> resetPasswordNotifier,
        IdentityProviderHelper helper,
        IdentityProviderService service,
        @Qualifier("strictFailureHandler") ValidationFailureHandler handler
    ) {
        return new ResetPasswordValidationService(resetPasswordNotifier, helper, service, handler);
    }

    @Bean
    @Qualifier("laxResetPasswordNotifier")
    public ResetPasswordNotifier<UserDto> laxResetPasswordNotifier(
        ResetPasswordNotifier<UserDto> resetPasswordNotifier,
        IdentityProviderHelper helper,
        IdentityProviderService service,
        @Qualifier("laxFailureHandler") ValidationFailureHandler handler
    ) {
        return new ResetPasswordValidationService(resetPasswordNotifier, helper, service, handler);
    }
}
