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
import fr.gouv.vitamui.archive.internal.server.service.ExternalParametersService;
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
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
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
        "The tenant Id and the SearchCriteria are mandatory parameters: ";
    private static final String IDENTIFIER_MANDATORY =
        "The identifier are mandatory parameters: ";

    private final ArchiveSearchInternalService archiveInternalService;
    private final ArchiveSearchUnitExportCsvInternalService archiveSearchUnitExportCsvInternalService;
    private final ExportDipInternalService exportDipInternalService;
    private final TransferVitamOperationsInternalService transferVitamOperationsInternalService;
    private final ArchiveSearchEliminationInternalService archiveSearchEliminationInternalService;
    private final ArchiveSearchMgtRulesInternalService archiveSearchMgtRulesInternalService;
    private final InternalSecurityService securityService;
    private final ExternalParametersService externalParametersService;
    private final ObjectMapper objectMapper;


    @Autowired
    public ArchiveSearchInternalController(final ArchiveSearchInternalService archiveInternalService,
        final InternalSecurityService securityService,
        final ObjectMapper objectMapper,
        final ArchiveSearchUnitExportCsvInternalService archiveSearchUnitExportCsvInternalService,
        final ExportDipInternalService exportDipInternalService,
        TransferVitamOperationsInternalService transferVitamOperationsInternalService,
        final ArchiveSearchMgtRulesInternalService archiveSearchMgtRulesInternalService,
        final ArchiveSearchEliminationInternalService archiveSearchEliminationInternalService,
        final ExternalParametersService externalParametersService) {
        this.archiveInternalService = archiveInternalService;
        this.securityService = securityService;
        this.objectMapper = objectMapper;
        this.archiveSearchUnitExportCsvInternalService = archiveSearchUnitExportCsvInternalService;
        this.exportDipInternalService = exportDipInternalService;
        this.transferVitamOperationsInternalService = transferVitamOperationsInternalService;
        this.archiveSearchEliminationInternalService = archiveSearchEliminationInternalService;
        this.archiveSearchMgtRulesInternalService = archiveSearchMgtRulesInternalService;
        this.externalParametersService = externalParametersService;
    }

    @PostMapping(RestApi.SEARCH_PATH)
    public ArchiveUnitsDto searchArchiveUnitsByCriteria(
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, searchQuery);
        LOGGER.debug("Calling service searchArchiveUnits By Criteria {} ",
            searchQuery);
        return archiveInternalService.searchArchiveUnitsByCriteria(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(RestApi.FILING_HOLDING_SCHEME_PATH)
    public VitamUISearchResponseDto getFillingHoldingScheme()
        throws VitamClientException, IOException, PreconditionFailedException {
        LOGGER.debug("Get filing plan");
        return objectMapper.treeToValue(archiveInternalService.getFillingHoldingScheme(
                externalParametersService.buildVitamContextFromExternalParam()),
            VitamUISearchResponseDto.class);
    }

    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public ResultsDto findUnitById(final @PathVariable("id") String unitId)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker
            .checkParameter(IDENTIFIER_MANDATORY, unitId);
        SanityChecker.checkSecureParameter(unitId);
        LOGGER.debug("UA Details  by id {}", unitId);
        return archiveInternalService.findArchiveUnitById(unitId,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(RestApi.OBJECTGROUP + CommonConstants.PATH_ID)
    public ResultsDto findObjectById(final @PathVariable("id") String objectId)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker
            .checkParameter(IDENTIFIER_MANDATORY, objectId);
        SanityChecker.checkSecureParameter(objectId);
        LOGGER.debug("Get ObjectGroup By id : {}", objectId);
        return archiveInternalService.findObjectById(objectId,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(value = RestApi.DOWNLOAD_ARCHIVE_UNIT +
        CommonConstants.PATH_ID, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(final @PathVariable("id") String objectId,
        final @RequestParam("usage") String usage, final @RequestParam("version") Integer version
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker
            .checkParameter(IDENTIFIER_MANDATORY, objectId);
        SanityChecker.checkSecureParameter(objectId, usage);
        LOGGER.debug("Download Archive Unit Object with id {}", objectId);
        VitamContext vitamContext = new VitamContext(securityService.getTenantIdentifier()).setAccessContract(
                externalParametersService.retrieveAccessContractFromExternalParam())
            .setApplicationSessionId(securityService.getApplicationId());
        return Mono.<Resource>fromCallable(() -> {
                Response response = archiveInternalService.downloadObjectFromUnit(objectId, usage, version, vitamContext);
                return new InputStreamResource((InputStream) response.getEntity());
            }).subscribeOn(Schedulers.boundedElastic())
            .flatMap(resource -> Mono.just(ResponseEntity
                .ok().cacheControl(CacheControl.noCache())
                .body(resource)));
    }

    @PostMapping(RestApi.EXPORT_CSV_SEARCH_PATH)
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Export to CSV file Archive Units by criteria {}", searchQuery);
        Resource exportedResult =
            archiveSearchUnitExportCsvInternalService
                .exportToCsvSearchArchiveUnitsByCriteria(searchQuery,
                    externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(exportedResult, HttpStatus.OK);
    }

    @PostMapping(RestApi.EXPORT_DIP)
    public ResponseEntity<String> exportDIPByCriteria(@RequestBody final ExportDipCriteriaDto exportDipCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, exportDipCriteriaDto);
        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        LOGGER.debug("Export DIP  by criteria {}", exportDipCriteriaDto);
        String result = exportDipInternalService.requestToExportDIP(exportDipCriteriaDto,
            externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.TRANSFER_REQUEST)
    public ResponseEntity<String> transferRequest(@RequestBody final TransferRequestDto transferRequestDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(transferRequestDto);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, transferRequestDto);
        SanityChecker.sanitizeCriteria(transferRequestDto);
        LOGGER.debug("Transfer request {}", transferRequestDto);
        String result = transferVitamOperationsInternalService.transferRequest(transferRequestDto,
            externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.ELIMINATION_ANALYSIS)
    public JsonNode startEliminationAnalysis(
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Calling elimination analysis by criteria {} ", searchQuery);
        return archiveSearchEliminationInternalService.startEliminationAnalysis(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @PostMapping(RestApi.ELIMINATION_ACTION)
    public JsonNode startEliminationAction(@RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, searchQuery);
        LOGGER.debug("Calling elimination action by criteria {} ", searchQuery);
        return archiveSearchEliminationInternalService.startEliminationAction(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @PostMapping(RestApi.MASS_UPDATE_UNITS_RULES)
    public ResponseEntity<String> updateArchiveUnitsRules(
        @RequestBody final RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(ruleSearchCriteriaDto);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, ruleSearchCriteriaDto);
        LOGGER.debug("Update Archive Units Rules by criteria {}", ruleSearchCriteriaDto);
        String result = archiveSearchMgtRulesInternalService.updateArchiveUnitsRules(ruleSearchCriteriaDto,
            externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.COMPUTED_INHERITED_RULES)
    public ResponseEntity<String> computedInheritedRules(
        @RequestBody final SearchCriteriaDto searchCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchCriteriaDto);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, searchCriteriaDto);
        LOGGER.debug("Computed Inherited Rules  by criteria {}", searchCriteriaDto);
        String result = archiveInternalService.computedInheritedRules(searchCriteriaDto,
            externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.UNIT_WITH_INHERITED_RULES)
    public ResultsDto selectUnitsWithInheritedRules(@RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException, IOException {
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, searchQuery);
        LOGGER.debug("Calling service select Unit With Inherited Rules By Criteria {} ", searchQuery);
        return archiveInternalService.selectUnitWithInheritedRules(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @PostMapping(RestApi.RECLASSIFICATION)
    public ResponseEntity<String> reclassification(
        @RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(reclassificationCriteriaDto);
        ParameterChecker.checkParameter(MANDATORY_PARAMETERS, reclassificationCriteriaDto);
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        String result = archiveInternalService.reclassification(reclassificationCriteriaDto,
            externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public String updateUnitById(final @PathVariable("id") String unitId,
        @RequestBody final UnitDescriptiveMetadataDto unitDescriptiveMetadataDto)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY, unitId);
        ParameterChecker.checkParameter("The request body is mandatory: ", unitDescriptiveMetadataDto);
        SanityChecker.checkSecureParameter(unitId);
        LOGGER.debug("update archiveUnit id  {}", unitId);
        return archiveInternalService.updateUnitById(unitId, unitDescriptiveMetadataDto,
            externalParametersService.buildVitamContextFromExternalParam());
    }


    @ApiOperation(value = "Upload an ATR for transfer acknowledgment operation", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = RestApi.TRANSFER_ACKNOWLEDGMENT, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String transferAcknowledgment(InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws PreconditionFailedException, VitamClientException {

        LOGGER.debug("[INTERNAL] : Transfer Acknowledgment Operation");
        ParameterChecker.checkParameter("The fileName is mandatory parameter: ",
            originalFileName);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        LOGGER.debug("Transfer Acknowledgment : upload  atr xml filename: {}", originalFileName);
        return transferVitamOperationsInternalService.transferAcknowledgmentService(inputStream,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(CommonConstants.EXTERNAL_ONTOLOGIES_LIST)
    public List<VitamUiOntologyDto> getExternalOntologyFieldsList() throws IOException {
        LOGGER.debug("[INTERNAL] : Get All ontologies values for tenant {}",
            securityService.getTenantIdentifier());
        final Integer tenantId = securityService.getTenantIdentifier();
        return archiveInternalService.readExternalOntologiesFromFile(tenantId);
    }

    @GetMapping(RestApi.PERSISTENT_IDENTIFIER)
    public PersistentIdentifierResponseDto findByPersistentIdentifier(
        final @RequestParam("id") String arkId
    ) throws VitamClientException {
        LOGGER.debug("[INTERNAL] : Get by persistent identifier {}", arkId);
        PersistentIdentifierResponseDto persistentIdentifierResponse = archiveInternalService.findByPersitentIdentifier(arkId, externalParametersService.buildVitamContextFromExternalParam());
        LOGGER.debug("[INTERNAL] : persistentIdentifierResponse = {}", persistentIdentifierResponse);
        return persistentIdentifierResponse;
    }

}
