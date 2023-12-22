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

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.ExternalParamProfileExternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the profile external params.
 */
@RestController
@RequestMapping(RestApi.V1_EXTERNAL_PARAM_PROFILE_URL)
@Api(tags = "externalparamprofile", value = "Access Contract External Parameters Profile")
public class ExternalParamProfileExternalController implements
    CrudController<ExternalParamProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ExternalParamProfileExternalController.class);

    private final ExternalParamProfileExternalService service;

    @Autowired
    public ExternalParamProfileExternalController(
        final ExternalParamProfileExternalService externalParamProfileExternalService) {
        this.service = externalParamProfileExternalService;
    }

    @Secured(ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ExternalParamProfileDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction,
        @RequestParam(required = false) final Optional<String> embedded) throws InvalidParseOperationException,
        PreconditionFailedException {

        SanityChecker.sanitizeCriteria(criteria);
        if(direction.isPresent()){
            SanityChecker.sanitizeCriteria(direction.get());
        }
        if(orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}, embedded = {}",
            page, size, criteria, orderBy, direction, embedded);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, embedded);
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    @Secured(ServicesData.ROLE_GET_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ResponseEntity<Void> checkExist(String criteria) {
        SanityChecker.sanitizeCriteria(Optional.of(criteria));
        LOGGER.debug("checkExist criteria={}", criteria);
        final boolean exist = service.checkExists(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ApiOperation(value = "Create external parameter profile")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ExternalParamProfileDto create(@RequestBody final ExternalParamProfileDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.debug("create class={}", entityDto.getClass().getName());
        return service.create(entityDto);
    }

    @ApiOperation(value = "Update external parameter profile")
    @PutMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    @Override
    public ExternalParamProfileDto update(String id, ExternalParamProfileDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    @Override
    public ExternalParamProfileDto getOne(@PathVariable String id) throws InvalidParseOperationException,
        PreconditionFailedException {

        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get One with id : {}", id);
        return service.getOne(id);
    }

    @ApiOperation(value = "get history by external parameter profile profile's id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    @Secured(ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id)throws InvalidParseOperationException,
        PreconditionFailedException  {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for external parameter profile with id :{}", id);
        return service.findHistoryById(id);
    }

    // @deprecated: This endpoint is mandatory only to handle ui module
    // after removing ui module we must delete this enpoint
    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ExternalParamProfileDto patch(final @PathVariable("id") String id,
        @RequestBody final Map<String, Object> partialDto) throws InvalidParseOperationException,
        PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update.");
        return service.patch(partialDto);
    }

    @PatchMapping(value = CommonConstants.PATH_ME)
    @Secured(ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE)
    public ExternalParamProfileDto patchMe(@RequestBody final Map<String, Object> partialDto) throws PreconditionFailedException {
        SanityChecker.sanitizeCriteria(partialDto);
        return service.patch(partialDto);
    }

}
