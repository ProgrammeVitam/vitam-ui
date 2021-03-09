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
package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.cas.service.CasInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import io.swagger.annotations.Api;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The controller for CAS operations.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_CAS_URL)
@Api(tags = "cas", value = "User authentication management for CAS", description = "User authentication management for CAS")
public class CasInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CasInternalController.class);

    @Value("${login.attempts.maximum.failures}")
    @NotNull
    @Setter
    private Integer maximumFailuresForLoginAttempts;

    @Setter
    @Autowired
    private IamLogbookService iamLogbookService;

    private final CasInternalService casService;

    private final PasswordEncoder passwordEncoder;

    private final UserInternalService internalUserService;

    @Autowired
    public CasInternalController(final CasInternalService casService, final PasswordEncoder passwordEncoder, final UserInternalService internalUserService) {
        this.casService = casService;
        this.passwordEncoder = passwordEncoder;
        this.internalUserService = internalUserService;
    }

    @PostMapping(value = RestApi.CAS_LOGIN_PATH)
    public ResponseEntity<UserDto> login(final @Valid @RequestBody LoginRequestDto dto) {
        final String username = dto.getUsername();
        final User user = casService.findEntityByEmail(username);
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
        }
        else if (nbFailedAttemps < maximumFailuresForLoginAttempts) {
            nbFailedAttemps = 0;
        }
        user.setNbFailedAttempts(nbFailedAttemps);
        user.setLastConnection(now);

        if (nbFailedAttemps >= maximumFailuresForLoginAttempts) {
            user.setStatus(UserStatusEnum.BLOCKED);
        }
        else if (user.getStatus() == UserStatusEnum.BLOCKED) {
            user.setStatus(UserStatusEnum.ENABLED);
        }
        casService.updateNbFailedAttempsPlusLastConnectionAndStatus(user, nbFailedAttemps, oldStatus);

        LOGGER.debug("username: {} -> passwordMatch: {} / nbFailedAttemps: {}", username, passwordMatch, nbFailedAttemps);
        if (nbFailedAttemps >= maximumFailuresForLoginAttempts) {
            final String message = "Too many login attempts for username: " + username;
            iamLogbookService.loginEvent(user, findSurrogateByEmail(dto), dto.getIp(), message);
            throw new TooManyRequestsException(message);
        }
        else if (passwordMatch) {
            final UserDto userDto = internalUserService.internalConvertFromEntityToDto(user);
            iamLogbookService.loginEvent(user, findSurrogateByEmail(dto), dto.getIp(), null);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        }
        else {
            final String message = "Bad credentials for username: " + username;
            iamLogbookService.loginEvent(user, findSurrogateByEmail(dto), dto.getIp(), message);
            throw new UnAuthorizedException(message);
        }
    }

    private String findSurrogateByEmail(final LoginRequestDto loginRequest) {
        final String surrogate = loginRequest.getSurrogate();
        if (surrogate != null) {
            try {
                return internalUserService.findUserByEmail(surrogate).getIdentifier();
            }
            catch (final NotFoundException e) {
                return "User not found: " + surrogate;
            }
        }
        return null;
    }

    @PostMapping(RestApi.CAS_CHANGE_PASSWORD_PATH)
    @ResponseBody
    public String changePassword(@RequestHeader(defaultValue = "") final String username, @RequestHeader(defaultValue = "") final String password) {
        LOGGER.debug("changePassword for username: {} / password_exists? {}", username, StringUtils.isNotBlank(password));
        casService.updatePassword(username, password);
        return "true";
    }

    @GetMapping(value = RestApi.CAS_USERS_PATH, params = "email")
    public UserDto getUserByEmail(@RequestParam final String email, final @RequestParam Optional<String> embedded) {
        LOGGER.debug("getUserByEmail: {}", email);
        ParameterChecker.checkParameter("user email is mandatory : ", email);
        return casService.getUserByEmail(email, embedded);
    }

    @GetMapping(value = RestApi.CAS_USERS_PATH, params = "id")
    public UserDto getUserById(@RequestParam final String id) {
        LOGGER.debug("getUserById: {}", id);
        return casService.getUserProfileById(id);
    }

    @GetMapping(value = RestApi.CAS_SUBROGATIONS_PATH, params = "superUserEmail")
    public List<SubrogationDto> getSubrogationsBySuperUserEmail(@RequestParam final String superUserEmail) {
        LOGGER.debug("getMySubrogationAsSuperuser: {}", superUserEmail);
        ParameterChecker.checkParameter("super user email is mandatory : ", superUserEmail);
        return casService.getSubrogationsBySuperUser(superUserEmail);
    }

    @GetMapping(value = RestApi.CAS_SUBROGATIONS_PATH, params = "superUserId")
    public List<SubrogationDto> getSubrogationsBySuperUserId(@RequestParam final String superUserId) {
        LOGGER.debug("findBySuperUserId: {}", superUserId);
        ParameterChecker.checkParameter("super user identifier is mandatory : ", superUserId);
        final UserDto user = internalUserService.getOne(superUserId, Optional.empty());
        if (user != null && user.getStatus() == UserStatusEnum.ENABLED) {
            final String email = user.getEmail();
            LOGGER.debug("-> email: {}", email);
            return casService.getSubrogationsBySuperUser(email);
        }
        else {
            return new ArrayList<>();
        }
    }

    @GetMapping(value = RestApi.CAS_LOGOUT_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestParam final String authToken, @RequestParam final String superUser) {
        LOGGER.debug("logout: authToken={}, superUser={}", authToken, superUser);
        ParameterChecker.checkParameter("The arguments authToken is mandatory : ", authToken);
        final String principal = casService.removeTokenAndGetUsername(authToken);

        if (StringUtils.isNotBlank(principal) && StringUtils.isNotBlank(superUser)) {
            casService.deleteSubrogationBySuperUserAndSurrogate(superUser, principal);
        }
    }
}
