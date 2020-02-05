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
package fr.gouv.vitamui.ui.commons.rest;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.ui.commons.service.SubrogationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "subrogations")
@RequestMapping("${ui-prefix}/subrogations")
@ResponseBody
public class SubrogationController extends AbstractUiRestController {

    private final SubrogationService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationController.class);

    @Autowired
    public SubrogationController(final SubrogationService service) {
        this.service = service;
    }

    @ApiOperation(value = "Create subrogation")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubrogationDto create(@RequestBody final SubrogationDto entityDto) {
        LOGGER.debug("create class={}", entityDto.getClass().getName());
        return service.create(buildUiHttpContext(), entityDto);
    }

    @ApiOperation(value = "get subrogation by id")
    @GetMapping(CommonConstants.PATH_ID)
    public SubrogationDto getOne(final @PathVariable String id) {
        LOGGER.debug("get subrogation id={}", id);
        return service.getOne(buildUiHttpContext(), id, Optional.empty());
    }

    @ApiOperation(value = "get all subrogation")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Collection<SubrogationDto> getAll(@RequestParam final Optional<String> criteria) {
        return service.getAll(buildUiHttpContext(), criteria);
    }

    @ApiOperation(value = "Delete subrogation for admin users")
    @DeleteMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final String id) {
        LOGGER.debug("Delete {}", id);
        service.delete(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "accept subrogation for surrogate users")
    @PatchMapping("/surrogate/accept/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SubrogationDto accept(@PathVariable final String id) {
        LOGGER.debug("Accepte subrogation id={}", id);
        return service.accept(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "decline subrogation for surrogate users")
    @DeleteMapping("/surrogate/decline/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void decline(@PathVariable final String id) {
        LOGGER.debug("Decline subrogation id={}", id);
        service.decline(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "get the subrogation of the current user as the surrogate")
    @GetMapping("/me/surrogate")
    public SubrogationDto getMySubrogationAsSurrogate() {
        return service.getMySubrogationAsSurrogate(buildUiHttpContext());
    }

    @ApiOperation(value = "get the subrogation of the current user as the super user")
    @GetMapping("/me/superuser")
    public SubrogationDto getMySubrogationAsSuperuser() {
        return service.getMySubrogationAsSuperuser(buildUiHttpContext());
    }

    @ApiOperation(value = "get generic's user")
    @GetMapping(path = "/users/generic", params = { "page", "size" })
    public PaginatedValuesDto<UserDto> getGenericUsers(@RequestParam final Integer page,
            @RequestParam final Integer size, @RequestParam(required = false) final Optional<String> criteria,
            @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size,
                criteria, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return service.getGenericUsers(buildUiHttpContext(), page, size, criteria, orderBy, direction);
    }

    @GetMapping(path = "/groups" + CommonConstants.PATH_ID)
    public GroupDto getGroupById(final @PathVariable("id") String id) {
        LOGGER.debug("Get group {}", id);
        return service.getGroupById(buildUiHttpContext(), id);
    }

}
