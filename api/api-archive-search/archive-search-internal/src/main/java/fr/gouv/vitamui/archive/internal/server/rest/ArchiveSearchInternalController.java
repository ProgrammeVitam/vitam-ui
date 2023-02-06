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
 */
package fr.gouv.vitamui.archive.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchEliminationInternalService;
import fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchInternalService;
import fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchMgtRulesInternalService;
import fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchUnitExportCsvInternalService;
import fr.gouv.vitamui.archive.internal.server.service.ExportDipInternalService;
import fr.gouv.vitamui.archive.internal.server.service.TransferVitamOperationsInternalService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.dtos.OntologyDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping(RestApi.ARCHIVE_SEARCH_PATH)
@Getter
@Setter
@Api(tags = "archives search", value = "Archives units search")
public class ArchiveSearchInternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalController.class);

    private static final String MANDATORY_PARAMETERS =
        "The tenant Id, the accessContract Id and the SearchCriteria are mandatory parameters: ";
    private static final String IDENTIFIER_ACCESS_CONTRACT_MANDATORY =
        "The identifier, the accessContract Id  are mandatory parameters: ";

    private final ArchiveSearchInternalService archiveInternalService;
    private final ArchiveSearchUnitExportCsvInternalService archiveSearchUnitExportCsvInternalService;
    private final ExportDipInternalService exportDipInternalService;
    private final TransferVitamOperationsInternalService transferVitamOperationsInternalService;
    private final ArchiveSearchEliminationInternalService archiveSearchEliminationInternalService;
    private final ArchiveSearchMgtRulesInternalService archiveSearchMgtRulesInternalService;
    private final InternalSecurityService securityService;
    private final ObjectMapper objectMapper;


    @Autowired
    public ArchiveSearchInternalController(final ArchiveSearchInternalService archiveInternalService,
        final InternalSecurityService securityService,
        final ObjectMapper objectMapper,
        final ArchiveSearchUnitExportCsvInternalService archiveSearchUnitExportCsvInternalService,
        final ExportDipInternalService exportDipInternalService,
        TransferVitamOperationsInternalService transferVitamOperationsInternalService,
        final ArchiveSearchMgtRulesInternalService archiveSearchMgtRulesInternalService,
        final ArchiveSearchEliminationInternalService archiveSearchEliminationInternalService) {
        this.archiveInternalService = archiveInternalService;
        this.securityService = securityService;
        this.objectMapper = objectMapper;
        this.archiveSearchUnitExportCsvInternalService = archiveSearchUnitExportCsvInternalService;
        this.exportDipInternalService = exportDipInternalService;
        this.transferVitamOperationsInternalService = transferVitamOperationsInternalService;
        this.archiveSearchEliminationInternalService = archiveSearchEliminationInternalService;
        this.archiveSearchMgtRulesInternalService = archiveSearchMgtRulesInternalService;
    }

    @PostMapping(RestApi.SEARCH_PATH)
    public ArchiveUnitsDto searchArchiveUnitsByCriteria(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter(MANDATORY_PARAMETERS,
                tenantId, accessContractId, searchQuery);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug("Calling service searchArchiveUnits for tenantId {}, accessContractId {} By Criteria {} ",
            tenantId,
            accessContractId, searchQuery);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return archiveInternalService.searchArchiveUnitsByCriteria(searchQuery, vitamContext);
    }

    @GetMapping(RestApi.FILING_HOLDING_SCHEME_PATH)
    public VitamUISearchResponseDto getFillingHoldingScheme(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
        throws VitamClientException, IOException, InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug("Get filing plan");
        ParameterChecker.checkParameter("The tenant Id, the accessContract Id  are mandatory parameters: ", tenantId,
            accessContractId);
        SanityChecker.checkSecureParameter(accessContractId);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return objectMapper.treeToValue(archiveInternalService.getFillingHoldingScheme(vitamContext),
            VitamUISearchResponseDto.class);
    }

    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public ResultsDto findUnitById(final @PathVariable("id") String id,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker
            .checkParameter(IDENTIFIER_ACCESS_CONTRACT_MANDATORY, id, accessContractId);
        SanityChecker.checkSecureParameter(id, accessContractId);
        LOGGER.debug("UA Details  {}", id);
        VitamContext vitamContext =
            securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        return archiveInternalService.findArchiveUnitById(id, vitamContext);
    }

    @GetMapping(RestApi.OBJECTGROUP + CommonConstants.PATH_ID)
    public ResultsDto findObjectById(final @PathVariable("id") String id,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker
            .checkParameter(IDENTIFIER_ACCESS_CONTRACT_MANDATORY, id, accessContractId);
        SanityChecker.checkSecureParameter(id, accessContractId);
        LOGGER.debug("Get ObjectGroup By id : {}", id);
        VitamContext vitamContext =
            securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        return archiveInternalService.findObjectById(id, vitamContext);
    }

    @GetMapping(value = RestApi.DOWNLOAD_ARCHIVE_UNIT +
        CommonConstants.PATH_ID, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(final @PathVariable("id") String id,
        final @RequestParam("usage") String usage, final @RequestParam("version") Integer version,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId
    ) throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker
            .checkParameter(IDENTIFIER_ACCESS_CONTRACT_MANDATORY, id, accessContractId);
        SanityChecker.checkSecureParameter(id, accessContractId, usage);
        LOGGER.debug("Access Contract {} ", accessContractId);
        LOGGER.debug("Download Archive Unit Object with id {}", id);
        final VitamContext vitamContext =
            securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        return Mono.<Resource>fromCallable(() -> {
                Response response = archiveInternalService.downloadObjectFromUnit(id, usage, version, vitamContext);
                return new InputStreamResource((InputStream) response.getEntity());
            }).subscribeOn(Schedulers.boundedElastic())
            .flatMap(resource -> Mono.just(ResponseEntity
                .ok().cacheControl(CacheControl.noCache())
                .body(resource)));
    }

    @PostMapping(RestApi.EXPORT_CSV_SEARCH_PATH)
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(accessContractId);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Export to CSV file Archive Units by criteria {}", searchQuery);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        Resource exportedResult =
            archiveSearchUnitExportCsvInternalService
                .exportToCsvSearchArchiveUnitsByCriteria(searchQuery, vitamContext);
        return new ResponseEntity<>(exportedResult, HttpStatus.OK);
    }

    @PostMapping(RestApi.EXPORT_DIP)
    public ResponseEntity<String> exportDIPByCriteria(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final ExportDipCriteriaDto exportDipCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        ParameterChecker
            .checkParameter(
                MANDATORY_PARAMETERS,
                tenantId, accessContractId, exportDipCriteriaDto);
        SanityChecker.checkSecureParameter(accessContractId);
        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        LOGGER.debug("Export DIP  by criteria {}", exportDipCriteriaDto);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        String result = exportDipInternalService.requestToExportDIP(exportDipCriteriaDto, vitamContext);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.TRANSFER_REQUEST)
    public ResponseEntity<String> transferRequest(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final TransferRequestDto transferRequestDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(transferRequestDto);
        ParameterChecker.checkParameter(
            MANDATORY_PARAMETERS,
            tenantId, accessContractId, transferRequestDto);
        SanityChecker.checkSecureParameter(accessContractId);
        SanityChecker.sanitizeCriteria(transferRequestDto);
        LOGGER.debug("Transfer request {}", transferRequestDto);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        String result = transferVitamOperationsInternalService.transferRequest(transferRequestDto, vitamContext);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.ELIMINATION_ANALYSIS)
    public JsonNode startEliminationAnalysis(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter(
                MANDATORY_PARAMETERS,
                tenantId, accessContractId, searchQuery);
        SanityChecker.checkSecureParameter(accessContractId);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Calling elimination analysis by criteria {} ", searchQuery);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return archiveSearchEliminationInternalService.startEliminationAnalysis(searchQuery, vitamContext);
    }

    @PostMapping(RestApi.ELIMINATION_ACTION)
    public JsonNode startEliminationAction(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter(
                MANDATORY_PARAMETERS,
                tenantId, accessContractId, searchQuery);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug("Calling elimination action by criteria {} ", searchQuery);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return archiveSearchEliminationInternalService.startEliminationAction(searchQuery, vitamContext);
    }

    @PostMapping(RestApi.MASS_UPDATE_UNITS_RULES)
    public ResponseEntity<String> updateArchiveUnitsRules(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(ruleSearchCriteriaDto);
        ParameterChecker
            .checkParameter(MANDATORY_PARAMETERS,
                tenantId, accessContractId, ruleSearchCriteriaDto);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug("Update Archive Units Rules by criteria {}", ruleSearchCriteriaDto);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        String result =
            archiveSearchMgtRulesInternalService.updateArchiveUnitsRules(vitamContext, ruleSearchCriteriaDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.COMPUTED_INHERITED_RULES)
    public ResponseEntity<String> computedInheritedRules(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchCriteriaDto);
        ParameterChecker
            .checkParameter(
                MANDATORY_PARAMETERS,
                tenantId, accessContractId, searchCriteriaDto);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug("Computed Inherited Rules  by criteria {}", searchCriteriaDto);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        String result = archiveInternalService.computedInheritedRules(vitamContext, searchCriteriaDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.UNIT_WITH_INHERITED_RULES)
    public ResultsDto selectUnitsWithInheritedRules(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter(MANDATORY_PARAMETERS,
                tenantId, accessContractId, searchQuery);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug(
            "Calling service select Unit With Inherited Rules for tenantId {}, accessContractId {} By Criteria {} ",
            tenantId,
            accessContractId, searchQuery);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return archiveInternalService.selectUnitWithInheritedRules(searchQuery, vitamContext);
    }

    @PostMapping(RestApi.RECLASSIFICATION)
    public ResponseEntity<String> reclassification(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(reclassificationCriteriaDto);
        ParameterChecker
            .checkParameter(MANDATORY_PARAMETERS,
                tenantId, accessContractId, reclassificationCriteriaDto);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        String result = archiveInternalService.reclassification(vitamContext, reclassificationCriteriaDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public String updateUnitById(final @PathVariable("id") String id,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final UnitDescriptiveMetadataDto unitDescriptiveMetadataDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker
            .checkParameter(IDENTIFIER_ACCESS_CONTRACT_MANDATORY, id, accessContractId);
        ParameterChecker
            .checkParameter("The request body is mandatory: ", unitDescriptiveMetadataDto);
        SanityChecker.checkSecureParameter(accessContractId, id);
        LOGGER.debug("update archiveUnit id  {}", id);
        VitamContext vitamContext =
            securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        return archiveInternalService.updateUnitById(id, unitDescriptiveMetadataDto, vitamContext);
    }


    @ApiOperation(value = "Upload an ATR for transfer acknowledgment operation", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = RestApi.TRANSFER_ACKNOWLEDGMENT, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String transferAcknowledgment(
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    )
        throws PreconditionFailedException, InvalidParseOperationException,
        VitamClientException {

        LOGGER.debug("[INTERNAL] : Transfer Acknowledgment Operation");
        ParameterChecker.checkParameter("The access contract and the fileName are mandatory parameters: ",
            accessContractId,
            originalFileName);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.debug("Transfer Acknowledgment : upload  atr xml filename: {}", originalFileName);

        VitamContext vitamContext =
            securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);

        return transferVitamOperationsInternalService.transferAcknowledgmentService(inputStream, vitamContext);
    }

    @GetMapping(CommonConstants.EXTERNAL_ONTOLOGIES_LIST)
    public List<OntologyDto> getExternalOntologiesList() throws IOException {
        LOGGER.debug("[INTERNAL] : Get All ontologies values for tenant {}",
            securityService.getTenantIdentifier());
        final Integer tenantId = securityService.getTenantIdentifier();
        return archiveInternalService.readExternalOntologiesFromFile(tenantId);
    }

}
