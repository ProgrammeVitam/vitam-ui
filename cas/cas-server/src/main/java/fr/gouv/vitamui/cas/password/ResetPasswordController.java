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
package fr.gouv.vitamui.cas.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.cas.model.UserLoginModel;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordResetUrlDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for CAS extra features.
 */
@RestController
@RequestMapping("/extras")
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordController {

    private static final long ACCOUNT_CREATION_EXPIRATION_IN_MINUTES = 24 * 60L;

    private final Utils utils;

    private final PasswordResetUrlBuilder passwordResetUrlBuilder;

    private final ObjectMapper objectMapper;

    @GetMapping("/passwordResetUrl")
    public ResponseEntity<PasswordResetUrlDto> buildPasswordResetUrl(
        @RequestParam(value = "email", defaultValue = "") final String email,
        @RequestParam(value = "customerId", defaultValue = "") final String customerId,
        final HttpServletRequest httpRequest
    ) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(customerId)) {
            LOGGER.warn("No email or no customerId is provided");
            return ResponseEntity.badRequest().build();
        }

        httpRequest.setAttribute(
            PmTransientSessionTicketExpirationPolicyBuilder.PM_EXPIRATION_IN_MINUTES_ATTRIBUTE,
            ACCOUNT_CREATION_EXPIRATION_IN_MINUTES
        );

        try {
            final var userLoginModel = new UserLoginModel();
            userLoginModel.setUserEmail(email.toLowerCase().trim());
            userLoginModel.setCustomerId(customerId);
            final String userLoginModelToToken = objectMapper.writeValueAsString(userLoginModel);

            final var url = passwordResetUrlBuilder.build(userLoginModelToToken).toString();

            LOGGER.debug(
                "Generated password reset URL [{}]; link is only active for the next [{}] minute(s)",
                utils.sanitizePasswordResetUrl(url),
                ACCOUNT_CREATION_EXPIRATION_IN_MINUTES
            );

            return ResponseEntity.ok(new PasswordResetUrlDto(url, ACCOUNT_CREATION_EXPIRATION_IN_MINUTES));
        } catch (final Throwable e) {
            LOGGER.error("Cannot build the password reset URL", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
