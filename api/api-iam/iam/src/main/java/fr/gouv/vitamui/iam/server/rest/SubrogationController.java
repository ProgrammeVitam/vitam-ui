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
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.subrogation.service.SubrogationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the subrogations.
 */
@RestController
@RequestMapping(RestApi.V1_SUBROGATIONS_URL)
@Getter
@Setter
@Tag(name = "Subrogations", description = "Subrogation Management")
public class SubrogationController implements CrudController<SubrogationDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubrogationController.class);

    private final SubrogationService subrogationService;

    @Autowired
    public SubrogationController(final SubrogationService subrogationService) {
        this.subrogationService = subrogationService;
    }

    @Override
    @GetMapping
    @Operation(operationId = "subrogations_getAll", summary = "Get all subrogations")
    @Secured(ServicesData.ROLE_GET_SUBROGATIONS)
    public List<SubrogationDto> getAll(@RequestParam final Optional<String> criteria) {
        return subrogationService.getAll(criteria);
    }

    @Override
    @Operation(operationId = "subrogations_checkExist", summary = "Check the existence of a subrogation by id")
    @RequestMapping(path = CommonConstants.PATH_ID, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final @PathVariable("id") String id) {
        throw new NotImplementedException("checkExist not supported");
    }

    @Override
    @GetMapping(CommonConstants.PATH_ID)
    @Operation(operationId = "subrogations_getOne", summary = "Get subrogation by id")
    @Secured(ServicesData.ROLE_GET_SUBROGATIONS)
    public SubrogationDto getOne(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get {}", id);
        return subrogationService.getOne(id);
    }

    @Override
    @PostMapping
    @Operation(operationId = "subrogations_create", summary = "Create a subrogation")
    @Secured(ServicesData.ROLE_CREATE_SUBROGATIONS)
    public SubrogationDto create(@Valid @RequestBody final SubrogationDto dto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Create {}", dto);
        return subrogationService.create(dto);
    }

    @Operation(operationId = "subrogations_getGenericUsers", summary = "Get all generic users with criteria")
    @Secured(ServicesData.ROLE_GET_USERS_SUBROGATIONS)
    @GetMapping(path = "/users/generic/paginated", params = { "page", "size" })
    public PaginatedValuesDto<UserDto> getGenericUsers(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(String.valueOf(page), String.valueOf(size));
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
        return subrogationService.getGenericUsers(page, size, criteria, orderBy, direction);
    }

    @GetMapping(path = "/groups" + CommonConstants.PATH_ID)
    @Operation(operationId = "subrogations_getGroupById", summary = "Get group by id")
    @Secured(ServicesData.ROLE_GET_GROUPS_SUBROGATIONS)
    public GroupDto getGroupById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get group {}", id);
        return subrogationService.getGroupById(id, Optional.of(EmbeddedOptions.ALL.toString()));
    }

    @Override
    public SubrogationDto update(String id, SubrogationDto dto) throws InvalidParseOperationException {
        throw new NotImplementedException("Update not supported");
    }

    @Operation(operationId = "subrogations_accept", summary = "Accept a subrogation")
    @PatchMapping("/surrogate/accept/{id}")
    public SubrogationDto accept(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Accepte subrogation id : {}", id);
        return subrogationService.accept(id);
    }

    @Operation(operationId = "subrogations_decline", summary = "Decline a subrogation")
    @DeleteMapping("/surrogate/decline/{id}")
    public void decline(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Decline subrogation id : {}", id);
        subrogationService.decline(id);
    }

    @Operation(
        operationId = "subrogations_getMySubrogationAsSurrogate",
        summary = "Get authenticated user's subrogation as surrogate"
    )
    @GetMapping("/me/surrogate")
    public SubrogationDto getMySubrogationAsSurrogate() {
        return subrogationService.getMySubrogationAsSurrogate();
    }

    @Operation(
        operationId = "subrogations_getMySubrogationAsSuperuser",
        summary = "Get authenticated user's subrogation as superuser"
    )
    @GetMapping("/me/superuser")
    public SubrogationDto getMySubrogationAsSuperuser() {
        return subrogationService.getMySubrogationAsSuperuser();
    }

    @Override
    @DeleteMapping(CommonConstants.PATH_ID)
    @Operation(operationId = "subrogations_delete", summary = "Delete a subrogation by id")
    @Secured(ServicesData.ROLE_DELETE_SUBROGATIONS)
    public void delete(@PathVariable final String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        subrogationService.delete(id);
    }
}
