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
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.service.ExternalParamProfileInternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(RestApi.V1_EXTERNAL_PARAM_PROFILE_URL)
@Api(tags = "externalparamprofile", value = "Access Contract External Parameters Profile")
public class ExternalParamProfileInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ExternalParamProfileInternalController.class);

    private final ExternalParamProfileInternalService externalParamProfileInternalService;

    @Autowired
    public ExternalParamProfileInternalController(
        final ExternalParamProfileInternalService externalParamProfileInternalService) {
        this.externalParamProfileInternalService = externalParamProfileInternalService;
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ExternalParamProfileDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction,
        @RequestParam(required = false) final Optional<String> embedded) throws InvalidParseOperationException,
        PreconditionFailedException {
        String orderByValue = orderBy.orElse(null);
        DirectionDto directionValue = direction.orElse(null);
        String criteriaValue = criteria.orElse(null);
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}, embedded = {}", page,
            size, criteriaValue, orderByValue, directionValue);
        SanityChecker.sanitizeCriteria(criteria);
        if (direction.isPresent()) {
            SanityChecker.sanitizeCriteria(direction.get());
        }
        SanityChecker.checkSecureParameter(String.valueOf(size), String.valueOf(page));
        return externalParamProfileInternalService.getAllPaginated(page, size, criteriaValue, orderByValue,
            directionValue);
    }

    @GetMapping(CommonConstants.PATH_ID)
    public ExternalParamProfileDto getOne(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug("GetMyExternalParameters");
        SanityChecker.checkSecureParameter(id);
        return externalParamProfileInternalService.getOne(id);
    }

    @ApiOperation(value = "Create external parameter profile")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalParamProfileDto create(@RequestBody final ExternalParamProfileDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.debug("create class={}", entityDto.getClass().getName());
        return externalParamProfileInternalService.create(entityDto);
    }

    @ApiOperation(value = "get history by external parameter profile profile's id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for external parameter profile with id :{}", id);
        return externalParamProfileInternalService.findHistoryById(id);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    public ExternalParamProfileDto patch(final @PathVariable("id") String id,
        @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException, BadRequestException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update.");
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        return externalParamProfileInternalService.patch(partialDto);
    }

    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        SanityChecker.sanitizeCriteria(Optional.of(criteria));
        ParameterChecker.checkParameter("criteria is mandatory : ", criteria);
        LOGGER.debug("checkExist criteria={}", criteria);
        final boolean exist = externalParamProfileInternalService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

}
