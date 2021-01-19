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
package fr.gouv.vitamui.referential.external.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.referential.common.dto.ReportType;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.OperationExternalService;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.OPERATIONS_URL)
@Getter
@Setter
public class OperationExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyExternalController.class);

    @Autowired
    private OperationExternalService operationExternalService;

    @GetMapping()
    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    public Collection<LogbookOperationDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all audits criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return operationExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
                                                         @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
                                                         @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        return operationExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for audit with id :{}", id);
        return operationExternalService.findHistoryById(id);
    }

    @GetMapping(CommonConstants.PATH_ID + "/download/{type}")
    public ResponseEntity<Resource> exportEventById(final @PathVariable("id") String id, final @PathVariable("type") ReportType type) {
        LOGGER.debug("export logbook for {} operation with id :{}", type, id);
        EnumUtils.checkValidEnum(ReportType.class, Optional.of(type.name()));
        ParameterChecker.checkParameter("Event Identifier is mandatory : " , id);
        return operationExternalService.export(id, type);
    }

    @Secured(ServicesData.ROLE_RUN_AUDITS)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public boolean create(final @Valid @RequestBody AuditOptions auditOptions) {
        LOGGER.debug("Create {}", auditOptions);
        ParameterChecker.checkParameter("Audit Options is mandatory parameter : " , auditOptions);
        SanityChecker.sanitizeCriteria(Optional.of(auditOptions.getQuery().toString()));
        return operationExternalService.runAudit(auditOptions);
    }

    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    @GetMapping(value = "/check" + CommonConstants.PATH_ID)
    public JsonNode checkTraceabilityOperation(final @PathVariable String id) {
        LOGGER.debug("Launch check traceability operation with id = {}", id);
        return operationExternalService.checkTraceabilityOperation(id);
    }


    @Secured(ServicesData.ROLE_RUN_PROBATIVE_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/probativeValue")
    public boolean runProbativeValue(final @Valid @RequestBody ProbativeValueRequest probativeValueRequest) {
        LOGGER.debug("Run {}", probativeValueRequest);
        SanityChecker.sanitizeCriteria(Optional.of(probativeValueRequest.getDslQuery().toString()));
        return operationExternalService.runProbativeValue(probativeValueRequest);
    }

    @Secured(ServicesData.ROLE_RUN_PROBATIVE_VALUE)
    @GetMapping("/probativeValue" + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> exportProbativeValue(final @PathVariable("id") String operationId) {
        LOGGER.debug("export logbook for operation with id :{}", operationId);
        ParameterChecker.checkParameter("Operation Identifier is mandatory : " , operationId);
        return operationExternalService.exportProbativeValue(operationId);
    }
}
