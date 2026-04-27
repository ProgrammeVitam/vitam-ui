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

package fr.gouv.vitamui.iam.server.user.password.reset;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@RequiredArgsConstructor
public class ResetPasswordValidationService implements ResetPasswordNotifier<UserDto> {

    private final ResetPasswordNotifier<UserDto> notifier;
    private final IdentityProviderHelper identityProviderHelper;
    private final IdentityProviderService identityProviderService;
    private final ValidationFailureHandler failureHandler;

    @Override
    public void notify(UserDto target) {
        if (target == null) {
            throw new IllegalArgumentException("User is null");
        }

        if (!validateEligibility(target)) {
            return;
        }

        if (!validatePattern(target)) {
            return;
        }

        notifier.notify(target);
    }

    private boolean validateEligibility(final UserDto userDto) {
        final boolean isEnabled = UserStatusEnum.ENABLED.equals(userDto.getStatus());
        final boolean isNominative = UserTypeEnum.NOMINATIVE.equals(userDto.getType());

        if (!isEnabled || !isNominative) {
            failureHandler.handle(
                String.format(
                    "User %s is not eligible: enabled=%s, nominative=%s",
                    userDto.getEmail(),
                    isEnabled,
                    isNominative
                )
            );
            return false;
        }
        return true;
    }

    private boolean validatePattern(final UserDto userDto) {
        final List<IdentityProviderDto> providers = identityProviderService.getAll(Optional.empty(), Optional.empty());

        final boolean matchesPattern = identityProviderHelper.identifierMatchProviderPattern(
            providers,
            userDto.getEmail(),
            userDto.getCustomerId()
        );

        if (!matchesPattern) {
            failureHandler.handle(
                String.format("User %s does not match any identity provider pattern", userDto.getEmail())
            );
        }

        return matchesPattern;
    }
}
