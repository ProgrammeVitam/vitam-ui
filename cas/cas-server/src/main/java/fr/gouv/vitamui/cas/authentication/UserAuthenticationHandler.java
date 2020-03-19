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

import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.servlet.http.HttpServletRequest;

import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Authentication handler to check the username/password on the IAM API.
 *
 *
 */
@Getter
@Setter
public class UserAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserAuthenticationHandler.class);

    @Autowired
    private CasExternalRestClient casExternalRestClient;

    @Autowired
    private Utils utils;

    @Value("${ip.header}")
    private String ipHeaderName;

    public UserAuthenticationHandler(final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
        super(UserAuthenticationHandler.class.getSimpleName(), servicesManager, principalFactory, 1);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                                        final String originalPassword) throws GeneralSecurityException, PreventedException {

        final String username = transformedCredential.getUsername().toLowerCase();
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        String surrogate = null;
        String ip = null;
        if (requestContext != null) {
            final MutableAttributeMap<Object> flow = requestContext.getFlowScope();
            if (flow != null) {
                final Object credential = flow.get("credential");
                if (credential instanceof SurrogateUsernamePasswordCredential) {
                    surrogate = ((SurrogateUsernamePasswordCredential) credential).getSurrogateUsername();
                }
            }
            final ExternalContext externalContext = requestContext.getExternalContext();
            if (externalContext != null) {
                ip = ((HttpServletRequest) externalContext.getNativeRequest()).getHeader(ipHeaderName);
            }
        }

        LOGGER.debug("Authenticating username: {} / surrogate: {} / IP: {}", username, surrogate, ip);

        final ExternalHttpContext context = utils.buildContext(username);
        try {
            final UserDto user = casExternalRestClient.login(context, username, originalPassword, surrogate, ip);
            if (user != null) {
                if (mustChangePassword(user)) {
                    LOGGER.info("Password expired for: {}", username);
                    throw new AccountPasswordMustChangeException("Password expired for: " + username);
                } else if (user.getStatus() == UserStatusEnum.ENABLED && user.getType() == UserTypeEnum.NOMINATIVE) {
                    final Principal principal = principalFactory.createPrincipal(username);
                    LOGGER.debug("Successful authentication, created principal: {}", principal);
                    return createHandlerResult(transformedCredential, principal, new ArrayList<>());
                } else {
                    LOGGER.debug("Cannot login user: {}", username);
                    throw new AccountException("Disabled or cannot login user: " + username);
                }
            } else {
                LOGGER.debug("No user found for: {}", username);
                throw new AccountNotFoundException("Bad credentials for: " + username);
            }
        }
        catch (final InvalidAuthenticationException e) {
            LOGGER.error("Bad credentials for username: {}", username);
            throw new CredentialNotFoundException("Bad credentials for username: " + username);
        }
        catch (final TooManyRequestsException e) {
            LOGGER.error("Too many login attempts for username: {}", username);
            throw new AccountLockedException("Too many login attempts for username: " + username);
        }
        catch (final InvalidFormatException e) {
            LOGGER.error("Bad status for username: {}", username);
            throw new AccountDisabledException("Bad status: " + username);
        }
        catch (final VitamUIException e) {
            LOGGER.error("Unexpected exception for username: {} -> {}", username, e);
            throw new PreventedException(e);
        }
    }

    protected boolean mustChangePassword(final UserDto user) {
        final OffsetDateTime pwdExpirationDate = user.getPasswordExpirationDate();
        return (pwdExpirationDate == null || pwdExpirationDate.isBefore(OffsetDateTime.now()));
    }
}
