/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

package fr.gouv.vitamui.referential.external.server.rest;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.ManagementContractExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.MANAGEMENT_CONTRACTS_URL)
@Getter
@Setter
public class ManagementContractExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ManagementContractExternalController.class
    );

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    private final ManagementContractExternalService managementContractExternalService;

    @Autowired
    public ManagementContractExternalController(ManagementContractExternalService managementContractExternalService) {
        this.managementContractExternalService = managementContractExternalService;
    }

    @GetMapping
    @Secured(ServicesData.ROLE_GET_MANAGEMENT_CONTRACT)
    public Collection<ManagementContractDto> getAll(final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("get all management contracts criteria={}", criteria);
        return managementContractExternalService.getAll(criteria);
    }

    @GetMapping(params = { "page", "size" })
    @Secured(ServicesData.ROLE_GET_MANAGEMENT_CONTRACT)
    public PaginatedValuesDto<ManagementContractDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) throws PreconditionFailedException {
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            orderBy,
            direction
        );
        return managementContractExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    @Secured(ServicesData.ROLE_GET_MANAGEMENT_CONTRACT)
    public ManagementContractDto getOne(final @PathVariable("identifier") String identifier)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, identifier);
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("get managementContract  by identifier = {}", identifier);
        return managementContractExternalService.getOne(identifier);
    }

    @PostMapping(CommonConstants.PATH_CHECK)
    @Secured({ ServicesData.ROLE_GET_MANAGEMENT_CONTRACT })
    public ResponseEntity<Void> check(
        @RequestBody ManagementContractDto managementContractDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) {
        SanityChecker.sanitizeCriteria(managementContractDto);
        LOGGER.debug("check exist managementContract = {}", managementContractDto);
        final boolean exist = managementContractExternalService.check(managementContractDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_MANAGEMENT_CONTRACT)
    public ManagementContractDto create(final @Valid @RequestBody ManagementContractDto managementContractDto)
        throws PreconditionFailedException {
        SanityChecker.sanitizeCriteria(managementContractDto);
        LOGGER.debug("Create new management contract {}", managementContractDto);
        return managementContractExternalService.create(managementContractDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_MANAGEMENT_CONTRACT)
    public ManagementContractDto patch(
        final @PathVariable("id") String id,
        @RequestBody final Map<String, Object> partialDto
    ) throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        LOGGER.debug("Patch {} with {}", id, partialDto);
        return managementContractExternalService.patch(partialDto);
    }

    @GetMapping(CommonConstants.PATH_ID + "/history")
    @Secured(ServicesData.ROLE_GET_MANAGEMENT_CONTRACT)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for ManagementContract with id :{}", id);
        return managementContractExternalService.findHistoryById(id);
    }
}
