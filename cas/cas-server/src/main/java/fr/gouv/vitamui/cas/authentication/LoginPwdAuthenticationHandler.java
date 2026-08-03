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
package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialNotFoundException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.cas.util.Constants.FLOW_LOGIN_CUSTOMER_ID;
import static fr.gouv.vitamui.cas.util.Constants.FLOW_LOGIN_EMAIL;
import static fr.gouv.vitamui.cas.util.Constants.FLOW_SURROGATE_CUSTOMER_ID;
import static fr.gouv.vitamui.cas.util.Constants.FLOW_SURROGATE_EMAIL;

/**
 * Authentication handler to check the username/password on the IAM API.
 */
@Slf4j
public class LoginPwdAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final CasApi casApi;

    private final String ipHeaderName;

    public LoginPwdAuthenticationHandler(
        final PrincipalFactory principalFactory,
        final CasApi casApi,
        final String ipHeaderName
    ) {
        // CAS 7.3 dropped the ServicesManager argument from AbstractAuthenticationHandler.
        super(LoginPwdAuthenticationHandler.class.getSimpleName(), principalFactory, 1);
        this.casApi = casApi;
        this.ipHeaderName = ipHeaderName;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential transformedCredential,
        final String originalPassword
    ) throws GeneralSecurityException, PreventedException {
        final var login = buildLoginRequestFromFlowScopeData(originalPassword);

        try {
            final var user = casApi.login(login);

            if (user != null) {
                if (mustChangePassword(user)) {
                    LOGGER.info("Password expired for: {} ({})", login.getLoginEmail(), login.getLoginCustomerId());
                    throw new AccountPasswordMustChangeException("Password expired for: " + login.getLoginEmail());
                } else if (user.getStatus() == UserStatusEnum.ENABLED && user.getType() == UserTypeEnum.NOMINATIVE) {
                    Map<String, List<Object>> attributes = new HashMap<>();

                    attributes.put(FLOW_LOGIN_EMAIL, List.of(login.getLoginEmail()));
                    attributes.put(FLOW_LOGIN_CUSTOMER_ID, List.of(login.getLoginCustomerId()));

                    if (login.getSurrogateEmail() != null) {
                        attributes.put(FLOW_SURROGATE_EMAIL, List.of(login.getSurrogateEmail()));
                        attributes.put(FLOW_SURROGATE_CUSTOMER_ID, List.of(login.getSurrogateCustomerId()));
                    }

                    Principal principal;
                    try {
                        principal = principalFactory.createPrincipal(login.getLoginEmail(), attributes);
                    } catch (final Throwable e) {
                        LOGGER.error("Error creating principal", e);
                        throw new PreventedException(e);
                    }
                    LOGGER.debug("Successful authentication, created principal: {}", principal);
                    return createHandlerResult(transformedCredential, principal, new ArrayList<>());
                } else {
                    LOGGER.debug("Cannot login user: {} ({})", login.getLoginEmail(), login.getLoginCustomerId());
                    throw new AccountException("Disabled or cannot login user: " + login.getLoginEmail());
                }
            } else {
                LOGGER.debug("No user found for: {} ({})", login.getLoginEmail(), login.getLoginCustomerId());
                throw new AccountNotFoundException("Bad credentials for: " + login.getLoginEmail());
            }
        } catch (final InvalidAuthenticationException e) {
            LOGGER.error("Bad credentials for username: {} ({})", login.getLoginEmail(), login.getLoginCustomerId());
            throw new CredentialNotFoundException("Bad credentials for username: " + login.getLoginEmail());
        } catch (final TooManyRequestsException e) {
            LOGGER.error(
                "Too many login attempts for username: {} ({})",
                login.getLoginEmail(),
                login.getLoginCustomerId()
            );
            throw new AccountLockedException("Too many login attempts for username: " + login.getLoginEmail());
        } catch (final InvalidFormatException e) {
            LOGGER.error("Bad status for username: {} ({})", login.getLoginEmail(), login.getLoginCustomerId());
            throw new AccountDisabledException("Bad status: " + login.getLoginEmail());
        } catch (final VitamUIException e) {
            LOGGER.error(
                "Unexpected exception for username: {}({})",
                login.getLoginEmail(),
                login.getLoginCustomerId(),
                e
            );
            throw new PreventedException(e);
        }
    }

    protected boolean mustChangePassword(final UserDto user) {
        var pwdExpirationDate = user.getPasswordExpirationDate();
        return (pwdExpirationDate == null || pwdExpirationDate.isBefore(OffsetDateTime.now()));
    }

    private LoginRequestDto buildLoginRequestFromFlowScopeData(String originalPassword) {
        var requestContext = RequestContextHolder.getRequestContext();
        var flowScope = requestContext.getFlowScope();

        String loginEmail = flowScope.getRequiredString(FLOW_LOGIN_EMAIL);
        String loginCustomerId = flowScope.getRequiredString(FLOW_LOGIN_CUSTOMER_ID);
        String surrogateEmail = flowScope.getString(FLOW_SURROGATE_EMAIL);
        String surrogateCustomerId = flowScope.getString(FLOW_SURROGATE_CUSTOMER_ID);
        String ip = extractClientIp(requestContext);

        logAuthenticationAttempt(loginEmail, loginCustomerId, surrogateEmail, surrogateCustomerId, ip);

        return buildLoginRequest(
            originalPassword,
            loginEmail,
            loginCustomerId,
            surrogateEmail,
            surrogateCustomerId,
            ip
        );
    }

    private String extractClientIp(RequestContext requestContext) {
        var externalContext = requestContext.getExternalContext();
        var request = (HttpServletRequest) externalContext.getNativeRequest();
        return request.getHeader(ipHeaderName);
    }

    private void logAuthenticationAttempt(
        String loginEmail,
        String loginCustomerId,
        String surrogateEmail,
        String surrogateCustomerId,
        String ip
    ) {
        LOGGER.debug(
            "Authenticating loginEmail={} loginCustomerId={} surrogateEmail={} surrogateCustomerId={} ip={}",
            loginEmail,
            loginCustomerId,
            surrogateEmail,
            surrogateCustomerId,
            ip
        );
    }

    private LoginRequestDto buildLoginRequest(
        String password,
        String loginEmail,
        String loginCustomerId,
        String surrogateEmail,
        String surrogateCustomerId,
        String ip
    ) {
        var login = new LoginRequestDto();
        login.setLoginEmail(loginEmail);
        login.setLoginCustomerId(loginCustomerId);
        login.setPassword(password);
        login.setSurrogateEmail(surrogateEmail);
        login.setSurrogateCustomerId(surrogateCustomerId);
        login.setIp(ip);
        return login;
    }
}
