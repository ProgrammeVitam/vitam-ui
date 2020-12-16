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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

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
public class GroupInternalController implements CrudController<GroupDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GroupInternalController.class);

    private GroupInternalService internalGroupService;

    @Autowired
    public GroupInternalController(final GroupInternalService internalGroupService) {
        this.internalGroupService = internalGroupService;
    }

    /**
     * Get All with criteria and embedded request.
     * @param criteria
     * @param embedded
     * @return
     */
    @GetMapping
    public List<GroupDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("get all criteria={}, embedded={}", criteria, embedded);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return internalGroupService.getAll(criteria, embedded);
    }

    /**
     * Get paginated items with criteria and embedded request.
     * @param criteria
     * @param embedded
     * @return
     */
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<GroupDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction, @RequestParam(required = false) final Optional<String> embedded) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}, embedded = {}", page, size, orderBy, direction, embedded);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        RestUtils.checkCriteria(criteria);
        return internalGroupService.getAllPaginated(page, size, criteria, orderBy, direction, embedded);
    }

    /**
     * GetOne with criteria, item id and embedded request.
     * @param id
     * @param criteria
     * @param embedded
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public GroupDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria, final @RequestParam Optional<String> embedded) {
        LOGGER.debug("Get {} criteria={} embedded={}", id, criteria, embedded);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return internalGroupService.getOne(id, criteria, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        LOGGER.debug("check exist criteria={}", criteria);
        RestUtils.checkCriteria(Optional.of(criteria));
        final boolean exist = internalGroupService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    /**
     * {@inheritDoc}
     */
    @PostMapping
    @Override
    public GroupDto create(final @Valid @RequestBody GroupDto dto) {
        LOGGER.debug("Create {}", dto);
        return internalGroupService.create(dto);
    }

    /**
     * {@inheritDoc}
     */
    @PutMapping(CommonConstants.PATH_ID)
    @Override
    public GroupDto update(final @PathVariable("id") String id, final @Valid @RequestBody GroupDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    public GroupDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return internalGroupService.patch(partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        LOGGER.debug("get logbook for group with id :{}", id);
        return internalGroupService.findHistoryById(id);
    }


    /**
     * Returns a list of profile levels by criteria.
     * @param criteria Criteria as json string
     * @return List of matching levels
     */
    @GetMapping(CommonConstants.PATH_LEVELS)
    public List<String> getLevels(final Optional<String> criteria) {
        LOGGER.debug("Get levels with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return internalGroupService.getLevels(criteria);
    }
}
