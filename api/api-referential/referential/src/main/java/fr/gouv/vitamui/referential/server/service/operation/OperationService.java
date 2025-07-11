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
package fr.gouv.vitamui.referential.server.service.operation;

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
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsCommonResponseDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationModel;
import fr.gouv.vitamui.referential.common.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.ReportType;
import fr.gouv.vitamui.referential.common.model.AuditCreateOptions;
import fr.gouv.vitamui.referential.common.service.OperationCommonService;
import fr.gouv.vitamui.referential.server.service.AbstractService;
import fr.gouv.vitamui.referential.server.service.probativevalue.ProbativeValueService;
import fr.gouv.vitamui.referential.server.service.service.ExternalParametersService;
import jakarta.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.model.objectgroup.ObjectGroupResponse.ALL_UNIT_UPS;
import static fr.gouv.vitam.common.model.objectgroup.ObjectGroupResponse.OPERATIONS;
import static fr.gouv.vitam.common.model.objectgroup.ObjectGroupResponse.ORIGINATING_AGENCY;

@Service
public class OperationService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationService.class);

    private final OperationCommonService operationCommonService;

    private final LogbookService logbookService;

    private ProbativeValueService probativeValueService;

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

    private final String AUDIT_PERIMETER_INGEST_OPERATION_PERIOD = "AUDIT_PERIMETER_INGEST_OPERATION_PERIOD";

    public static final String DSL_QUERY = "$query";

    public static final String DSL_QUERY_FIELDS = "$fields";
    public static final String DSL_QUERY_EVID = "evId";
    public static final String APPROXIMATE_CREATION_DATE = "#approximate_creation_date";
    public static final String EV_DATE_TIME = "evDateTime";
    public static final String EV_TYPE_PROC = "evTypeProc";
    public static final String INGEST = "INGEST";
    private final String START_TIME = "T00:00:00.000";
    private final String END_TIME = "T23:59:59.999";

    public OperationService(
        OperationCommonService operationCommonService,
        LogbookService logbookService,
        ObjectMapper objectMapper,
        ExternalParametersService externalParametersService,
        SecurityService securityService,
        ProbativeValueService probativeValueService
    ) {
        super(securityService);
        this.operationCommonService = operationCommonService;
        this.logbookService = logbookService;
        this.objectMapper = objectMapper;
        this.externalParametersService = externalParametersService;
        this.probativeValueService = probativeValueService;
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
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
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

    public void runAudit(VitamContext context, AuditCreateOptions auditCreateOptions) {
        ObjectMapper mapper = new ObjectMapper();
        AuditOptions auditOptions;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            Optional<Long> thresholdOpt = externalParametersService.retrieveProfilThreshold();
            LOGGER.info("All Operations Audit EvIdAppSession : {} ", context.getApplicationSessionId());

            if (AUDIT_FILE_RECTIFICATION.equals(auditCreateOptions.getAuditActions())) {
                operationCommonService.launchRectificationAudit(context, auditCreateOptions.getObjectId());
            } else {
                auditOptions = updateAuditDslQuery(auditCreateOptions, thresholdOpt, context);
                if (AUDIT_FILE_CONSISTENCY.equals(auditOptions.getAuditActions())) {
                    operationCommonService.lauchEvidenceAudit(context, auditOptions.getQuery());
                } else {
                    operationCommonService.runAudit(context, mapper.valueToTree(auditOptions));
                }
            }
        } catch (AccessExternalClientServerException | VitamClientException | BadRequestException e) {
            throw new InternalServerException("Unable to run audit", e);
        }
    }

    public AuditOptions updateAuditDslQuery(
        AuditCreateOptions auditCreateOptions,
        Optional<Long> thresholdOpt,
        VitamContext context
    ) {
        SelectMultiQuery multiQuery = new SelectMultiQuery();
        AuditOptions auditOptions = new AuditOptions();
        auditOptions.setAuditType(auditCreateOptions.getAuditType());
        auditOptions.setAuditActions(auditCreateOptions.getAuditActions());
        auditOptions.setObjectId(auditCreateOptions.getObjectId());
        auditOptions.setAuditType("dsl");

        try {
            if (!"dsl".equals(auditOptions.getAuditType()) || null == thresholdOpt) {
                throw new InvalidCreateOperationException("Invalid audit query");
            }

            BooleanQuery and = and();
            if (!AUDIT_PERIMETER_INGEST_OPERATION_PERIOD.equals(auditCreateOptions.getAuditPerimeter())) {
                if (CollectionUtils.isNotEmpty(Arrays.stream(auditCreateOptions.getOriginatingAgencyIds()).toList())) {
                    and.add(QueryHelper.in(ORIGINATING_AGENCY, auditCreateOptions.getOriginatingAgencyIds()));
                }
                if (CollectionUtils.isNotEmpty(Arrays.stream(auditCreateOptions.getAttachmentPositionIds()).toList())) {
                    and.add(QueryHelper.in(ALL_UNIT_UPS, auditCreateOptions.getAttachmentPositionIds()));
                }
                if (CollectionUtils.isNotEmpty(Arrays.stream(auditCreateOptions.getIngestOperationIds()).toList())) {
                    and.add(QueryHelper.in(OPERATIONS, auditCreateOptions.getIngestOperationIds()));
                }

                if (StringUtils.isNotEmpty(auditCreateOptions.getStartDate())) {
                    String fullDate = auditCreateOptions.getStartDate().concat(START_TIME);
                    and.add(QueryHelper.gte(APPROXIMATE_CREATION_DATE, fullDate));
                }
                if (StringUtils.isNotEmpty(auditCreateOptions.getEndDate())) {
                    String fullDate = auditCreateOptions.getEndDate().concat(END_TIME);
                    and.add(QueryHelper.lte(APPROXIMATE_CREATION_DATE, fullDate));
                }

                multiQuery.setQuery(and);
            } else {
                if (StringUtils.isNotEmpty(auditCreateOptions.getStartDate())) {
                    String fullDate = auditCreateOptions.getStartDate().concat(START_TIME);
                    and.add(QueryHelper.gte(EV_DATE_TIME, fullDate));
                }
                if (StringUtils.isNotEmpty(auditCreateOptions.getEndDate())) {
                    String fullDate = auditCreateOptions.getEndDate().concat(END_TIME);
                    and.add(QueryHelper.lte(EV_DATE_TIME, fullDate));
                }
                and.add(QueryHelper.eq(EV_TYPE_PROC, INGEST));

                ObjectNode queryNode = JsonHandler.createObjectNode();
                ObjectNode projectionNode = JsonHandler.createObjectNode();
                ObjectNode fieldsNode = JsonHandler.createObjectNode();
                queryNode.put(DSL_QUERY, and.getCurrentQuery());

                fieldsNode.put(DSL_QUERY_EVID, 1);
                projectionNode.put(DSL_QUERY_FIELDS, fieldsNode);
                queryNode.put(DSL_QUERY_PROJECTION, projectionNode);

                LogbookOperationsResponseDto response = this.findAll(context, queryNode);
                String[] ingestIds = response
                    .getResults()
                    .stream()
                    .map(LogbookOperationModel::getId)
                    .toArray(String[]::new);

                BooleanQuery finalAnd = and();
                finalAnd.add(QueryHelper.in(OPERATIONS, ingestIds));
                multiQuery.setQuery(finalAnd);
            }
            auditOptions.setQuery(multiQuery.getFinalSelect());

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
        return auditOptions;
    }

    public Response export(VitamContext context, String id, ReportType type) {
        try {
            LOGGER.info("Export  Operations EvIdAppSession : {} ", context.getApplicationSessionId());
            switch (type) {
                case AUDIT:
                    return operationCommonService.exportAudit(context, id);
                case TRACEABILITY:
                    return operationCommonService.exportTraceability(context, id);
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
            operationCommonService.runProbativeValue(context, probativeValueRequest);
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to generate Probative value", e);
        }
    }

    public List<LogbookOperationDto> getAll(Optional<String> criteria) {
        VitamContext vitamContext = buildVitamContext();
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }
            query = VitamQueryHelper.createQueryDSL(vitamCriteria, null, null, Optional.empty(), Optional.empty());
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find LogbookOperations with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        return OperationConverter.convertVitamsToDtos(this.findAll(vitamContext, query).getResults());
    }

    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        VitamContext vitamContext = buildVitamContext();
        return this.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    public boolean runAudit(AuditCreateOptions auditOptions) {
        VitamContext vitamContext = buildVitamContext();
        this.runAudit(vitamContext, auditOptions);
        return true; // Suppose que l'opération est toujours réussie
    }

    public LogbookOperationsCommonResponseDto findHistoryById(String id) {
        VitamContext vitamContext = buildVitamContext();
        JsonNode history = this.findHistoryByIdentifier(vitamContext, id);
        try {
            return objectMapper.treeToValue(history, LogbookOperationsCommonResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Error parsing history data", e);
        }
    }

    public ResponseEntity<Resource> export(String id, ReportType type) {
        VitamContext vitamContext = buildVitamContext();

        Response response = this.export(vitamContext, id, type);
        Object entity = response.getEntity();
        if (entity instanceof InputStream stream) {
            Resource resource = new InputStreamResource(stream);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

    public JsonNode checkTraceabilityOperation(String id) {
        VitamContext vitamContext = buildVitamContext();
        return this.checkTraceabilityOperation(vitamContext, id);
    }

    public boolean runProbativeValue(ProbativeValueRequest probativeValueRequest) {
        VitamContext vitamContext = buildVitamContext();
        this.runProbativeValue(vitamContext, probativeValueRequest);
        return true; // Suppose que l'opération est toujours réussie
    }

    public ResponseEntity<Resource> exportProbativeValue(String operationId) throws PreconditionFailedException {
        VitamContext vitamContext = buildVitamContext();
        String tempFolder = "/tmp/" + operationId + ".zip";
        File zip = new File(tempFolder);
        try {
            FileOutputStream zipOutputStream = new FileOutputStream(zip);
            probativeValueService.exportReport(vitamContext, operationId, "/tmp", zipOutputStream);
            Resource resource = new InputStreamResource(new FileInputStream(zip.getAbsoluteFile()));
            return new ResponseEntity<>(resource, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            throw new InternalServerException("Error while generating probative value ZIP", e);
        }
    }
}
