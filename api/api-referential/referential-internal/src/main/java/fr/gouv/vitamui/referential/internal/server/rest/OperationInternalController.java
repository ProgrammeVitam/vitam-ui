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
package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.referential.common.dto.ReportType;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.operation.OperationInternalService;
import fr.gouv.vitamui.referential.internal.server.probativevalue.ProbativeValueInternalService;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.OPERATIONS_URL)
@Getter
@Setter
public class OperationInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OperationInternalController.class);

    @Autowired
    private OperationInternalService operationInternalService;

    @Autowired
    private ProbativeValueInternalService probativeValueInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return operationInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @PostMapping
    public void create(@Valid @RequestBody AuditOptions auditOptions, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId
    ) {
        LOGGER.debug("run audit ={}", auditOptions);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        operationInternalService.runAudit(vitamContext, auditOptions);
    }

    @GetMapping(CommonConstants.PATH_ID + "/download/{type}")
    public ResponseEntity<Resource> exportEventById(
            final @PathVariable("id") String id, final @PathVariable("type") ReportType type,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId) {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        LOGGER.debug("export logbook for operation with id :{}", id);
        Response response = operationInternalService.export(vitamContext, id, type);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for operation with id :{}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return operationInternalService.findHistoryByIdentifier(vitamContext, id);
    }

    @GetMapping(value = "/check" + CommonConstants.PATH_ID)
    public JsonNode checkTraceabilityOperation(final @PathVariable String id, @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId) {
        LOGGER.debug("Launch check traceability operation with id = {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        return operationInternalService.checkTraceabilityOperation(vitamContext, id);
    }

    @PostMapping("/probativeValue")
    public void runProbativeValue(@Valid @RequestBody ProbativeValueRequest probativeValueRequest, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId) {
        LOGGER.debug("run probative value ={}", probativeValueRequest);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        operationInternalService.runProbativeValue(vitamContext, probativeValueRequest);
    }

    @GetMapping("/probativeValue" + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> exportProbativeValue(final @PathVariable("id") String operationId,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId) {
        LOGGER.debug("Export probative value: ", operationId);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        String tempFolder = "/tmp/" + operationId + ".zip";
        File zip = new File(tempFolder);
        try {
            FileOutputStream zipOutputStream = new FileOutputStream(zip);
            probativeValueInternalService.exportReport(vitamContext, operationId, "/tmp", zipOutputStream);
            Resource resource = new InputStreamResource(new FileInputStream(zip.getAbsoluteFile()));
            return new ResponseEntity<>(resource, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            throw new InternalServerException("Error while generating probative value ZIP", e);
        }
    }
}
