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
package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.CasExternalService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
public class CasExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CasExternalController.class);

    private final CasExternalService casService;

    @Autowired
    public CasExternalController(final CasExternalService casService) {
        this.casService = casService;
    }

    @PostMapping(value = RestApi.CAS_LOGIN_PATH)
    @Secured(ServicesData.ROLE_CAS_LOGIN)
    public ResponseEntity<UserDto> login(final @Valid @RequestBody LoginRequestDto dto) {
        final UserDto user = casService.login(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping(RestApi.CAS_CHANGE_PASSWORD_PATH)
    @Secured(ServicesData.ROLE_CAS_CHANGE_PASSWORD)
    @ResponseBody
    public String changePassword(@RequestHeader(defaultValue = "") final String username, @RequestHeader(defaultValue = "") final String password) {
        LOGGER.debug("changePassword for username: {} / password_exists? {}", username, StringUtils.isNotBlank(password));
        ParameterChecker.checkParameter("The user and password are mandatory : ", username, password);
        SanityChecker.check(username);
        casService.changePassword(username, password);
        return "true";
    }

    @GetMapping(value = RestApi.CAS_USERS_PATH, params = "email")
    @Secured(ServicesData.ROLE_CAS_USERS)
    public UserDto getUserByEmail(@RequestParam final String email, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("getUserByEmail: {} embedded: {}", email, embedded);
        ParameterChecker.checkParameter("The email is mandatory : ", email);
        return casService.getUserByEmail(email, embedded);
    }

    @GetMapping(value = RestApi.CAS_USERS_PATH, params = "id")
    @Secured(ServicesData.ROLE_CAS_USERS)
    public UserDto getUserById(@RequestParam final String id) {
        LOGGER.debug("getUserById: {}", id);
        ParameterChecker.checkParameter("The identifier is mandatory : ", id);
        return casService.getUserById(id);
    }

    @GetMapping(value = RestApi.CAS_SUBROGATIONS_PATH, params = "superUserEmail")
    @Secured(ServicesData.ROLE_CAS_SUBROGATIONS)
    public List<SubrogationDto> getSubrogationsBySuperUserEmail(@RequestParam final String superUserEmail) {
        LOGGER.debug("getMySubrogationAsSuperuser: {}", superUserEmail);
        ParameterChecker.checkParameter("The superUserEmail is mandatory : ", superUserEmail);
        return casService.getSubrogationsBySuperUser(superUserEmail);
    }

    @GetMapping(value = RestApi.CAS_SUBROGATIONS_PATH, params = "superUserId")
    @Secured(ServicesData.ROLE_CAS_SUBROGATIONS)
    public List<SubrogationDto> getSubrogationsBySuperUserId(@RequestParam final String superUserId) {
        LOGGER.debug("getSubrogationsBySuperUserId: {}", superUserId);
        ParameterChecker.checkParameter("The superUserId is mandatory : ", superUserId);
        return casService.getSubrogationsBySuperUserId(superUserId);
    }

    @GetMapping(value = RestApi.CAS_LOGOUT_PATH)
    @Secured(ServicesData.ROLE_CAS_LOGOUT)
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestParam final String authToken, @RequestParam final String superUser) {

        LOGGER.debug("logout: authToken={}, superUser={}", authToken, superUser);
        ParameterChecker.checkParameter("The authToken is mandatory : ", authToken);
        casService.logout(authToken, superUser);
    }
}
