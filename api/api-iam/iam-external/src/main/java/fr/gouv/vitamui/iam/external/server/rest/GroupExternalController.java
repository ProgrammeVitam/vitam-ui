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
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.GroupExternalService;
import io.swagger.annotations.Api;
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
 * The controller to check existence, create, read, update and delete the profile groups.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_GROUPS_URL)
@Getter
@Setter
@Api(tags = "groups", value = "Profiles Groups Management", description = "Profiles Groups Management")
public class GroupExternalController implements CrudController<GroupDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GroupExternalController.class);

    private final GroupExternalService groupCrudService;

    @Autowired
    public GroupExternalController(final GroupExternalService profileGroupCrudService) {
        groupCrudService = profileGroupCrudService;
    }

    @GetMapping
    @Secured(ServicesData.ROLE_GET_GROUPS)
    public List<GroupDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("get all group criteria={}, embedded={}", criteria, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return groupCrudService.getAll(criteria, embedded);
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    @Secured(ServicesData.ROLE_GET_GROUPS)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        RestUtils.checkCriteria(Optional.of(criteria));
        LOGGER.debug("check exist by criteria={}", criteria);
        final boolean exist = groupCrudService.checkExists(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_GROUPS)
    public GroupDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> embedded) {
        LOGGER.debug("Get group {} embedded={}", id, embedded);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.check(id);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return groupCrudService.getOne(id, embedded);
    }

    @Secured(ServicesData.ROLE_GET_GROUPS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<GroupDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction, @RequestParam(required = false) final Optional<String> embedded) {

        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}, embedded = {}", page, size, orderBy, direction, embedded);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        RestUtils.checkCriteria(criteria);
        return groupCrudService.getAllPaginated(page, size, criteria, orderBy, direction, embedded);
    }

    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_GROUPS)
    @Override
    public GroupDto create(final @Valid @RequestBody GroupDto dto) {
        LOGGER.debug("Create group {}", dto);
        return groupCrudService.create(dto);
    }

    @Override
    public GroupDto update(final @PathVariable("id") String id, final @Valid @RequestBody GroupDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_GROUPS)
    public GroupDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch group {} with {}", id, partialDto);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch group : the DTO id must match the path id");
        return groupCrudService.patch(partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for group with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return groupCrudService.findHistoryById(id);
    }

    /**
     * Get levels by criteria.
     * @param criteria Criteria as json string
     * @return List of matching levels
     */
    @GetMapping(CommonConstants.PATH_LEVELS)
    @Secured(ServicesData.ROLE_GET_GROUPS)
    public List<String> getLevels(final Optional<String> criteria) {
        LOGGER.debug("Get levels with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return groupCrudService.getLevels(criteria);
    }

}
