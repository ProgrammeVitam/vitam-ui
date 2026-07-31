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
package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.VitamuiRestClientFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sends the welcome email to a freshly created user by delegating to the Spring Authorization Server
 * ({@code POST /api/password/first-connection}) — SAS owns the reset/welcome token store and the
 * mailer since chantier #6.
 */
@Getter
@Setter
public class UserEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEmailService.class);

    /**
     * Path on the auth-server that accepts the welcome payload. Kept in a property so ops can pin a
     * different route if the SAS ever moves that endpoint (unlikely).
     */
    @Value("${auth-server.first-connection.path:/api/password/first-connection}")
    @NotNull
    private String firstConnectionPath;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private IdentityProviderService internalIdentityProviderService;

    private final VitamuiRestClientFactory vitamuiRestClientFactory;

    public UserEmailService(final VitamuiRestClientFactory vitamuiRestClientFactory) {
        this.vitamuiRestClientFactory = vitamuiRestClientFactory;
    }

    /**
     * Fires the welcome email for a newly created user. Silent no-op when the user is not eligible
     * (not nominative, not enabled, or their customer has no enabled internal IdP — the user could
     * never sign in with a local password anyway, so the "choose your password" link would lead
     * nowhere).
     *
     * <p>The pattern-based guard used before (email must match an internal IdP pattern) was dropped:
     * IdP patterns are prefix-based (e.g. {@code admin.*}) and would silently drop welcome emails
     * for legitimate accounts that don't happen to fit the pattern. The right question is "does the
     * customer accept local passwords at all?" — a much less brittle test.
     *
     * <p>The SAS call is best-effort; on failure we log and continue so a mail hiccup can't roll
     * back the user creation.
     */
    public void sendCreationEmail(final UserDto userDto) {
        if (
            userDto == null ||
            userDto.getStatus() != UserStatusEnum.ENABLED ||
            userDto.getType() != UserTypeEnum.NOMINATIVE
        ) {
            return;
        }
        final List<IdentityProviderDto> providers = internalIdentityProviderService.getAll(
            Optional.empty(),
            Optional.empty()
        );
        boolean customerHasInternalIdp = providers
            .stream()
            .anyMatch(
                p ->
                    userDto.getCustomerId().equals(p.getCustomerId()) &&
                    Boolean.TRUE.equals(p.getInternal()) &&
                    Boolean.TRUE.equals(p.getEnabled())
            );
        if (!customerHasInternalIdp) {
            LOGGER.debug(
                "Skipping welcome email for {} — customer {} has no enabled internal IdP",
                userDto.getEmail(),
                userDto.getCustomerId()
            );
            return;
        }

        final UserInfoDto userInfoDto = userInfoService.getOne(userDto.getUserInfoId());
        final Map<String, String> body = Map.of(
            "email",
            userDto.getEmail(),
            "customerId",
            userDto.getCustomerId(),
            "firstname",
            userDto.getFirstname() != null ? userDto.getFirstname() : "",
            "lastname",
            userDto.getLastname() != null ? userDto.getLastname() : "",
            "language",
            LanguageDto.valueOf(userInfoDto.getLanguage()).getLanguage()
        );

        try {
            LOGGER.debug("Requesting welcome email from SAS for {}", userDto.getEmail());
            vitamuiRestClientFactory
                .getRestClient()
                .post()
                .uri(vitamuiRestClientFactory.getBaseUrl() + firstConnectionPath)
                .body(body)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            // Do not fail user creation because SAS is down; ops will replay via the reset flow.
            LOGGER.warn("Welcome email request to SAS failed for {}: {}", userDto.getEmail(), e.toString());
        }
    }
}
