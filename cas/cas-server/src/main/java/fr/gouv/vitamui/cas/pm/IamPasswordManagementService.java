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

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.Getter;
import lombok.Setter;

import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;

/**
 * Specific password management service based on the IAM API.
 *
 *
 */
@Getter
@Setter
public class IamPasswordManagementService extends BasePasswordManagementService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IamPasswordManagementService.class);

    private final CasExternalRestClient casExternalRestClient;

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final CentralAuthenticationService centralAuthenticationService;

    private final Utils utils;

    private final TicketRegistry ticketRegistry;

    public IamPasswordManagementService(final PasswordManagementProperties passwordManagementProperties,
                                        final CipherExecutor<Serializable, String> cipherExecutor,
                                        final String issuer,
                                        final PasswordHistoryService passwordHistoryService,
                                        final CasExternalRestClient casExternalRestClient,
                                        final ProvidersService providersService,
                                        final IdentityProviderHelper identityProviderHelper,
                                        final CentralAuthenticationService centralAuthenticationService,
                                        final Utils utils,
                                        final TicketRegistry ticketRegistry) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.casExternalRestClient = casExternalRestClient;
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
        this.centralAuthenticationService = centralAuthenticationService;
        this.utils = utils;
        this.ticketRegistry = ticketRegistry;
    }

    protected RequestContext blockIfSubrogation() {
        val requestContext = RequestContextHolder.getRequestContext();
        val authentication = WebUtils.getAuthentication(requestContext);
        if (authentication != null) {
            String superUsername = (String) utils.getAttributeValue(authentication.getAttributes(), SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL);
            if (superUsername == null) {
                superUsername = (String) utils.getAttributeValue(authentication.getPrincipal().getAttributes(), SUPER_USER_ATTRIBUTE);
            }
            LOGGER.debug("is it currently a superUser: {}", superUsername);
            Assert.isNull(superUsername, "cannot use password management with subrogation");
        }

        return requestContext;
    }

    @Override
    public boolean changeInternal(final Credential c, final PasswordChangeRequest bean) throws InvalidPasswordException {
        val requestContext = blockIfSubrogation();
        val flowScope = requestContext.getFlowScope();
        if (flowScope != null) {
            flowScope.put("passwordHasBeenChanged", true);
        }

        val upc = (UsernamePasswordCredential) c;
        val username = upc.getUsername();
        Assert.notNull(username, "username can not be null");
        // username to lowercase
        val usernameLowercase = username.toLowerCase().trim();
        LOGGER.debug("username: {}", usernameLowercase);
        val identityProvider = identityProviderHelper.findByUserIdentifier(providersService.getProviders(), usernameLowercase);
        Assert.isTrue(identityProvider.isPresent(), "only a user [" + usernameLowercase + "] linked to an identity provider can change his password");
        Assert.isTrue(identityProvider.get().getInternal() != null && identityProvider.get().getInternal(),
                "only an internal user [" + usernameLowercase + "] can change his password");

        try {
            casExternalRestClient.changePassword(utils.buildContext(usernameLowercase), usernameLowercase, bean.getPassword());
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
        val usernameWithLowercase = username.toLowerCase().trim();
        try {
            val user = casExternalRestClient.getUserByEmail(utils.buildContext(usernameWithLowercase), usernameWithLowercase, Optional.empty());
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
