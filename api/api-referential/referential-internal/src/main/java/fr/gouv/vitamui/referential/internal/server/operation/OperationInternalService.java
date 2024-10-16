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
package fr.gouv.vitamui.referential.internal.server.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.AccessUnauthorizedException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.ReportType;
import fr.gouv.vitamui.referential.common.service.OperationService;
import fr.gouv.vitamui.referential.internal.server.service.ExternalParametersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;

@Service
public class OperationInternalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationInternalService.class);

    private final OperationService operationService;

    private final LogbookService logbookService;
    private final ExternalParametersService externalParametersService;
    private final String AUDIT_FILE_CONSISTENCY = "AUDIT_FILE_CONSISTENCY";
    private final String AUDIT_FILE_RECTIFICATION = "AUDIT_FILE_RECTIFICATION";
    private final String AUDIT_FILE_INTEGRITY = "AUDIT_FILE_INTEGRITY";
    private final String AUDIT_FILE_EXISTING = "AUDIT_FILE_EXISTING";
    private final List<String> AUDITS_WITHOUT_PROJECTION = List.of(AUDIT_FILE_INTEGRITY, AUDIT_FILE_EXISTING);
    public static final String DSL_QUERY_PROJECTION = "$projection";
    public static final String DSL_QUERY_FILTER = "$filter";
    public static final String DSL_QUERY_FACETS = "$facets";
    private final ObjectMapper objectMapper;

    @Autowired
    OperationInternalService(
        OperationService operationService,
        LogbookService logbookService,
        ObjectMapper objectMapper,
        ExternalParametersService externalParametersService
    ) {
        this.operationService = operationService;
        this.logbookService = logbookService;
        this.objectMapper = objectMapper;
        this.externalParametersService = externalParametersService;
    }

    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        LOGGER.info("All Operations EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find LogbookOperations with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        LogbookOperationsResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();
        List<LogbookOperationDto> valuesDto = OperationConverter.convertVitamsToDtos(results.getResults());
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    private LogbookOperationsResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<LogbookOperation> requestResponse;
        try {
            LOGGER.info("All Operations EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = logbookService.selectOperations(query, vitamContext);

            final LogbookOperationsResponseDto logbookOperationsResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                LogbookOperationsResponseDto.class
            );

            return logbookOperationsResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find LogbookOperations", e);
        }
    }

    public void runAudit(VitamContext context, AuditOptions auditOptions) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            Optional<Long> thresholdOpt = externalParametersService.retrieveProfilThreshold();
            LOGGER.info("All Operations Audit EvIdAppSession : {} ", context.getApplicationSessionId());
            updateAuditDslQuery(auditOptions, thresholdOpt);
            auditOptions.setAuditType("dsl");

            if (AUDIT_FILE_CONSISTENCY.equals(auditOptions.getAuditActions())) {
                operationService.lauchEvidenceAudit(context, auditOptions.getQuery());
            } else if (AUDIT_FILE_RECTIFICATION.equals(auditOptions.getAuditActions())) {
                operationService.launchRectificationAudit(context, auditOptions.getAuditType());
            } else {
                operationService.runAudit(context, mapper.valueToTree(auditOptions));
            }
        } catch (AccessExternalClientServerException | VitamClientException | BadRequestException e) {
            throw new InternalServerException("Unable to run audit", e);
        }
    }

    public void updateAuditDslQuery(AuditOptions auditOptions, Optional<Long> thresholdOpt) {
        SelectMultiQuery multiQuery = new SelectMultiQuery();
        try {
            if (
                !List.of("originatingagency", "tenant", "dsl").contains(auditOptions.getAuditType()) ||
                null == thresholdOpt
            ) {
                throw new InvalidCreateOperationException("Invalid audit query");
            }
            if (("originatingagency").equals(auditOptions.getAuditType())) {
                multiQuery.setQuery(QueryHelper.eq("#originating_agency", auditOptions.getObjectId()));
                auditOptions.setQuery(multiQuery.getFinalSelect());
            } else if (("tenant").equals(auditOptions.getAuditType())) {
                multiQuery.setQuery(QueryHelper.eq("#tenant", auditOptions.getObjectId()));
                auditOptions.setQuery(multiQuery.getFinalSelect());
            }
            if (thresholdOpt.isPresent()) {
                ObjectNode previousDslQuery = (ObjectNode) auditOptions.getQuery();
                previousDslQuery.put("$threshold", thresholdOpt.get());
                auditOptions.setQuery(previousDslQuery);
            }

            Arrays.stream(new String[] { DSL_QUERY_PROJECTION, DSL_QUERY_FILTER, DSL_QUERY_FACETS }).forEach(
                ((ObjectNode) auditOptions.getQuery())::remove
            );
            if (!AUDITS_WITHOUT_PROJECTION.contains(auditOptions.getAuditActions())) {
                ObjectNode dslQueryProjection = (ObjectNode) auditOptions.getQuery();
                dslQueryProjection.put(DSL_QUERY_PROJECTION, objectMapper.readTree("{}"));
                auditOptions.setQuery(dslQueryProjection);
            }
        } catch (InvalidCreateOperationException e) {
            LOGGER.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (JsonMappingException e) {
            LOGGER.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    public Response export(VitamContext context, String id, ReportType type) {
        try {
            LOGGER.info("Export  Operations EvIdAppSession : {} ", context.getApplicationSessionId());
            switch (type) {
                case AUDIT:
                    return operationService.exportAudit(context, id);
                case TRACEABILITY:
                    return operationService.exportTraceability(context, id);
                default:
                    throw new InternalServerException("Unable to  export that kind of report: " + type);
            }
        } catch (VitamClientException | AccessExternalClientServerException e) {
            throw new InternalServerException("Unable to export operation report", e);
        }
    }

    public JsonNode checkTraceabilityOperation(VitamContext vitamContext, String id) {
        final Select select = new Select();
        final BooleanQuery query;

        try {
            query = and();
            query.add(eq("evIdProc", id));
            select.setQuery(query);

            RequestResponse response = logbookService.checkTraceability(vitamContext, select.getFinalSelect());
            return response.toJsonNode();
        } catch (
            InvalidCreateOperationException
            | AccessExternalClientServerException
            | InvalidParseOperationException
            | AccessUnauthorizedException e
        ) {
            throw new InternalServerException("Unable to check traceability operation", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, String id) {
        try {
            LOGGER.info("Operation History EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<LogbookOperation> requestResponse = logbookService.selectOperationbyId(id, vitamContext);
            return requestResponse.toJsonNode();
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public void runProbativeValue(VitamContext context, ProbativeValueRequest probativeValueRequest) {
        try {
            LOGGER.info("All Operations Probative Value EvIdAppSession : {} ", context.getApplicationSessionId());
            operationService.runProbativeValue(context, probativeValueRequest);
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to generate Probative value", e);
        }
    }
}
