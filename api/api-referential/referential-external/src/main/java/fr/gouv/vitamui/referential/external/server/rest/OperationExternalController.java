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
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.referential.common.dto.ReportType;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.OperationExternalService;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.OPERATIONS_URL)
@Getter
@Setter
public class OperationExternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgencyExternalController.class);

    @Autowired
    private OperationExternalService operationExternalService;

    @GetMapping
    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    public Collection<LogbookOperationDto> getAll(final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("get all audits criteria={}", criteria);
        return operationExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) {
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            orderBy,
            direction
        );
        return operationExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id)
        throws InvalidParseOperationException {
        LOGGER.debug("get logbook for audit with id :{}", id);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for audit with id :{}", id);
        return operationExternalService.findHistoryById(id);
    }

    @GetMapping(CommonConstants.PATH_ID + "/download/{type}")
    public ResponseEntity<Resource> exportEventById(
        final @PathVariable("id") String id,
        final @PathVariable("type") ReportType type
    ) throws InvalidParseOperationException, PreconditionFailedException {
        EnumUtils.checkValidEnum(ReportType.class, Optional.of(type.name()));
        ParameterChecker.checkParameter("Event Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("export logbook for {} operation with id :{}", type, id);
        return operationExternalService.export(id, type);
    }

    @Secured(ServicesData.ROLE_RUN_AUDITS)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public boolean create(final @Valid @RequestBody AuditOptions auditOptions)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Audit Options is mandatory parameter : ", auditOptions);
        SanityChecker.sanitizeCriteria(auditOptions);
        LOGGER.debug("Create {}", auditOptions);
        return operationExternalService.runAudit(auditOptions);
    }

    @PostMapping(value = "/timestamp")
    public ObjectNode extractInfoFromTimestamp(final @RequestBody String timestamp) {
        final ObjectNode result = JsonHandler.createObjectNode();
        try {
            ASN1InputStream bIn = new ASN1InputStream(
                new ByteArrayInputStream(org.bouncycastle.util.encoders.Base64.decode(timestamp.getBytes()))
            );
            ASN1Primitive obj = bIn.readObject();
            TimeStampResponse tsResp = new TimeStampResponse(obj.toASN1Primitive().getEncoded());
            SignerId signerId = tsResp.getTimeStampToken().getSID();
            X500Name signerCertIssuer = signerId.getIssuer();
            result.put(
                "genTime",
                LocalDateUtil.fromDate(tsResp.getTimeStampToken().getTimeStampInfo().getGenTime()).format(
                    DateTimeFormatter.ISO_DATE_TIME
                )
            );
            result.put("signerCertIssuer", signerCertIssuer.toString());
        } catch (TSPException | IOException e) {
            LOGGER.error("Error while transforming timestamp", e);
            throw new BadRequestException("Error while transforming timestamp", e);
        }
        return result;
    }

    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    @GetMapping(value = "/check" + CommonConstants.PATH_ID)
    public JsonNode checkTraceabilityOperation(final @PathVariable String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Launch check traceability operation with id = {}", id);
        return operationExternalService.checkTraceabilityOperation(id);
    }

    @Secured(ServicesData.ROLE_RUN_PROBATIVE_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/probativeValue")
    public boolean runProbativeValue(final @Valid @RequestBody ProbativeValueRequest probativeValueRequest)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(probativeValueRequest);
        LOGGER.debug("Run {}", probativeValueRequest);
        return operationExternalService.runProbativeValue(probativeValueRequest);
    }

    @Secured(ServicesData.ROLE_RUN_PROBATIVE_VALUE)
    @GetMapping("/probativeValue" + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> exportProbativeValue(final @PathVariable("id") String operationId)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Operation Identifier is mandatory : ", operationId);
        SanityChecker.checkSecureParameter(operationId);
        LOGGER.debug("export logbook for operation with id :{}", operationId);
        return operationExternalService.exportProbativeValue(operationId);
    }
}
