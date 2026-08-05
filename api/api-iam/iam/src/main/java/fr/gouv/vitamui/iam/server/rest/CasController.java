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
import fr.gouv.vitamui.iam.auth.contract.AuthContractApi;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.auth.contract.LoginRequestDto;
import fr.gouv.vitamui.iam.auth.contract.PasswordPolicyDto;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import fr.gouv.vitamui.iam.auth.contract.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.auth.contract.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
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
import java.util.Map;
import java.util.Optional;

/**
 * The controller for CAS operations.
 */
@RestController
@RequestMapping(AuthContractApi.V1_AUTH_URL)
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

    @PostMapping(value = AuthContractApi.LOGIN_PATH)
    @Operation(operationId = "cas_login", summary = "Performs the login of a user")
    @Secured(ServicesData.ROLE_CAS_LOGIN)
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

    @PostMapping(AuthContractApi.CHANGE_PASSWORD_PATH)
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

    @GetMapping(value = AuthContractApi.USERS_PATH, params = "email")
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

    @GetMapping(value = AuthContractApi.USERS_PATH + AuthContractApi.USERS_PROVISIONING_PATH)
    @Operation(
        operationId = "cas_getUser",
        summary = "Get a user by their loginEmail, loginCustomerId and optional idp"
    )
    @Secured(ServicesData.ROLE_CAS_USERS)
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

    @GetMapping(value = AuthContractApi.SUBROGATIONS_PATH)
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

    @GetMapping(value = AuthContractApi.LOGOUT_PATH)
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

    @GetMapping(value = AuthContractApi.CUSTOMERS_PATH)
    @Operation(operationId = "cas_getCustomersByIds", summary = "Get all customers by ids")
    @Secured(ServicesData.ROLE_CAS_CUSTOMER_IDS)
    public Collection<CustomerDto> getCustomersByIds(final @RequestParam List<String> customerIds) {
        LOGGER.debug("get all customers by ids={}", customerIds);
        ParameterChecker.checkParameter("CustomerIds are mandatory : ", customerIds);
        SanityChecker.checkSecureParameter(customerIds.toArray(new String[0]));
        return casService.getCustomersByIds(customerIds);
    }

    /**
     * Home Realm Discovery : les organisations et fournisseurs d'identité candidats pour un email.
     *
     * La cardinalité de la réponse porte la décision du serveur d'authentification — aucune entrée pour une
     * configuration inexploitable, une pour enchaîner directement, plusieurs pour faire choisir
     * l'organisation. La réponse a la même forme selon que le compte existe ou non, afin de ne pas révéler
     * son existence.
     */
    /**
     * Les attributs d'authentification d'un utilisateur, prêts à être portés tels quels par le jeton.
     *
     * Le serveur d'authentification n'a plus à connaître ni les noms d'attributs ni la façon dont chacun
     * se dérive du modèle utilisateur : il recopie la table sans l'interpréter.
     */
    @PostMapping(value = AuthContractApi.PRINCIPAL_ATTRIBUTES_PATH)
    @Operation(operationId = "cas_buildPrincipalAttributes", summary = "Build the authentication attributes of a user")
    @Secured(ServicesData.ROLE_CAS_PRINCIPAL_ATTRIBUTES)
    public Map<String, List<String>> buildPrincipalAttributes(
        final @Valid @RequestBody PrincipalAttributesRequestDto request
    ) throws InvalidParseOperationException {
        LOGGER.debug("build the principal attributes");
        SanityChecker.checkSecureParameter(request.getLoginEmail(), request.getLoginCustomerId());
        return casService.buildPrincipalAttributes(request);
    }

    /**
     * Valide qu'une subrogation autorise ce super-utilisateur à prendre la place de cet utilisateur.
     *
     * Une réponse vaut autorisation ; un refus prend la forme d'un 404, jamais d'une réponse vide. Le
     * serveur d'authentification n'a donc plus à récupérer les subrogations pour les filtrer lui-même.
     */
    @PostMapping(value = AuthContractApi.SUBROGATION_VALIDATE_PATH)
    @Operation(operationId = "cas_validateSubrogation", summary = "Validate a subrogation and resolve both users")
    @Secured(ServicesData.ROLE_CAS_SUBROGATION_VALIDATE)
    public SubrogationValidateResponseDto validateSubrogation(
        final @Valid @RequestBody SubrogationValidateRequestDto request
    ) throws InvalidParseOperationException {
        LOGGER.debug("validate a subrogation");
        SanityChecker.checkSecureParameter(
            request.getSuperUserEmail(),
            request.getSuperUserCustomerId(),
            request.getSurrogateEmail(),
            request.getSurrogateCustomerId()
        );
        return casService.validateSubrogation(request);
    }

    /**
     * La politique de mot de passe appliquée par IAM, pour que le serveur d'authentification affiche
     * exactement les contraintes qui seront vérifiées plutôt que sa propre copie de la configuration.
     */
    @GetMapping(value = AuthContractApi.PASSWORD_POLICY_PATH)
    @Operation(operationId = "cas_getPasswordPolicy", summary = "Get the password policy enforced by IAM")
    @Secured(ServicesData.ROLE_CAS_PASSWORD_POLICY)
    public PasswordPolicyDto getPasswordPolicy() {
        LOGGER.debug("get the password policy");
        return casService.getPasswordPolicy();
    }

    @GetMapping(value = AuthContractApi.HRD_PATH, params = "email")
    @Operation(
        operationId = "cas_resolveHrd",
        summary = "Resolve the organisations and identity providers for an email"
    )
    @Secured(ServicesData.ROLE_CAS_HRD)
    public List<HrdEntryDto> resolveHrd(final @RequestParam String email) {
        LOGGER.debug("resolve HRD entries");
        ParameterChecker.checkParameter("The email is mandatory : ", email);
        SanityChecker.checkSecureParameter(email);
        return casService.resolveHrdEntries(email);
    }
}
