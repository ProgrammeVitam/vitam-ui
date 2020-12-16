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

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the subrogations.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_SUBROGATIONS_URL)
@Getter
@Setter
@Api(tags = "subrogations", value = "Subrogation Management", description = "Subrogation Management")
public class SubrogationInternalController implements CrudController<SubrogationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationInternalController.class);

    @Autowired
    private SubrogationInternalService internalSubrogationService;

    @Autowired
    private UserInternalService internalUserService;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    public List<SubrogationDto> getAll(@RequestParam final Optional<String> criteria) {
        return internalSubrogationService.getAll(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequestMapping(path = CommonConstants.PATH_ID, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final @PathVariable("id") String id) {
        throw new NotImplementedException("checkExist not supported");
    }

    /**
     * GetOne with criteria, item id.
     * @param id
     * @param criteria
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public SubrogationDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria) {
        LOGGER.debug("Get {} criteria={}", id, criteria);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        RestUtils.checkCriteria(criteria);
        return internalSubrogationService.getOne(id, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public SubrogationDto create(@Valid final @RequestBody SubrogationDto dto) {
        LOGGER.debug("Create {}", dto);
        return internalSubrogationService.create(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(CommonConstants.PATH_ID)
    public SubrogationDto update(final @PathVariable("id") String id, final @Valid @RequestBody SubrogationDto dto) {
        throw new NotImplementedException("Update not supported");
    }

    @PatchMapping("/surrogate/accept/{id}")
    public SubrogationDto accept(final @PathVariable("id") String id) {
        LOGGER.debug("Accepte subrogation id : {}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return internalSubrogationService.accept(id);
    }

    @DeleteMapping("/surrogate/decline/{id}")
    public void decline(final @PathVariable("id") String id) {
        LOGGER.debug("Decline subrogation id : {}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        internalSubrogationService.decline(id);
    }

    @GetMapping("/me/surrogate")
    public SubrogationDto getMySubrogationAsSurrogate() {
        return internalSubrogationService.getMySubrogationAsSurrogate();
    }

    @GetMapping("/me/superuser")
    public SubrogationDto getMySubrogationAsSuperuser() {
        return internalSubrogationService.getMySubrogationAsSuperuser();
    }

    @GetMapping(path = "/users", params = { "page", "size" })
    public PaginatedValuesDto<UserDto> getUsers(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return internalSubrogationService.getUsers(page, size, criteria, orderBy, direction);
    }

    @GetMapping(path = "/groups" + CommonConstants.PATH_ID)
    public GroupDto getGroupById(final @PathVariable("id") String id, final @RequestParam Optional<String> embedded) {
        LOGGER.debug("Get group {}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return internalSubrogationService.getGroupById(id, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(@PathVariable final String id) {
        internalSubrogationService.delete(id);
    }
}
