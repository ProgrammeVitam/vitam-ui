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

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication handler to check the username/password on the IAM API.
 */
public class UserAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationHandler.class);

    private final CasExternalRestClient casExternalRestClient;

    private final Utils utils;

    private final String ipHeaderName;

    public UserAuthenticationHandler(
        final ServicesManager servicesManager,
        final PrincipalFactory principalFactory,
        final CasExternalRestClient casExternalRestClient,
        final Utils utils,
        final String ipHeaderName
    ) {
        super(UserAuthenticationHandler.class.getSimpleName(), servicesManager, principalFactory, 1);
        this.casExternalRestClient = casExternalRestClient;
        this.utils = utils;
        this.ipHeaderName = ipHeaderName;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential transformedCredential,
        final String originalPassword
    ) throws GeneralSecurityException, PreventedException {
        val requestContext = RequestContextHolder.getRequestContext();
        val flowScope = requestContext.getFlowScope();
        val loginEmail = flowScope.getRequiredString(Constants.FLOW_LOGIN_EMAIL);
        val loginCustomerId = flowScope.getRequiredString(Constants.FLOW_LOGIN_CUSTOMER_ID);
        val surrogateEmail = flowScope.getString(Constants.FLOW_SURROGATE_EMAIL);
        val surrogateCustomerId = flowScope.getString(Constants.FLOW_SURROGATE_CUSTOMER_ID);
        val externalContext = requestContext.getExternalContext();
        val ip = ((HttpServletRequest) externalContext.getNativeRequest()).getHeader(ipHeaderName);
        val context = utils.buildContext(loginEmail);

        LOGGER.debug(
            "Authenticating loginEmail: {} / loginCustomerId: {} / surrogateEmail: {} / surrogateCustomerId:" +
            " {} / IP: {}",
            loginEmail,
            loginCustomerId,
            surrogateEmail,
            surrogateCustomerId,
            ip
        );

        try {
            val user = casExternalRestClient.login(
                context,
                loginEmail,
                loginCustomerId,
                originalPassword,
                surrogateEmail,
                surrogateCustomerId,
                ip
            );
            if (user != null) {
                if (mustChangePassword(user)) {
                    LOGGER.info("Password expired for: {} ({})", loginEmail, loginCustomerId);
                    throw new AccountPasswordMustChangeException("Password expired for: " + loginEmail);
                } else if (user.getStatus() == UserStatusEnum.ENABLED && user.getType() == UserTypeEnum.NOMINATIVE) {
                    Map<String, List<Object>> attributes = new HashMap<>();

                    attributes.put(Constants.FLOW_LOGIN_EMAIL, List.of(loginEmail));
                    attributes.put(Constants.FLOW_LOGIN_CUSTOMER_ID, List.of(loginCustomerId));

                    if (surrogateEmail != null) {
                        attributes.put(Constants.FLOW_SURROGATE_EMAIL, List.of(surrogateEmail));
                        attributes.put(Constants.FLOW_SURROGATE_CUSTOMER_ID, List.of(surrogateCustomerId));
                    }

                    final Principal principal = principalFactory.createPrincipal(loginEmail, attributes);
                    LOGGER.debug("Successful authentication, created principal: {}", principal);
                    return createHandlerResult(transformedCredential, principal, new ArrayList<>());
                } else {
                    LOGGER.debug("Cannot login user: {} ({})", loginEmail, loginCustomerId);
                    throw new AccountException("Disabled or cannot login user: " + loginEmail);
                }
            } else {
                LOGGER.debug("No user found for: {} ({})", loginEmail, loginCustomerId);
                throw new AccountNotFoundException("Bad credentials for: " + loginEmail);
            }
        } catch (final InvalidAuthenticationException e) {
            LOGGER.error("Bad credentials for username: {} ({})", loginEmail, loginCustomerId);
            throw new CredentialNotFoundException("Bad credentials for username: " + loginEmail);
        } catch (final TooManyRequestsException e) {
            LOGGER.error("Too many login attempts for username: {} ({})", loginEmail, loginCustomerId);
            throw new AccountLockedException("Too many login attempts for username: " + loginEmail);
        } catch (final InvalidFormatException e) {
            LOGGER.error("Bad status for username: {} ({})", loginEmail, loginCustomerId);
            throw new AccountDisabledException("Bad status: " + loginEmail);
        } catch (final VitamUIException e) {
            LOGGER.error(String.format("Unexpected exception for username: %s(%s)", loginEmail, loginCustomerId), e);
            throw new PreventedException(e);
        }
    }

    protected boolean mustChangePassword(final UserDto user) {
        val pwdExpirationDate = user.getPasswordExpirationDate();
        return (pwdExpirationDate == null || pwdExpirationDate.isBefore(OffsetDateTime.now()));
    }
}
