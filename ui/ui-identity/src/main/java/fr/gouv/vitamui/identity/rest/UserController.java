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
package fr.gouv.vitamui.identity.rest;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.identity.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api(tags = "users")
@RequestMapping("${ui-prefix}/users")
@RestController
@ResponseBody
public class UserController extends AbstractUiRestController {

    protected final UserService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserController.class);

    @Autowired
    public UserController(final UserService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get entity")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDto getOne(final @PathVariable String id) {
        LOGGER.debug("Get user={}", id);
        return service.getOne(buildUiHttpContext(), id);
    }

    /**
     * Modify user
     *
     * @param userDto
     * @return
     */
    @ApiOperation(value = "Update entity")
    @PutMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@RequestBody final UserDto userDto) {
        LOGGER.debug("update user {}", userDto.getId());
        return service.update(buildUiHttpContext(), userDto);
    }

    /**
     * Create user
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "Create entity")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody final UserDto dto) {
        LOGGER.debug("create user = {}", dto.getEmail());
        return service.create(buildUiHttpContext(), dto);
    }

    /**
     * Find users by customer.
     *
     * @param page
     * @param size
     * @param orderBy
     * @param direction
     * @return
     */
    @ApiOperation(value = "Find users by customer")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<UserDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Optional<String> criteria,
            @RequestParam final Integer size, @RequestParam final Optional<String> orderBy, @RequestParam final Optional<DirectionDto> direction) {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    @ApiOperation(value = "Patch entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch User {} with {}", id, partialDto);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch user : the DTO id must match the path id.");
        return service.patch(buildUiHttpContext(), partialDto, id);
    }

    /**
     * Check user by email.
     *
     * @param criteria
     * @return
     */
    @ApiOperation(value = "Check entity exist by params")
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExistByParams(@RequestParam final String criteria) {
        LOGGER.debug("check exist by criteria={}", criteria);
        RestUtils.checkCriteria(Optional.of(criteria));
        final boolean exist = service.checkExist(buildUiHttpContext(), criteria);
        LOGGER.debug("response value={}" + exist);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ApiOperation(value = "get history by user's id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable String id) {
        LOGGER.debug("get logbook for users with id :{}", id);
        SanityChecker.check(id);
        return service.findHistoryById(buildUiHttpContext(), id);
    }

    /**
     * Get levels by criteria.
     * @param criteria Criteria as json string
     * @return List of matching levels
     */
    @GetMapping(CommonConstants.PATH_LEVELS)
    public List<String> getLevels(final Optional<String> criteria) {
        LOGGER.debug("Get levels with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return service.getLevels(buildUiHttpContext(), criteria);
    }
}
