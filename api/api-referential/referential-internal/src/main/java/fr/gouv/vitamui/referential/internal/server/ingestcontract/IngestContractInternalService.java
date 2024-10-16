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
package fr.gouv.vitamui.referential.internal.server.ingestcontract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.dto.IngestContractResponseDto;
import fr.gouv.vitamui.referential.common.dto.SignaturePolicyDto;
import fr.gouv.vitamui.referential.common.service.IngestContractService;
import fr.gouv.vitamui.referential.internal.server.utils.ExportCSVUtils;
import fr.gouv.vitamui.referential.internal.server.utils.ImportCSVUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IngestContractInternalService {

    private static final String INGEST_CONTRACT = "INGEST_CONTRACT";

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestContractInternalService.class);

    private final IngestContractService ingestContractService;

    private final ObjectMapper objectMapper;

    private final IngestContractConverter converter;

    private final LogbookService logbookService;

    private final ApplicationInternalRestClient applicationInternalRestClient;

    private final InternalSecurityService internalSecurityService;

    @Autowired
    public IngestContractInternalService(
        IngestContractService ingestContractService,
        ObjectMapper objectMapper,
        IngestContractConverter converter,
        LogbookService logbookService,
        ApplicationInternalRestClient applicationInternalRestClient,
        InternalSecurityService internalSecurityService
    ) {
        this.ingestContractService = ingestContractService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
        this.applicationInternalRestClient = applicationInternalRestClient;
        this.internalSecurityService = internalSecurityService;
    }

    public IngestContractDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.info("Ingest Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<IngestContractModel> requestResponse = ingestContractService.findIngestContractById(
                vitamContext,
                identifier
            );
            final IngestContractResponseDto ingestContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                IngestContractResponseDto.class
            );
            if (ingestContractResponseDto.getResults().isEmpty()) {
                return null;
            } else {
                return converter.convertVitamToDto(ingestContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Ingest Contrat", e);
        }
    }

    public List<IngestContractDto> getAll(VitamContext vitamContext) {
        final RequestResponse<IngestContractModel> requestResponse;
        try {
            LOGGER.debug("All Ingest Contracts EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = ingestContractService.findIngestContracts(vitamContext, new Select().getFinalSelect());
            final IngestContractResponseDto ingestContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                IngestContractResponseDto.class
            );

            return converter.convertVitamsToDtos(ingestContractResponseDto.getResults());
        } catch (JsonProcessingException | VitamClientException e) {
            throw new InternalServerException("Unable to get Ingest Contrats", e);
        }
    }

    public PaginatedValuesDto<IngestContractDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        Map<String, Object> vitamCriteria = new HashMap<>();
        LOGGER.debug("All Ingest Contracts EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            JsonNode query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
            IngestContractResponseDto results = this.findAll(vitamContext, query);
            boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

            final List<IngestContractDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
            LOGGER.debug("Vitam UI DTO: {}", valuesDto);
            return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated ingest contracts", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }
    }

    public IngestContractResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        try {
            LOGGER.info("All Ingest Contracts EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            final RequestResponse<IngestContractModel> requestResponse = ingestContractService.findIngestContracts(
                vitamContext,
                query
            );
            LOGGER.debug("VITAM Response: {}", requestResponse.toJsonNode().toPrettyString());
            final IngestContractResponseDto ingestContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                IngestContractResponseDto.class
            );
            LOGGER.debug("VITAM DTO: {}", ingestContractResponseDto);

            return ingestContractResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find Ingest Contrats", e);
        }
    }

    public Boolean check(VitamContext vitamContext, IngestContractDto ingestContractDto) {
        try {
            LOGGER.debug("Ingest Contract Check EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            Integer ingestContractCheckedTenant = ingestContractService.checkAbilityToCreateIngestContractInVitam(
                converter.convertDtosToVitams(Arrays.asList(ingestContractDto)),
                vitamContext.getApplicationSessionId()
            );
            return !vitamContext.getTenantId().equals(ingestContractCheckedTenant);
        } catch (ConflictException e) {
            return true;
        }
    }

    public IngestContractDto create(VitamContext vitamContext, IngestContractDto ingestContractDto) {
        ingestContractService.checkAbilityToCreateIngestContractInVitam(
            converter.convertDtosToVitams(Arrays.asList(ingestContractDto)),
            vitamContext.getApplicationSessionId()
        );

        try {
            LOGGER.debug("Create Ingest Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());

            RequestResponse requestResponse = ingestContractService.createIngestContracts(
                vitamContext,
                converter.convertDtosToVitams(Arrays.asList(ingestContractDto))
            );
            final IngestContractModel ingestContractVitamDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                IngestContractModel.class
            );
            return converter.convertVitamToDto(ingestContractVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create ingest contract", e);
        }
    }

    private JsonNode convertMapPartialDtoToUpperCaseVitamFields(Map<String, Object> partialDto) {
        ObjectNode propertiesToUpdate = JsonHandler.createObjectNode();

        // Transform Vitam-UI fields into Vitam fields
        if (partialDto.get("name") != null) {
            propertiesToUpdate.put("Name", (String) partialDto.get("name"));
        }
        if (partialDto.get("description") != null) {
            propertiesToUpdate.put("Description", (String) partialDto.get("description"));
        }
        if (partialDto.get("checkParentLink") != null) {
            propertiesToUpdate.put("CheckParentLink", (String) partialDto.get("checkParentLink"));
        }
        if (partialDto.get("linkParentId") != null) {
            propertiesToUpdate.put("LinkParentId", (String) partialDto.get("linkParentId"));
        }
        if (partialDto.get("managementContractId") != null) {
            propertiesToUpdate.put("ManagementContractId", (String) partialDto.get("managementContractId"));
        }
        if (partialDto.get("status") != null) {
            propertiesToUpdate.put("Status", (String) partialDto.get("status"));
        }
        if (partialDto.get("activationDate") != null) {
            propertiesToUpdate.put("ActivationDate", (String) partialDto.get("activationDate"));
        }
        if (partialDto.get("deactivationDate") != null) {
            propertiesToUpdate.put("DeactivationDate", (String) partialDto.get("deactivationDate"));
        }
        if (partialDto.get("lastUpdate") != null) {
            propertiesToUpdate.put("LastUpdate", (String) partialDto.get("lastUpdate"));
        }
        if (partialDto.get("creationDate") != null) {
            propertiesToUpdate.put("CreationDate", (String) partialDto.get("creationDate"));
        }
        if (partialDto.get("masterMandatory") != null) {
            propertiesToUpdate.put("MasterMandatory", (boolean) partialDto.get("masterMandatory"));
        }
        if (partialDto.get("formatUnidentifiedAuthorized") != null) {
            propertiesToUpdate.put(
                "FormatUnidentifiedAuthorized",
                (boolean) partialDto.get("formatUnidentifiedAuthorized")
            );
        }
        if (partialDto.get("everyFormatType") != null) {
            propertiesToUpdate.put("EveryFormatType", (boolean) partialDto.get("everyFormatType"));
        }
        if (partialDto.get("everyDataObjectVersion") != null) {
            propertiesToUpdate.put("EveryDataObjectVersion", (boolean) partialDto.get("everyDataObjectVersion"));
        }

        if (partialDto.get("checkParentId") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value : (List<String>) partialDto.get("checkParentId")) {
                array.add(value);
            }
            propertiesToUpdate.set("CheckParentId", array);
        }
        if (partialDto.get("formatType") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value : (List<String>) partialDto.get("formatType")) {
                array.add(value);
            }
            propertiesToUpdate.set("FormatType", array);
        }
        if (partialDto.get("archiveProfiles") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value : (List<String>) partialDto.get("archiveProfiles")) {
                array.add(value);
            }
            propertiesToUpdate.set("ArchiveProfiles", array);
        }
        if (partialDto.get("dataObjectVersion") != null) {
            ArrayNode array = JsonHandler.createArrayNode();
            for (String value : (List<String>) partialDto.get("dataObjectVersion")) {
                array.add(value);
            }
            propertiesToUpdate.set("DataObjectVersion", array);
        }
        if (partialDto.get("computeInheritedRulesAtIngest") != null) {
            propertiesToUpdate.put(
                "ComputeInheritedRulesAtIngest",
                (boolean) partialDto.get("computeInheritedRulesAtIngest")
            );
        }
        if (partialDto.get("signaturePolicy") != null) {
            Map<String, Object> map = (Map<String, Object>) partialDto.get("signaturePolicy");
            map = map
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> StringUtils.capitalize(entry.getKey()), Map.Entry::getValue));
            JsonNode signaturePolicy = objectMapper.valueToTree(map);
            propertiesToUpdate.set("SignaturePolicy", signaturePolicy);
        }
        return propertiesToUpdate;
    }

    public IngestContractDto patch(VitamContext vitamContext, final Map<String, Object> partialDto) {
        String id = (String) partialDto.get("identifier");
        if (id == null) {
            throw new BadRequestException("id must be one the the update criteria");
        }
        partialDto.remove("id");
        partialDto.remove("identifier");

        try {
            LOGGER.debug("Patch Ingest Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            // Fix because Vitam doesn't allow String Array as action value (transformed to a string representation"[value1, value2]"
            JsonNode fieldsUpdated = convertMapPartialDtoToUpperCaseVitamFields(partialDto);

            ArrayNode actions = JsonHandler.createArrayNode();
            if (partialDto.get("managementContractId") == null) {
                ObjectNode unsetAction = JsonNodeFactory.instance.objectNode();
                ArrayNode unsetArray = JsonNodeFactory.instance.arrayNode();
                unsetArray.add("ManagementContractId");
                unsetAction.set("$unset", unsetArray);
                actions.add(unsetAction);
            }

            if (!fieldsUpdated.isEmpty()) {
                ObjectNode action = JsonHandler.createObjectNode();
                action.set("$set", fieldsUpdated);
                actions.add(action);
            }

            ObjectNode query = JsonHandler.createObjectNode();
            query.set("$action", actions);

            LOGGER.debug("Send IngestContract update request: {}", query);

            RequestResponse<?> requestResponse = ingestContractService.patchIngestContract(vitamContext, id, query);
            if (Response.Status.OK.getStatusCode() != requestResponse.getHttpCode()) {
                throw new AccessExternalClientException("Can't patch ingest contract");
            }
            return getOne(vitamContext, id);
        } catch (InvalidParseOperationException | AccessExternalClientException e) {
            throw new InternalServerException("Can't patch ingest contract", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        try {
            LOGGER.info("Ingest Contract History EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id), vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public ResponseEntity<Void> importIngestContracts(VitamContext vitamContext, MultipartFile file) {
        Boolean isIdentifierMandatory = applicationInternalRestClient
            .isApplicationExternalIdentifierEnabled(internalSecurityService.getHttpContext(), INGEST_CONTRACT)
            .getBody();

        if (isIdentifierMandatory == null) {
            throw new InternalServerException("The result of the API call should not be null");
        }

        IngestContractCSVUtils.checkImportFile(file, isIdentifierMandatory);
        LOGGER.debug("ingest contracts file {} has been validated before parsing it", file.getOriginalFilename());

        List<IngestContractDto> ingestContractDtos = convertCsvFileToIngestContractsDto(file);
        LOGGER.debug("ingest contracts file {} has been parsed in accessContract List", file.getOriginalFilename());

        RequestResponse<?> result;

        try {
            result = ingestContractService.createIngestContracts(
                vitamContext,
                converter.convertDtosToVitams(ingestContractDtos)
            );
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create ingest contracts", e);
        }

        if (HttpStatus.OK.value() == result.getHttpCode()) {
            LOGGER.debug("ingest contracts file {} has been successfully import to VITAM", file.getOriginalFilename());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        throw new BadRequestException(
            "The CSV file has been rejected by vitam",
            null,
            List.of(
                ImportCSVUtils.errorToJson(
                    ErrorImportFile.builder()
                        .error(ErrorImportFileMessage.REJECT_BY_VITAM_CHECK_LOGBOOK_OPERATION_APP)
                        .build()
                )
            )
        );
    }

    private List<IngestContractDto> convertCsvFileToIngestContractsDto(MultipartFile file) {
        try (Reader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)) {
            CsvToBean<IngestContractDto> csvToBean = new CsvToBeanBuilder<IngestContractDto>(reader)
                .withType(IngestContractDto.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                .withSeparator(';')
                .build();

            return csvToBean.parse();
        } catch (RuntimeException | IOException e) {
            throw new BadRequestException("Unable to read access contracts CSV file ", e);
        }
    }

    public Resource exportIngestContracts(VitamContext vitamContext) {
        final List<IngestContractDto> ingestContracts = getAll(vitamContext);

        ExportIngestContracts exporter = new ExportIngestContracts();

        final List<String[]> csvLines = new ArrayList<>();

        // headers
        csvLines.add(exporter.getHeaders().toArray(new String[exporter.getSize()]));

        SimpleDateFormat dateFormat = new SimpleDateFormat(exporter.getPatternDate());

        // rows
        ingestContracts.forEach(ingestContract -> {
            try {
                csvLines.add(buildIngestContractExportValues(ingestContract, exporter.getArrayJoinStr(), dateFormat));
            } catch (ParseException e) {
                throw new BadRequestException("Unable to parse ingest contract to a csv line", e);
            }
        });

        return ExportCSVUtils.generateCSVFile(csvLines, exporter.getSeparator());
    }

    private String[] buildIngestContractExportValues(
        final IngestContractDto ingestContract,
        final String arrayJoinStr,
        final DateFormat df
    ) throws ParseException {
        final String archiveProfiles = ingestContract.getArchiveProfiles() == null
            ? null
            : String.join(arrayJoinStr, ingestContract.getArchiveProfiles());
        final String checkParentId = ingestContract.getCheckParentId() == null
            ? null
            : String.join(arrayJoinStr, ingestContract.getCheckParentId());
        final String dataObjectVersion = ingestContract.getDataObjectVersion() == null
            ? null
            : String.join(arrayJoinStr, ingestContract.getDataObjectVersion());
        final String formatType = ingestContract.getFormatType() == null
            ? null
            : String.join(arrayJoinStr, ingestContract.getFormatType());

        final var activationDate = ingestContract.getActivationDate() == null
            ? null
            : df.format(LocalDateUtil.getDate(ingestContract.getActivationDate()));
        final var deactivationDate = ingestContract.getDeactivationDate() == null
            ? null
            : df.format(LocalDateUtil.getDate(ingestContract.getDeactivationDate()));
        final SignaturePolicyDto signaturePolicyDto = Optional.ofNullable(ingestContract.getSignaturePolicy()).orElse(
            new SignaturePolicyDto()
        );

        return new String[] {
            ingestContract.getIdentifier(),
            ingestContract.getName(),
            ingestContract.getDescription(),
            String.valueOf(ingestContract.getStatus()),
            archiveProfiles,
            ingestContract.getCheckParentLink(),
            checkParentId,
            ingestContract.getLinkParentId(),
            String.valueOf(ingestContract.isFormatUnidentifiedAuthorized()),
            String.valueOf(ingestContract.isEveryFormatType()),
            formatType,
            ingestContract.getManagementContractId(),
            String.valueOf(ingestContract.isComputeInheritedRulesAtIngest()),
            String.valueOf(ingestContract.isMasterMandatory()),
            String.valueOf(ingestContract.isEveryDataObjectVersion()),
            dataObjectVersion,
            signaturePolicyDto.getSignedDocument().name(),
            signaturePolicyDto.getSigningRole(),
            activationDate,
            deactivationDate,
        };
    }
}
