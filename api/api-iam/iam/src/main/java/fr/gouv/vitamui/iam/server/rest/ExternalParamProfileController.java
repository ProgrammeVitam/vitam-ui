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
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.HistoryEventDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.server.externalparamprofile.service.ExternalParamProfileService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the profile external params.
 */
@RestController
@RequestMapping(RestApi.V1_EXTERNAL_PARAM_PROFILE_URL)
@Tag(name = "ExternalParamProfile", description = "Access Contract External Parameters Profile")
public class ExternalParamProfileController implements CrudController<ExternalParamProfileDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalParamProfileController.class);

    private final ExternalParamProfileService externalParamProfileService;

    public ExternalParamProfileController(final ExternalParamProfileService externalParamProfileService) {
        this.externalParamProfileService = externalParamProfileService;
    }

    @Secured(ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    @Operation(
        operationId = "externalParamProfile_getAllPaginated",
        summary = "Get external parameters profile, paginated result"
    )
    @GetMapping(path = "/paginated", params = { "page", "size" })
    public PaginatedValuesDto<ExternalParamProfileDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction,
        @RequestParam(required = false) final Optional<String> embedded
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(criteria);
        direction.ifPresent(SanityChecker::sanitizeCriteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        LOGGER.debug(
            "getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}, embedded = {}",
            page,
            size,
            criteria,
            orderBy,
            direction,
            embedded
        );
        return externalParamProfileService.getAllPaginated(
            page,
            size,
            criteria.orElse(null),
            orderBy.orElse(null),
            direction.orElse(null)
        );
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    @Operation(
        operationId = "externalParamProfile_checkExist",
        summary = "Check existence of external parameter profile"
    )
    @Secured(ServicesData.ROLE_GET_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ResponseEntity<Void> checkExist(String criteria) {
        SanityChecker.sanitizeCriteria(Optional.of(criteria));
        LOGGER.debug("checkExist criteria={}", criteria);
        final boolean exist = externalParamProfileService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "externalParamProfile_create", summary = "Create external parameter profile")
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ExternalParamProfileDto create(@RequestBody final ExternalParamProfileDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.debug("create class={}", entityDto.getClass().getName());
        return externalParamProfileService.create(entityDto);
    }

    @Hidden
    @PutMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    @Override
    public ExternalParamProfileDto update(String id, ExternalParamProfileDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Operation(operationId = "externalParamProfile_getOne", summary = "Get external parameter profile by id")
    @Secured(ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    @Override
    public ExternalParamProfileDto getOne(@PathVariable String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get One with id : {}", id);
        return externalParamProfileService.getOne(id);
    }

    @Operation(
        operationId = "externalParamProfile_findHistoryById",
        summary = "Get history by external parameter profile profile's id"
    )
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    @Secured(ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public List<HistoryEventDto> findHistoryById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for external parameter profile with id :{}", id);
        return externalParamProfileService.findHistoryById(id);
    }

    @Operation(operationId = "externalParamProfile_patchMe", summary = "Patch external parameter profile")
    @PatchMapping(value = CommonConstants.PATH_ME)
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ExternalParamProfileDto patchMe(@RequestBody final Map<String, Object> partialDto)
        throws PreconditionFailedException {
        SanityChecker.sanitizeCriteria(partialDto);
        return externalParamProfileService.patch(partialDto);
    }
}
