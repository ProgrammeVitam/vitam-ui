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
package fr.gouv.vitamui.referential.internal.server.accesscontract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.AccessContractDto;
import fr.gouv.vitamui.referential.common.dto.AccessContractResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamUIAccessContractService;
import fr.gouv.vitamui.referential.internal.server.utils.ExportCSVUtils;
import fr.gouv.vitamui.referential.internal.server.utils.ImportCSVUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AccessContractInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessContractInternalService.class);

    private static final String ACCESS_CONTRACT = "ACCESS_CONTRACT";

    private final AccessContractService accessContractService;

    private final VitamUIAccessContractService vitamUIAccessContractService;

    private final ObjectMapper objectMapper;

    private final AccessContractConverter converter;

    private final LogbookService logbookService;

    private final ApplicationInternalRestClient applicationInternalRestClient;

    private final InternalSecurityService internalSecurityService;

    @Autowired
    public AccessContractInternalService(AccessContractService accessContractService,
        VitamUIAccessContractService vitamUIAccessContractService, ObjectMapper objectMapper,
        AccessContractConverter converter, LogbookService logbookService, ApplicationInternalRestClient applicationInternalRestClient, InternalSecurityService internalSecurityService) {
        this.accessContractService = accessContractService;
        this.vitamUIAccessContractService = vitamUIAccessContractService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
        this.applicationInternalRestClient = applicationInternalRestClient;
        this.internalSecurityService = internalSecurityService;
    }

    public AccessContractDto getOne(VitamContext vitamContext, String identifier) {
        try {

            LOGGER.debug("Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<AccessContractModel> requestResponse =
                accessContractService.findAccessContractById(vitamContext, identifier);
            final AccessContractResponseDto accessContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);
            if (accessContractResponseDto.getResults().isEmpty()) {
                return null;
            } else {
                return converter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Access Contract", e);
        }
    }

    public List<AccessContractDto> getAll(VitamContext vitamContext) {
        final RequestResponse<AccessContractModel> requestResponse;
        try {

            LOGGER.debug("List of Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = accessContractService
                .findAccessContracts(vitamContext, new Select().getFinalSelect());
            final AccessContractResponseDto accessContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);

            return converter.convertVitamsToDtos(accessContractResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Access Contracts", e);
        }
    }

    public PaginatedValuesDto<AccessContractDto> getAllPaginated(final Integer pageNumber, final Integer size,
        final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
        Optional<String> criteria) {

        Map<String, Object> vitamCriteria = new HashMap<>();
        try {
            LOGGER.debug("List of Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {
                };
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            JsonNode query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
            LOGGER.debug("jsonQuery: {}", query);

            AccessContractResponseDto results = this.findAll(vitamContext, query);
            boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

            final List<AccessContractDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
            return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);

        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated access contracts", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }
    }

    public AccessContractResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<AccessContractModel> requestResponse;
        try {
            LOGGER.debug("List of Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = accessContractService.findAccessContracts(vitamContext, query);
            return objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);

        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Can't find access contracts", e);
        }
    }

    public Boolean check(VitamContext vitamContext, AccessContractDto accessContractDto) {
        try {
            LOGGER.debug("Access Contract Check EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            Integer accessContractCheckedTenant = accessContractService.checkAbilityToCreateAccessContractInVitam(
                converter.convertDtosToVitams(Arrays.asList(accessContractDto)),
                vitamContext.getApplicationSessionId());
            return !vitamContext.getTenantId().equals(accessContractCheckedTenant);
        } catch (ConflictException e) {
            return true;
        }
    }

    public AccessContractDto create(VitamContext vitamContext, AccessContractDto accessContractDto) {

        LOGGER.debug("Creating Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        RequestResponse requestResponse;
        try {
            requestResponse = accessContractService.createAccessContracts(vitamContext, converter.convertDtosToVitams(List.of(accessContractDto)));
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create access contract", e);
        }

        if (requestResponse == null || HttpStatus.OK.value() != requestResponse.getHttpCode()) {
            throw new BadRequestException("Could not create access contract in vitam");
        }

        // Vitam does not return any AccessContractDto so we return the given one successfully created
        return accessContractDto;
    }

    public AccessContractDto patch(VitamContext vitamContext, final Map<String, Object> partialDto) {
        String id = (String) partialDto.get("identifier");
        if (id == null) {
            throw new BadRequestException("id must be one the the update criteria");
        }
        partialDto.remove("id");
        partialDto.remove("identifier");

        try {
            LOGGER.debug("Patch Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            // Fix because Vitam doesn't allow String Array as action value (transformed to a string representation"[value1, value2]"
            // Manual setting instead of updateRequest.addActions( UpdateActionHelper.set(fieldsUpdated));
            JsonNode fieldsUpdated = converter.convertToUpperCaseFields(partialDto);

            ObjectNode action = JsonHandler.createObjectNode();
            action.set("$set", fieldsUpdated);

            ArrayNode actions = JsonHandler.createArrayNode();
            actions.add(action);

            ObjectNode query = JsonHandler.createObjectNode();
            query.set("$action", actions);

            LOGGER.debug("Send AccessContract update request: {}", query);

            vitamUIAccessContractService.patchAccessContract(vitamContext, id, query);
            return getOne(vitamContext, id);
        } catch (InvalidParseOperationException | AccessExternalClientException e) {
            throw new InternalServerException("Can't patch access contract", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        LOGGER.debug("Find History Access Contract By ID {}, EvIdAppSession : {}", id, vitamContext.getApplicationSessionId());

        try {
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id), vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public ResponseEntity<Void> importAccessContracts(VitamContext context, MultipartFile file) {

        Boolean isIdentifierMandatory = applicationInternalRestClient.isApplicationExternalIdentifierEnabled(internalSecurityService.getHttpContext(), ACCESS_CONTRACT).getBody();

        if (isIdentifierMandatory == null) {
            throw new InternalServerException("The result of the API call should not be null");
        }

        AccessContractCSVUtils.checkImportFile(file, isIdentifierMandatory);
        LOGGER.debug("access contracts file {} has been validated before parsing it", file.getOriginalFilename());

        List<AccessContractCSVDto> accessContract = convertCsvFileToAccessContractsDto(file);
        LOGGER.debug("access contracts file {} has been parsed in accessContract List", file.getOriginalFilename());

        Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();
        String jsonString = gson.toJson(accessContract);
        LOGGER.debug("access contracts file {} has been parsed in JSON String", file.getOriginalFilename());

        RequestResponse<?> result;
        try {
            result = accessContractService.createAccessContracts(context, new ByteArrayInputStream(jsonString.getBytes()));
            LOGGER.debug("access contracts file {} has been send to VITAM", file.getOriginalFilename());
        } catch (InvalidParseOperationException | AccessExternalClientException e) {
            throw new InternalServerException("Unable to import access contracts file " + file.getOriginalFilename() + " : ", e);
        }

        if (HttpStatus.OK.value() == result.getHttpCode()) {
            LOGGER.debug("access contracts file {} has been successfully import to VITAM", file.getOriginalFilename());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        throw new BadRequestException("The CSV file has been rejected by vitam", null, List.of(ImportCSVUtils.errorToJson(ErrorImportFile.builder().error(ErrorImportFileMessage.REJECT_BY_VITAM_CHECK_LOGBOOK_OPERATION_APP).build())));

    }

    private List<AccessContractCSVDto> convertCsvFileToAccessContractsDto(MultipartFile file) {
        try (Reader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)) {
            CsvToBean<AccessContractCSVDto> csvToBean = new CsvToBeanBuilder<AccessContractCSVDto>(reader)
                .withType(AccessContractCSVDto.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                .withSeparator(';')
                .build();

            return csvToBean.parse();
        } catch (RuntimeException | IOException e) {
            throw new BadRequestException("Unable to read access contracts CSV file " + file.getOriginalFilename(), e);
        }
    }

    public Resource exportAccessContracts(VitamContext vitamContext) {

        final List<AccessContractDto> accessContracts = getAll(vitamContext);

        ExportAccessContracts exporter = new ExportAccessContracts();

        final List<String[]> csvLines = new ArrayList<>();

        // headers
        csvLines.add(exporter.getHeaders().toArray(new String[exporter.getSize()]));

        SimpleDateFormat dateFormat = new SimpleDateFormat(exporter.getPatternDate());

        // rows
        accessContracts.forEach(accessContract -> {
            try {
                csvLines.add(buildAccessContractExportValues(accessContract, exporter.getArrayJoinStr(), dateFormat));
            } catch (ParseException e) {
                throw new BadRequestException("Unable to parse access contract to a csv line", e);
            }
        });

        return ExportCSVUtils.generateCSVFile(csvLines, exporter.getSeparator());
    }

    private String[] buildAccessContractExportValues(final AccessContractDto accessContract, final String arrayJoinStr, final DateFormat df) throws ParseException {
        final String originatingAgencies = accessContract.getOriginatingAgencies() == null ? null : String.join(arrayJoinStr, accessContract.getOriginatingAgencies());
        final String dataObjectVersions = accessContract.getDataObjectVersion() == null ? null : String.join(arrayJoinStr, accessContract.getDataObjectVersion());
        final String rootUnits = accessContract.getRootUnits() == null ? null : String.join(arrayJoinStr, accessContract.getRootUnits());
        final String excludedRootUnits = accessContract.getExcludedRootUnits() == null ? null : String.join(arrayJoinStr, accessContract.getExcludedRootUnits());
        final String ruleCategoryToFilter = accessContract.getRuleCategoryToFilter() == null ? null : String.join(arrayJoinStr, accessContract.getRuleCategoryToFilter());

        final var creationDate = accessContract.getCreationDate() == null ? null : df.format(LocalDateUtil.getDate(accessContract.getCreationDate()));
        final var lastUpdateDate = accessContract.getLastUpdate() == null ? null : df.format(LocalDateUtil.getDate(accessContract.getLastUpdate()));
        final var activationDate = accessContract.getActivationDate() == null ? null : df.format(LocalDateUtil.getDate(accessContract.getActivationDate()));
        final var deactivationDate = accessContract.getDeactivationDate() == null ? null : df.format(LocalDateUtil.getDate(accessContract.getDeactivationDate()));

        return new String[]{
            accessContract.getIdentifier(),
            accessContract.getName(),
            accessContract.getDescription(),
            accessContract.getStatus(),
            accessContract.getWritingPermission().toString(),
            accessContract.getEveryOriginatingAgency().toString(),
            originatingAgencies,
            accessContract.getEveryDataObjectVersion().toString(),
            dataObjectVersions,
            rootUnits,
            excludedRootUnits,
            accessContract.getAccessLog(),
            ruleCategoryToFilter,
            accessContract.getWritingRestrictedDesc().toString(),
            creationDate,
            lastUpdateDate,
            activationDate,
            deactivationDate
        };
    }
}
