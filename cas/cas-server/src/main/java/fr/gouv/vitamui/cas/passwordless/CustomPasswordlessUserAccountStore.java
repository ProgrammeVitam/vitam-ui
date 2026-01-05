/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020) and the signatories
 * of the "VITAM - Accord du Contributeur" agreement.
 *
 * <p>contact@programmevitam.fr
 *
 * <p>This software is a computer program whose purpose is to implement implement a digital
 * archiving front-office system for the secure and efficient high volumetry VITAM solution.
 *
 * <p>This software is governed by the CeCILL-C license under French law and abiding by the rules of
 * distribution of free software. You can use, modify and/ or redistribute the software under the
 * terms of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * <p>As a counterpart to the access to the source code and rights to copy, modify and redistribute
 * granted by the license, users are provided only with a limited warranty and the software's
 * author, the holder of the economic rights, and the successive licensors have only limited
 * liability.
 *
 * <p>In this respect, the user's attention is drawn to the risks associated with loading, using,
 * modifying and/or developing or reproducing the software by the user in light of its specific
 * status of free software, that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the software's suitability as
 * regards their requirements in conditions enabling the security of their systems and/or data to be
 * ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * <p>The fact that you are presently reading this means that you have had knowledge of the CeCILL-C
 * license and that you accept its terms.
 */
package fr.gouv.vitamui.cas.passwordless;

import fr.gouv.vitamui.cas.delegation.Pac4jClientIdentityProviderDto;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class can dispatch the user: - either to the password page - or to an external IdP
 * (authentication delegation) - or to the bad configuration page if the user is not linked to any
 * identity provider - or to the disabled account page if the user is disabled.
 */
@RequiredArgsConstructor
@Slf4j
public class CustomPasswordlessUserAccountStore extends Constants implements PasswordlessUserAccountStore {

    public static final String CUSTOM_PASSWORDLESS_ERROR = "customPasswordlessError";

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final CasApi casApi;

    private final String surrogationSeparator;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final String u) {
        val requestContext = RequestContextHolder.getRequestContext();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        val username = requestContext.getRequestParameters().getRequired(PasswordlessRequestParser.PARAMETER_USERNAME);
        LOGGER.debug("Username: {}", username);
        String dispatchedUser = username;
        val flowScope = requestContext.getFlowScope();
        flowScope.put(PROVIDED_USERNAME, username);
        flowScope.put(LOGIN_USER_EMAIL_PARAM, username);

        String surrogate = null;
        if (username.contains(surrogationSeparator)) {
            dispatchedUser = StringUtils.substringAfter(username, surrogationSeparator).trim();
            surrogate = StringUtils.substringBefore(username, surrogationSeparator).trim();
        }
        flowScope.put(DISPATCHED_USERNAME, dispatchedUser);
        LOGGER.debug("Dispatched user: {} / surrogate: {}", dispatchedUser, surrogate);

        // if the user is disabled, send him to a specific page (ignore not found users: it will fail
        // when checking login/password)
        UserDto dispatcherUserDto = null;

        try {
            final var enabledUsers = findEnabledUsers(dispatchedUser);
            final var hasNoEnabledUser = enabledUsers.isEmpty();
            if (hasNoEnabledUser) {
                return userDisabled(request);
            }
            dispatcherUserDto = enabledUsers.getFirst();
        } catch (final InvalidFormatException e) {
            return userDisabled(request);
        } catch (final NotFoundException ignored) {}

        if (surrogate != null) {
            try {
                final var enabledUsers = findEnabledUsers(surrogate);
                final var hasNoEnabledUser = enabledUsers.isEmpty();
                if (hasNoEnabledUser) {
                    LOGGER.error("Bad status for surrogate: {}", surrogate);
                    return userDisabled(request);
                }
            } catch (final InvalidFormatException e) {
                return userDisabled(request);
            } catch (final NotFoundException ignored) {}
        }

        final List<IdentityProviderDto> providers = providersService.getProviders();
        boolean isInternal;
        Pac4jClientIdentityProviderDto provider;

        if (dispatcherUserDto == null) {
            provider = (Pac4jClientIdentityProviderDto) identityProviderHelper
                .findAutoProvisioningProviderByEmail(providers, dispatchedUser)
                .orElse(null);
        } else {
            provider = (Pac4jClientIdentityProviderDto) identityProviderHelper
                .findByUserIdentifierAndCustomerId(
                    providers,
                    dispatcherUserDto.getEmail(),
                    dispatcherUserDto.getCustomerId()
                )
                .orElse(null);
        }

        if (provider != null) {
            isInternal = provider.getInternal();
        } else {
            return badConfiguration(request);
        }

        val account = new PasswordlessUserAccount();
        account.setUsername(dispatchedUser);
        if (isInternal) {
            account.setRequestPassword(true);
            if (dispatcherUserDto != null && dispatcherUserDto.isOtp()) {
                account.setMultifactorAuthenticationEligible(TriStateBoolean.TRUE);
            }
        } else {
            account.setDelegatedAuthenticationEligible(TriStateBoolean.TRUE);
            account.setAllowedDelegatedClients(Collections.singletonList(provider.getTechnicalName()));
        }
        return Optional.of(account);
    }

    private Optional<PasswordlessUserAccount> userDisabled(final HttpServletRequest request) {
        request.setAttribute(
            CUSTOM_PASSWORDLESS_ERROR,
            CustomPasswordlessAuthenticationWebflowConfigurer.USER_DISABLED
        );
        return Optional.empty();
    }

    private Optional<PasswordlessUserAccount> badConfiguration(final HttpServletRequest request) {
        request.setAttribute(
            CUSTOM_PASSWORDLESS_ERROR,
            CustomPasswordlessAuthenticationWebflowConfigurer.BAD_CONFIGURATION
        );
        return Optional.empty();
    }

    /**
     * TODO: handle same username/login across multiple providers.
     * Finds every enabled users by username/login.
     *
     * @param username to find across providers
     * @return a list of enabled users
     */
    private List<fr.gouv.vitamui.iam.openapiclient.domain.UserDto> findEnabledUsers(String username) {
        final var users = casApi.getUsersByEmail(username, null);
        final var enabledUsers = users
            .stream()
            .filter(user -> user.getStatus().equals(UserStatusEnum.ENABLED))
            .toList();

        if (enabledUsers.size() > 1) {
            LOGGER.warn("Multiple users with same username/login found");
        }

        return enabledUsers;
    }
}
