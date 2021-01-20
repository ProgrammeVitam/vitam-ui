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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.UserExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

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
@Getter
@Setter
public class UserExternalController implements CrudController<UserDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserExternalController.class);

    private final UserExternalService userExternalService;

    @Autowired
    public UserExternalController(final UserExternalService userService) {
        userExternalService = userService;
    }

    @Secured(ServicesData.ROLE_GET_USERS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<UserDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return userExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Override
    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_USERS)
    public UserDto getOne(final @PathVariable("id") String id) {
        LOGGER.debug("Get {}", id);
        return userExternalService.getOne(id);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    @Secured({ ServicesData.ROLE_GET_USERS, ServicesData.ROLE_CHECK_USERS })
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        LOGGER.debug("Check exists by criteria", criteria);
        RestUtils.checkCriteria(Optional.of(criteria));
        final boolean exist = userExternalService.checkExists(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Override
    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_USERS)
    public UserDto create(final @Valid @RequestBody UserDto dto) {
        LOGGER.debug("Create {}", dto);
        return userExternalService.create(dto);
    }

    @Override
    @PutMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_USERS)
    public UserDto update(final @PathVariable("id") String id, final @Valid @RequestBody UserDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "Unable to update user : the DTO id must match the path id");
        return userExternalService.update(dto);
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_USERS)
    public UserDto patch(final @PathVariable("id") String id, final @RequestBody Map<String, Object> partialDto) {
        LOGGER.debug("Patch User {} with {}", id, partialDto);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch user : the DTO id must match the path id");
        return userExternalService.patch(partialDto);
    }

    @PatchMapping(CommonConstants.PATH_ME)
    @Secured(ServicesData.ROLE_UPDATE_ME_USERS)
    public UserDto patchMe(@RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch me with {}", partialDto);
        return userExternalService.patchMe(partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for user with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.check(id);
        return userExternalService.findHistoryById(id);
    }

    /**
     * Get levels by criteria.
     * @param criteria Criteria as json string
     * @return List of matching levels
     */
    @GetMapping(CommonConstants.PATH_LEVELS)
    @Secured(ServicesData.ROLE_GET_USERS)
    public List<String> getLevels(final Optional<String> criteria) {
        LOGGER.debug("Get levels with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return userExternalService.getLevels(criteria);
    }

    /**
     * Create/refresh current user analytics
     * @param partialDto analytics to create or refresh
     * @return current user with updated analytics
     */
    @PostMapping(CommonConstants.PATH_ANALYTICS)
    public UserDto patchAnalytics(@RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch analytics with {}", partialDto);
        return userExternalService.patchAnalytics(partialDto);
    }
}
