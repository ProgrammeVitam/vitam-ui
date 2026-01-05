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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.model.UserLoginModel;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;

/**
 * Specific password management service based on the IAM API.
 */
@Getter
@Setter
@Slf4j
public class IamPasswordManagementService extends BasePasswordManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CasApi casApi;

    private final ProvidersService providersService;

    private final IdentityProviderHelper identityProviderHelper;

    private final CentralAuthenticationService centralAuthenticationService;

    private final Utils utils;

    private final TicketRegistry ticketRegistry;

    private final PasswordValidator passwordValidator;

    private final PasswordConfiguration passwordConfiguration;

    public IamPasswordManagementService(
        final PasswordManagementProperties passwordManagementProperties,
        final CipherExecutor<Serializable, String> cipherExecutor,
        final String issuer,
        final PasswordHistoryService passwordHistoryService,
        final CasApi casApi,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final CentralAuthenticationService centralAuthenticationService,
        final Utils utils,
        final TicketRegistry ticketRegistry,
        final PasswordValidator passwordValidator,
        final PasswordConfiguration passwordConfiguration
    ) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.casApi = casApi;
        this.providersService = providersService;
        this.identityProviderHelper = identityProviderHelper;
        this.centralAuthenticationService = centralAuthenticationService;
        this.utils = utils;
        this.ticketRegistry = ticketRegistry;
        this.passwordValidator = passwordValidator;
        this.passwordConfiguration = passwordConfiguration;
    }

    protected RequestContext blockIfSubrogation() {
        final var requestContext = RequestContextHolder.getRequestContext();
        final var authentication = WebUtils.getAuthentication(requestContext);
        if (authentication != null) {
            // login/pwd subrogation
            String superUsername = (String) utils.getAttributeValue(
                authentication.getAttributes(),
                SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL
            );
            if (superUsername == null) {
                // authn delegation subrogation
                superUsername = (String) utils.getAttributeValue(
                    authentication.getPrincipal().getAttributes(),
                    SUPER_USER_ATTRIBUTE
                );
            }
            LOGGER.debug("is it currently a superUser: {}", superUsername);
            Assert.isNull(superUsername, "cannot use password management with subrogation");
        }

        return requestContext;
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) throws InvalidPasswordException {
        final var requestContext = blockIfSubrogation();
        final var flowScope = requestContext.getFlowScope();

        if (flowScope != null) {
            flowScope.put("passwordHasBeenChanged", true);
        }

        String password = new String(bean.getPassword());
        String confirmedPassword = new String(bean.getConfirmedPassword());

        if (!passwordValidator.isEqualConfirmed(password, confirmedPassword)) {
            throw new PasswordConfirmException();
        }

        if (!passwordValidator.isValid(getProperties().getCore().getPasswordPolicyPattern(), password)) {
            throw new PasswordNotMatchRegexException();
        }

        final var username = bean.getUsername();
        LOGGER.debug("passwordConfiguration: {}", passwordConfiguration);
        Assert.notNull(username, "username can not be null");

        final UserLoginModel userLogin = extractUserLoginAndCustomerIdModel(flowScope, username);
        final UserDto user = findUserByEmailAndCustomerId(userLogin.getUserEmail(), userLogin.getCustomerId());

        if (user == null) {
            LOGGER.debug("User not found with login: {}", userLogin.getUserEmail());
            throw new InvalidPasswordException();
        }

        if (user.getStatus() != UserStatusEnum.ENABLED) {
            LOGGER.debug("User cannot login: {} - User {}", userLogin.getUserEmail(), user.toString());
            throw new InvalidPasswordException();
        }

        if (
            (passwordConfiguration.getProfile().equalsIgnoreCase("anssi") &&
                passwordConfiguration.isCheckOccurrence() &&
                passwordConfiguration.getOccurrencesCharsNumber() != null &&
                passwordConfiguration.getOccurrencesCharsNumber() > 0) ||
            (!passwordConfiguration.getProfile().equalsIgnoreCase("anssi") &&
                passwordConfiguration.isCheckOccurrence() &&
                passwordConfiguration.getOccurrencesCharsNumber() != null &&
                passwordConfiguration.getOccurrencesCharsNumber() > 0)
        ) {
            String userLastName = user.getLastname();
            Assert.notNull(userLastName, "user last name can not be null");
            if (
                passwordValidator.isContainsUserOccurrences(
                    userLastName,
                    password,
                    passwordConfiguration.getOccurrencesCharsNumber()
                )
            ) {
                throw new PasswordContainsUserDictionaryException(
                    "Invalid password containing an occurence of user name !"
                );
            }
        }

        final var identityProvider = identityProviderHelper.findByUserIdentifierAndCustomerId(
            providersService.getProviders(),
            userLogin.getUserEmail(),
            userLogin.getCustomerId()
        );
        Assert.isTrue(
            identityProvider.isPresent(),
            "only a user [" + userLogin.getUserEmail() + "] linked to an identity provider can change his password"
        );
        Assert.isTrue(
            identityProvider.get().getInternal() != null && identityProvider.get().getInternal(),
            "only an internal user [" + userLogin.getUserEmail() + "] can change his password"
        );

        try {
            casApi.changePassword(userLogin.getUserEmail(), userLogin.getCustomerId(), password);
            return true;
        } catch (final ConflictException e) {
            throw new PasswordAlreadyUsedException();
        } catch (final VitamUIException e) {
            LOGGER.error("Cannot change password", e);
            return false;
        }
    }

    @NotNull
    private UserLoginModel extractUserLoginAndCustomerIdModel(MutableAttributeMap<Object> flowScope, String username) {
        // IMPORTANT: 2 possible workflows :
        // -> If we came from password expiration workflow ==> We already have the
        // username/customerId from flow scope
        // -> If we came from password reset link by email ==> We use a dirty hack to
        // encode a username+password pair as
        // a json-serialized UserLoginModel encoded into the `username` field.

        String loginEmailFromFlowScope = null;
        String loginCustomerIdFromFlowScope = null;
        if (flowScope != null) {
            loginEmailFromFlowScope = flowScope.getString(Constants.FLOW_LOGIN_EMAIL);
            loginCustomerIdFromFlowScope = flowScope.getString(Constants.FLOW_LOGIN_CUSTOMER_ID);
        }
        if (StringUtils.isNoneBlank(loginEmailFromFlowScope, loginCustomerIdFromFlowScope)) {
            // User customerId already in the scope ==> We came from password expired flow
            Assert.isTrue(
                Objects.equals(loginEmailFromFlowScope, username),
                "Email does not match login email from flow"
            );
            UserLoginModel userLoginNode = new UserLoginModel();
            userLoginNode.setCustomerId(loginCustomerIdFromFlowScope);
            userLoginNode.setUserEmail(username);
            return userLoginNode;
        }

        try {
            UserLoginModel userLoginNode = OBJECT_MAPPER.readValue(username, new TypeReference<>() {});

            if (StringUtils.isBlank(userLoginNode.getUserEmail())) {
                LOGGER.error("Could not find the user email for password changing ");
                throw new UsernameNotFoundException("Could not find the user email for password changing ");
            }
            if (StringUtils.isBlank(userLoginNode.getCustomerId())) {
                LOGGER.error("Could not find the user customer Id for password changing ");
                throw new InsufficientAuthenticationException(
                    "Could not find the user customer Id  for password changing "
                );
            }

            userLoginNode.setUserEmail(userLoginNode.getUserEmail().toLowerCase().trim());
            return userLoginNode;
        } catch (JacksonException e) {
            throw new IllegalStateException(
                "Cannot deserialize username field into a " +
                UserLoginModel.class.getSimpleName() +
                " instance. " +
                "Field value: '" +
                username +
                "'",
                e
            );
        }
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        final var username = query.getUsername().toLowerCase().trim();
        final String customerId = (String) query.getRecord().getFirst(Constants.RESET_PWD_CUSTOMER_ID_ATTR);

        try {
            UserDto user = findUserByEmailAndCustomerId(username, customerId);
            if (user == null) {
                LOGGER.error("User not found");
                return null;
            }
            if (UserStatusEnum.ENABLED.equals(user.getStatus())) {
                return user.getEmail();
            }
            return null;
        } catch (final VitamUIException e) {
            LOGGER.error("Cannot retrieve user: {}", username, e);
            throw new PreventedException(e);
        }
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        throw new UnsupportedOperationException("security questions/answers are not available");
    }

    private static class PasswordAlreadyUsedException extends InvalidPasswordException {

        private static final long serialVersionUID = -8981663363751187075L;

        public PasswordAlreadyUsedException() {
            super(".alreadyUsed", null, null);
        }

        @Override
        public String getMessage() {
            return "password already used";
        }
    }

    protected static class PasswordNotMatchRegexException extends InvalidPasswordException {

        private static final long serialVersionUID = -8981663363751187076L;

        public PasswordNotMatchRegexException() {
            super(".invalidPassword", null, null);
        }

        @Override
        public String getMessage() {
            return "password not match global regex";
        }
    }

    protected static class PasswordConfirmException extends InvalidPasswordException {

        private static final long serialVersionUID = -8981663363751187076L;

        public PasswordConfirmException() {
            super(".pwdNotConfirm", null, null);
        }

        @Override
        public String getMessage() {
            return "password confirm error";
        }
    }

    protected static class PasswordContainsUserDictionaryException extends InvalidPasswordException {

        private static final long serialVersionUID = -8981663363751187075L;

        public PasswordContainsUserDictionaryException(String message) {
            super(".invalidPwdDictionary", message, null);
        }

        @Override
        public String getMessage() {
            return "Invalid password containing an occurence of user name !";
        }
    }

    private UserDto findUserByEmailAndCustomerId(String email, String customerId) {
        return casApi.getUser(email, customerId, null, null, null);
    }
}
