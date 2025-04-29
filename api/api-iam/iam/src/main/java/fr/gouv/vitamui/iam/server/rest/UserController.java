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
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsCommonResponseDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the users.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_USERS_URL)
@RequiredArgsConstructor
@Getter
@Setter
@Tag(name = "Users", description = "Users Management")
public class UserController implements CrudController<UserDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ConnectionHistoryService connectionHistoryService;
    private final SecurityService securityService;

    @Operation(operationId = "exportUsers", summary = "Export users to xlsx file")
    @GetMapping(CommonConstants.PATH_EXPORT)
    public Resource exportUsers(@RequestParam(required = false) final Optional<String> criteria) {
        LOGGER.debug("Export all users to xlsx file");
        return userService.exportUsers(criteria);
    }

    @Operation(operationId = "getAllPaginated", summary = "Get all users, paginated")
    @Secured(ServicesData.ROLE_GET_USERS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<UserDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(String.valueOf(size), String.valueOf(page));
        SanityChecker.sanitizeCriteria(criteria);
        direction.ifPresent(SanityChecker::sanitizeCriteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, direction={}",
            page,
            size,
            criteria,
            orderBy,
            direction
        );
        return userService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Override
    @GetMapping(CommonConstants.PATH_ID)
    @Operation(operationId = "getOne", summary = "Get a user by its id")
    @Secured(ServicesData.ROLE_GET_USERS)
    public UserDto getOne(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get {}", id);
        return userService.getOne(id);
    }

    @Operation(operationId = "getMe", summary = "Get the current user")
    @GetMapping(CommonConstants.PATH_ME)
    public AuthUserDto getMe() {
        LOGGER.debug("getMe");
        return userService.getMe();
    }

    @Override
    @Operation(operationId = "checkExist", summary = "Check the existence of a user by criteria")
    @Secured({ ServicesData.ROLE_GET_USERS, ServicesData.ROLE_CHECK_USERS })
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        SanityChecker.sanitizeCriteria(Optional.of(criteria));
        LOGGER.debug("Check exists by criteria", criteria);
        final boolean exist = userService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Override
    @PostMapping
    @Operation(operationId = "create", summary = "Create a user")
    @Secured(ServicesData.ROLE_CREATE_USERS)
    public UserDto create(final @Valid @RequestBody UserDto dto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Create {}", dto);
        userService.checkCustomerId(
            dto.getCustomerId(),
            "Unable to create user " + dto.getEmail() + " (" + dto.getCustomerId() + ")"
        );
        return userService.create(dto);
    }

    @Override
    @PutMapping(CommonConstants.PATH_ID)
    @Operation(operationId = "update", summary = "Update a user")
    @Secured(ServicesData.ROLE_UPDATE_USERS)
    public UserDto update(final @PathVariable("id") String id, final @Valid @RequestBody UserDto dto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Update {} with {}", id, dto);
        Assert.isTrue(
            StringUtils.equals(id, dto.getId()),
            "The DTO identifier must match the path identifier for update."
        );
        userService.checkCustomerId(dto.getCustomerId(), "Unable to update user " + dto.getId());
        return userService.update(dto);
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Operation(operationId = "patch", summary = "Patch a user")
    @Secured(ServicesData.ROLE_UPDATE_USERS)
    public UserDto patch(final @PathVariable("id") String id, final @RequestBody Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch User {} with {}", id, partialDto);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "Unable to patch user : the DTO id must match the path id"
        );
        final String customerId = (String) partialDto.get("customerId");
        if (StringUtils.isNotEmpty(customerId)) {
            userService.checkCustomerId(customerId, "Unable to patch user");
        }
        partialDto.put("customerId", securityService.getCustomerId());
        return userService.patch(partialDto);
    }

    @Operation(operationId = "findHistoryById", summary = "Get user history by its id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsCommonResponseDto findHistoryById(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for users with id :{}", id);
        return userService.findHistoryById(id);
    }

    /**
     * Get levels by criteria.
     * @param criteria Criteria as json string
     * @return List of matching levels
     */
    @GetMapping(CommonConstants.PATH_LEVELS)
    @Operation(operationId = "getLevels", summary = "Get levels by criteria")
    @Secured(ServicesData.ROLE_GET_USERS)
    public List<String> getLevels(final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("Get levels with criteria={}", criteria);
        return userService.getLevels(criteria);
    }

    /**
     * Create/refresh current user analytics
     * @param partialDto analytics to create or refresh
     * @return current user with updated analytics
     */
    @Operation(operationId = "patchAnalytics", summary = "Create/refresh current user analytics")
    @PostMapping(CommonConstants.PATH_ANALYTICS)
    public UserDto patchAnalytics(@RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch analytics with {}", partialDto);
        return userService.patchAnalytics(partialDto);
    }
}
