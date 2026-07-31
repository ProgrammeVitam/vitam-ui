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
package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.cas.CreateTokenRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.CreateTokenResponseDto;
import fr.gouv.vitamui.iam.common.dto.cas.HrdEntryDto;
import fr.gouv.vitamui.iam.common.dto.cas.JitProvisionRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The controller for CAS operations.
 */
@RestController
@RequestMapping(RestApi.V1_CAS_URL)
@Tag(name = "Cas", description = "User authentication management for CAS")
public class CasController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasController.class);

    @Value("${login.attempts.maximum.failures}")
    @NotNull
    @Setter
    private Integer maximumFailuresForLoginAttempts;

    @Setter
    @Autowired
    private IamLogbookService iamLogbookService;

    private final CasService casService;

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    @Autowired
    public CasController(
        final CasService casService,
        final PasswordEncoder passwordEncoder,
        final UserService userService
    ) {
        this.casService = casService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping(value = RestApi.CAS_LOGIN_PATH)
    @Operation(operationId = "cas_login", summary = "Performs the login of a user")
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public ResponseEntity<UserDto> login(final @Valid @RequestBody LoginRequestDto dto) {
        final String username = dto.getLoginEmail();
        final User user = casService.findUserByEmailAndCustomerId(dto.getLoginEmail(), dto.getLoginCustomerId());
        final UserStatusEnum oldStatus = user.getStatus();
        final String password = user.getPassword();
        int nbFailedAttemps = user.getNbFailedAttempts();
        final OffsetDateTime lastConnection = user.getLastConnection();
        final OffsetDateTime now = OffsetDateTime.now();
        final OffsetDateTime nowLess20Minutes = now.plusMinutes(-casService.getTimeIntervalForLoginAttempts());

        if (lastConnection != null && lastConnection.isBefore(nowLess20Minutes)) {
            LOGGER.debug("reset nbFailedAttemps");
            nbFailedAttemps = 0;
        }

        final boolean passwordMatch = passwordEncoder.matches(dto.getPassword(), password);
        if (!passwordMatch) {
            nbFailedAttemps++;
        } else if (nbFailedAttemps < maximumFailuresForLoginAttempts) {
            nbFailedAttemps = 0;
        }
        user.setNbFailedAttempts(nbFailedAttemps);
        user.setLastConnection(now);

        if (nbFailedAttemps >= maximumFailuresForLoginAttempts) {
            user.setStatus(UserStatusEnum.BLOCKED);
        } else if (user.getStatus() == UserStatusEnum.BLOCKED) {
            user.setStatus(UserStatusEnum.ENABLED);
        }
        casService.updateNbFailedAttempsPlusLastConnectionAndStatus(user, nbFailedAttemps, oldStatus);

        LOGGER.debug(
            "username: {} -> passwordMatch: {} / nbFailedAttemps: {}",
            username,
            passwordMatch,
            nbFailedAttemps
        );
        if (nbFailedAttemps >= maximumFailuresForLoginAttempts) {
            final String message = "Too many login attempts for username: " + username;
            iamLogbookService.loginEvent(user, findSurrogateDescriptionStringForLogging(dto), dto.getIp(), message);
            throw new TooManyRequestsException(message);
        } else if (passwordMatch) {
            final UserDto userDto = userService.internalConvertFromEntityToDto(user);
            iamLogbookService.loginEvent(user, findSurrogateDescriptionStringForLogging(dto), dto.getIp(), null);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } else {
            final String message = "Bad credentials for username: " + username;
            iamLogbookService.loginEvent(user, findSurrogateDescriptionStringForLogging(dto), dto.getIp(), message);
            throw new UnAuthorizedException(message);
        }
    }

    private String findSurrogateDescriptionStringForLogging(final LoginRequestDto loginRequest) {
        final String surrogate = loginRequest.getSurrogateEmail();
        final String surrogateCustomerId = loginRequest.getSurrogateCustomerId();
        if (surrogate != null) {
            try {
                return userService.findUserByEmailAndCustomerId(surrogate, surrogateCustomerId).getIdentifier();
            } catch (final NotFoundException e) {
                return "User not found: " + surrogate + " and customerId " + surrogateCustomerId;
            }
        }
        return null;
    }

    @PostMapping(RestApi.CAS_CHANGE_PASSWORD_PATH)
    @Operation(operationId = "cas_changePassword", summary = "Change password of a user")
    @Secured(ServicesData.ROLE_CAS_CHANGE_PASSWORD)
    @ResponseBody
    public String changePassword(
        @RequestHeader final String username,
        @RequestHeader final String password,
        @RequestHeader final String customerId
    ) {
        LOGGER.debug(
            "changePassword for username: {} / password_exists? {}, customerId {} ",
            username,
            StringUtils.isNotBlank(password),
            customerId
        );
        ParameterChecker.checkParameter(
            "The user, customer id and password are mandatory : ",
            username,
            customerId,
            password
        );
        SanityChecker.checkSecureParameter(username);
        casService.updatePassword(username, password, customerId);
        return "true";
    }

    @GetMapping(value = RestApi.CAS_USERS_PATH, params = "email")
    @Operation(operationId = "cas_getUsersByEmail", summary = "Get all users having a given email address")
    @Secured(ServicesData.ROLE_CAS_USERS)
    public List<UserDto> getUsersByEmail(
        @RequestParam final String email,
        @RequestParam final Optional<String> embedded
    ) {
        LOGGER.debug("getUserByEmail: {} embedded: {}", email, embedded);
        ParameterChecker.checkParameter("The email is mandatory : ", email);
        return casService.getUsersByEmail(email, embedded.orElse(null));
    }

    @GetMapping(value = RestApi.CAS_USERS_PATH + RestApi.USERS_PROVISIONING)
    @Operation(
        operationId = "cas_getUser",
        summary = "Get a user by their loginEmail, loginCustomerId and optional idp"
    )
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public UserDto getUser(
        @RequestParam String loginEmail,
        @RequestParam String loginCustomerId,
        @RequestParam(required = false) String idp,
        @RequestParam(required = false) String userIdentifier,
        @RequestParam(required = false) String embedded
    ) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(idp, loginEmail, loginCustomerId);

        if (userIdentifier != null) {
            SanityChecker.checkSecureParameter(userIdentifier);
        }

        LOGGER.debug(
            "getUser - email : {}, customerId : {}, idp : {}, userIdentifier : {}, embedded options : {}",
            loginEmail,
            loginCustomerId,
            idp,
            userIdentifier,
            embedded
        );

        return casService.getUser(loginEmail, loginCustomerId, idp, userIdentifier, embedded);
    }

    @GetMapping(value = RestApi.CAS_SUBROGATIONS_PATH)
    @Operation(
        operationId = "getSubrogationsBySuperUserIdOrEmailAndCustomerId",
        summary = "Get available subrogations for a super user by super user id or by super user email and customerId"
    )
    @Secured(ServicesData.ROLE_CAS_SUBROGATIONS)
    public List<SubrogationDto> getSubrogationsBySuperUserIdOrEmailAndCustomerId(
        @RequestParam(required = false) final String superUserId,
        @RequestParam(required = false) final String superUserEmail,
        @RequestParam(required = false) final String superUserCustomerId
    ) {
        LOGGER.debug(
            "getSubrogationsBySuperUserIdOrEmailAndCustomerId: id: {} | email: {} / customerId: {}",
            superUserId,
            superUserEmail,
            superUserCustomerId
        );
        String email = superUserEmail, customerId = superUserCustomerId;
        if (superUserId != null && !superUserId.isEmpty() && !superUserId.trim().isEmpty()) {
            SanityChecker.checkSecureParameter(superUserId);
            final UserDto user = userService.getOne(superUserId, Optional.empty());
            if (user != null && user.getStatus() == UserStatusEnum.ENABLED) {
                email = user.getEmail();
                customerId = user.getCustomerId();
                LOGGER.debug("-> email: {}, customerId: {}", email, customerId);
            } else {
                return new ArrayList<>();
            }
        }
        ParameterChecker.checkParameter("The superUserEmail is mandatory : ", email);
        ParameterChecker.checkParameter("The superUserCustomerId is mandatory : ", customerId);
        return casService.getSubrogationsBySuperUser(email, customerId);
    }

    @GetMapping(value = RestApi.CAS_LOGOUT_PATH)
    @Operation(
        operationId = "cas_logout",
        summary = "Logout a user, remove the token and delete the subrogation if needed"
    )
    @Secured(ServicesData.ROLE_CAS_LOGOUT)
    @ResponseStatus(HttpStatus.OK)
    public void logout(
        @RequestParam final String authToken,
        @RequestParam(required = false) final String superUser,
        @RequestParam(required = false) final String superUserCustomerId
    ) {
        LOGGER.debug(
            "logout: authToken={}, superUser={}, superUserCustomerId={}",
            authToken,
            superUser,
            superUserCustomerId
        );
        ParameterChecker.checkParameter("The arguments authToken is mandatory : ", authToken);
        final CasService.PrincipalFromToken principal = casService.removeTokenAndGetPrincipal(authToken);

        if (principal != null && StringUtils.isNotBlank(superUser)) {
            casService.deleteSubrogationBySuperUserAndSurrogate(
                superUser,
                superUserCustomerId,
                principal.getEmail(),
                principal.getCustomerId()
            );
        }
    }

    @GetMapping(value = RestApi.CAS_CUSTOMERS_PATH)
    @Operation(operationId = "cas_getCustomersByIds", summary = "Get all customers by ids")
    @Secured(ServicesData.ROLE_CAS_CUSTOMER_IDS)
    public Collection<CustomerDto> getCustomersByIds(final @RequestParam List<String> customerIds) {
        LOGGER.debug("get all customers by ids={}", customerIds);
        ParameterChecker.checkParameter("CustomerIds are mandatory : ", customerIds);
        SanityChecker.checkSecureParameter(customerIds.toArray(new String[0]));
        return casService.getCustomersByIds(customerIds);
    }

    /**
     * Mint an opaque auth token ({@code TOK-<UUID>}) pointing to an existing user, persisted in the {@code tokens} collection.
     * Consumed by external issuers (e.g. Spring Authorization Server POC) after they have authenticated a user via {@link #login}
     * and need a Bearer that vitam-ui Resource Servers accept without any change.
     */
    @PostMapping(value = RestApi.CAS_TOKENS_PATH)
    @Operation(operationId = "cas_createAuthToken", summary = "Create an opaque auth token for a given user")
    @ResponseStatus(HttpStatus.CREATED)
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public CreateTokenResponseDto createAuthToken(@Valid @RequestBody final CreateTokenRequestDto request) {
        LOGGER.debug(
            "createAuthToken refId={} surrogation={} api={}",
            request.getRefId(),
            request.isSurrogation(),
            request.isApi()
        );
        ParameterChecker.checkParameter("refId is mandatory : ", request.getRefId());
        SanityChecker.checkSecureParameter(request.getRefId());
        final String tokenId = casService.createAuthToken(request.getRefId(), request.isSurrogation(), request.isApi());
        return new CreateTokenResponseDto(tokenId);
    }

    /**
     * Mini Home Realm Discovery: given an email, return the list of {@code (customerId, identityProviderId, internal)}
     * tuples matching through the identity provider {@code patterns} regex list.
     * For an unknown email or one that matches no provider, returns an empty list (anti-enumeration).
     */
    @GetMapping(value = RestApi.CAS_HRD_PATH, params = "email")
    @Operation(
        operationId = "cas_resolveHrd",
        summary = "Mini HRD: resolve an email to the list of matching (customer, IdP) tuples"
    )
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public List<HrdEntryDto> resolveHrd(@RequestParam final String email) {
        LOGGER.debug("resolveHrd email={}", email);
        ParameterChecker.checkParameter("email is mandatory : ", email);
        SanityChecker.checkSecureParameter(email);
        return casService.resolveHrdEntries(email);
    }

    /**
     * Just-in-Time provisioning: creates a new vitam-ui user from claims received after a successful
     * federated authentication (OIDC or SAML). Called by SAS POC when the standard resolution fails
     * and the identity provider has {@code autoProvisioningEnabled=true}.
     */
    @PostMapping(value = RestApi.CAS_USERS_JIT_PATH)
    @Operation(operationId = "cas_jitProvisionUser", summary = "JIT-provision a user from an external IdP")
    @ResponseStatus(HttpStatus.CREATED)
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public UserDto jitProvisionUser(@Valid @RequestBody final JitProvisionRequestDto request) {
        LOGGER.debug(
            "jitProvisionUser email={} customer={} idp={}",
            request.getEmail(),
            request.getCustomerId(),
            request.getIdentityProviderId()
        );
        SanityChecker.checkSecureParameter(request.getEmail(), request.getSubjectId());
        return casService.jitProvisionUser(request);
    }

    /**
     * Returns the full {@link IdentityProviderDto} of a given identity provider (including sensitive
     * fields such as client_secret, SAML keystore, etc.). Used by SAS POC to build the
     * {@code ClientRegistration} / {@code RelyingPartyRegistration} on demand at each callback.
     */
    @GetMapping(value = RestApi.CAS_IDP_PATH + "/{id}")
    @Operation(operationId = "cas_getIdentityProvider", summary = "Return a full IdentityProviderDto by id")
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public IdentityProviderDto getIdentityProvider(@PathVariable final String id) {
        LOGGER.debug("getIdentityProvider id={}", id);
        SanityChecker.checkSecureParameter(id);
        return casService.getIdentityProviderById(id);
    }

    /**
     * Validates that an ACCEPTED subrogation exists between the given super-user and surrogate,
     * and returns both resolved user ids. Called by auth-server (SAS POC) at the {@code /login/subrogate} step.
     */
    @PostMapping(value = RestApi.CAS_SUBROGATION_VALIDATE_PATH)
    @Operation(
        operationId = "cas_validateSubrogation",
        summary = "Validate an ACCEPTED subrogation between super-user and surrogate"
    )
    @Secured(ServicesData.ROLE_SYSTEM_SAS)
    public SubrogationValidateResponseDto validateSubrogation(
        @Valid @RequestBody final SubrogationValidateRequestDto request
    ) {
        LOGGER.debug(
            "validateSubrogation superUser={} surrogate={}",
            request.getSuperUserEmail(),
            request.getSurrogateEmail()
        );
        SanityChecker.checkSecureParameter(request.getSuperUserEmail(), request.getSurrogateEmail());
        return casService.validateSubrogation(request);
    }
}
