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
package fr.gouv.vitamui.cas.pm;

import java.util.Map;
import java.util.Optional;

import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.Getter;
import lombok.Setter;

/**
 * Specific password management service based on the IAM API.
 *
 *
 */
@Getter
@Setter
public class IamRestPasswordManagementService extends BasePasswordManagementService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IamRestPasswordManagementService.class);

    private static final String PM_TICKET_ID = "pmTicketId";

    private final CasExternalRestClient casExternalRestClient;

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    @Autowired
    private DefaultCentralAuthenticationService centralAuthenticationService;

    @Autowired
    private Utils utils;

    @Autowired
    private PmTokenTicketFactory pmTokenTicketFactory;

    @Autowired
    private TicketRegistry ticketRegistry;

    public IamRestPasswordManagementService(final CasExternalRestClient casExternalRestClient, final PasswordManagementProperties passwordManagementProperties,
            final ProvidersService providersService, final IdentityProviderHelper identityProviderHelper) {
        super(passwordManagementProperties, null, null);
        this.casExternalRestClient = casExternalRestClient;
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
    }

    protected RequestContext blockIfSubrogation() {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        Authentication authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
            if (tgtId != null) {
                final TicketGrantingTicket tgt = centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
                authentication = tgt.getAuthentication();
            }
        }
        if (authentication != null) {
            final String superUsername = utils.getSuperUsername(authentication);
            Assert.isNull(superUsername, "cannot use password management with subrogation");
        }

        return requestContext;
    }

    @Override
    public boolean changeInternal(final Credential c, final PasswordChangeBean bean) {
        final RequestContext requestContext = blockIfSubrogation();
        final MutableAttributeMap flowScope = requestContext.getFlowScope();
        if (flowScope != null) {
            flowScope.put("passwordHasBeenChanged", true);
        }

        final UsernamePasswordCredential upc = (UsernamePasswordCredential) c;
        final String username = upc.getUsername();
        Assert.notNull(username, "username can not be null");
        // username to lowercase
        final String usernameLowercase = username.toLowerCase();
        LOGGER.debug("username: {}", usernameLowercase);
        final Optional<IdentityProviderDto> identityProvider = identityProviderHelper.findByUserIdentifier(providersService.getProviders(), usernameLowercase);
        Assert.isTrue(identityProvider.isPresent(), "only a user [" + usernameLowercase + "] linked to an identity provider can change his password");
        Assert.isTrue(identityProvider.get().getInternal() != null && identityProvider.get().getInternal(),
                "only an internal user [" + usernameLowercase + "] can change his password");

        // we don't care about the fact that the oldPassword is the same as upc.getPassword();
        try {
            casExternalRestClient.changePassword(utils.buildContext(usernameLowercase), usernameLowercase, bean.getPassword());

            final String ticket = (String) requestContext.getFlowScope().get(PM_TICKET_ID);
            if (ticket != null) {
                ticketRegistry.deleteTicket(ticket);
            }

            return true;
        }
        catch (final ConflictException e) {
            throw new PasswordAlreadyUsedException();
        }
        catch (final VitamUIException e) {
            LOGGER.error("Cannot change password", e);
            return false;
        }
    }

    @Override
    public String findEmail(final String username) {
        String email = null;
        final String usernameWithLowercase = username.toLowerCase();
        try {
            final UserDto user = casExternalRestClient.getUserByEmail(utils.buildContext(usernameWithLowercase), usernameWithLowercase, Optional.empty());
            if (user != null && UserStatusEnum.ENABLED.equals(user.getStatus())) {
                email = user.getEmail();
            }
        }
        catch (final VitamUIException e) {
            LOGGER.error("Cannot retrieve user: {}", usernameWithLowercase, e);
        }
        return email;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        throw new UnsupportedOperationException("security questions/answers are not available");
    }

    @Override
    public String createToken(final String to) {
        final PmTokenTicket ticket = pmTokenTicketFactory.create(to, (int) properties.getReset().getExpirationMinutes());
        ticketRegistry.addTicket(ticket);
        return ticket.getId();
    }

    @Override
    public String parseToken(final String token) {
        final PmTokenTicket ticket = ticketRegistry.getTicket(token, PmTokenTicket.class);
        if (ticket == null || ticket.isExpired()) {
            LOGGER.warn("PM token ticket expired: {}", token);
            return null;
        }
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            requestContext.getFlowScope().put(PM_TICKET_ID, ticket.getId());
        }
        return ticket.getUser();
    }

    private static class PasswordAlreadyUsedException extends InvalidPasswordException {

        /**
         *
         */
        private static final long serialVersionUID = -8981663363751187075L;

        public PasswordAlreadyUsedException() {
            super(".alreadyUsed", null, null);
        }

        @Override
        public String getMessage() {
            return "password already used";
        }
    }
}
