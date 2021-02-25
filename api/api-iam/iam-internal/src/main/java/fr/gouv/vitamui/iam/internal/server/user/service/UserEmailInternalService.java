/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.internal.server.user.service;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.RestClientFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import lombok.Getter;
import lombok.Setter;

/**
 * Send user email service.
 *
 *
 */
@Getter
@Setter
public class UserEmailInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserEmailInternalService.class);

    @Value("${cas.reset.password.url}")
    @NotNull
    @Setter
    private String casResetPasswordUrl;

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @Autowired
    private IdentityProviderInternalService internalIdentityProviderService;

    private final RestClientFactory restClientFactory;

    public UserEmailInternalService(final RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public void sendCreationEmail(final UserDto userDto) {
        if (userDto != null && userDto.getStatus() == UserStatusEnum.ENABLED && userDto.getType() == UserTypeEnum.NOMINATIVE) {
            try {
                final List<IdentityProviderDto> providers = internalIdentityProviderService.getAll(Optional.empty(), Optional.empty());
                if (identityProviderHelper.identifierMatchProviderPattern(providers, userDto.getEmail())) {
                    LOGGER.debug("Sending mail after creating  user: {}", userDto.getEmail());
                    restClientFactory.getRestTemplate().getForEntity(restClientFactory.getBaseUrl() + casResetPasswordUrl, Boolean.class, userDto.getEmail(),
                            userDto.getFirstname(), userDto.getLastname(), LanguageDto.valueOf(userDto.getLanguage()).getLanguage());
                }
            }
            catch (final Exception e) {
                LOGGER.error("User creation: failed to send mail after creation. \n{}", e);
            }

        }
    }
}
