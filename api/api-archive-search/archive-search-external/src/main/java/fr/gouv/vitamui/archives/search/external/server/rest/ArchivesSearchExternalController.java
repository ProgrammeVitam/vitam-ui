/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archives.search.external.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.external.server.service.ArchiveSearchEliminationService;
import fr.gouv.vitamui.archives.search.external.server.service.ArchiveSearchMgtRulesService;
import fr.gouv.vitamui.archives.search.external.server.service.ArchiveSearchService;
import fr.gouv.vitamui.archives.search.external.server.service.ArchiveSearchUnitExportCsvService;
import fr.gouv.vitamui.archives.search.external.server.service.ExportDipService;
import fr.gouv.vitamui.archives.search.external.server.service.TransferVitamOperationsService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * UI Archive-Search External controller
 */
@Api(tags = "Archives search")
@RequestMapping(RestApi.ARCHIVE_SEARCH_PATH)
@RestController
@ResponseBody
public class ArchivesSearchExternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivesSearchExternalController.class);

    private static final String MANDATORY_QUERY = "The query is a mandatory parameter: ";
    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    private final ArchiveSearchService archiveSearchService;
    private final ArchiveSearchUnitExportCsvService archiveSearchUnitExportCsvService;
    private final ExportDipService exportDipService;
    private final TransferVitamOperationsService transferVitamOperationsService;
    private final ArchiveSearchEliminationService archiveSearchEliminationService;
    private final ArchiveSearchMgtRulesService archiveSearchMgtRulesService;

    @Autowired
    public ArchivesSearchExternalController(
        ArchiveSearchService archiveSearchService,
        ArchiveSearchUnitExportCsvService archiveSearchUnitExportCsvService,
        ExportDipService exportDipService,
        TransferVitamOperationsService transferVitamOperationsService,
        ArchiveSearchEliminationService archiveSearchEliminationService,
        ArchiveSearchMgtRulesService archiveSearchMgtRulesService
    ) {
        this.archiveSearchService = archiveSearchService;
        this.archiveSearchUnitExportCsvService = archiveSearchUnitExportCsvService;
        this.exportDipService = exportDipService;
        this.transferVitamOperationsService = transferVitamOperationsService;
        this.archiveSearchEliminationService = archiveSearchEliminationService;
        this.archiveSearchMgtRulesService = archiveSearchMgtRulesService;
    }

    @PostMapping(RestApi.SEARCH_PATH)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public VitamUIArchiveUnitResponseDto searchArchiveUnitsByCriteria(final @RequestBody SearchCriteriaDto query)
        throws VitamClientException, IOException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.debug("Calling search archive Units By Criteria {} ", query);
        return archiveSearchService.searchArchiveUnitsByCriteria(query);
    }

    @GetMapping(RestApi.FILING_HOLDING_SCHEME_PATH)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public VitamUISearchResponseDto getFillingHoldingScheme() throws VitamClientException {
        return archiveSearchService.getFillingHoldingScheme();
    }

    @GetMapping(
        value = RestApi.DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Secured(ServicesData.ARCHIVE_SEARCH_ROLE_GET_ARCHIVE_BINARY)
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(
        final @PathVariable("id") String id,
        final @RequestParam(value = "usage", required = false) String usage,
        final @RequestParam(value = "version", required = false) Integer version
    ) throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Download the Archive Unit Object with id {} ", id);
        return archiveSearchService.downloadObjectFromUnit(id, usage, version);
    }

    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public ResultsDto findUnitById(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("the UA by id {} ", id);
        return archiveSearchService.findArchiveUnitById(id);
    }

    @GetMapping(RestApi.OBJECTGROUP + CommonConstants.PATH_ID)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public ResultsDto findObjectById(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find a ObjectGroup by id {} ", id);
        return archiveSearchService.findObjectById(id);
    }

    @PostMapping(RestApi.EXPORT_CSV_SEARCH_PATH)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public Resource exportCsvArchiveUnitsByCriteria(final @RequestBody SearchCriteriaDto query)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.debug("Calling export to csv search archive Units By Criteria {} ", query);
        return archiveSearchUnitExportCsvService.exportToCsvSearchArchiveUnitsByCriteria(query);
    }

    @PostMapping(RestApi.EXPORT_DIP)
    @Secured(ServicesData.ROLE_EXPORT_DIP)
    public String exportDIPByCriteria(final @RequestBody ExportDipCriteriaDto exportDipCriteriaDto)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, exportDipCriteriaDto);
        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        LOGGER.debug("Calling export DIP By Criteria {} ", exportDipCriteriaDto);
        return exportDipService.requestToExportDIP(exportDipCriteriaDto);
    }

    @PostMapping(RestApi.TRANSFER_REQUEST)
    @Secured(ServicesData.ROLE_TRANSFER_REQUEST)
    public String transferRequest(final @RequestBody TransferRequestDto transferRequestDto)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, transferRequestDto);
        SanityChecker.sanitizeCriteria(transferRequestDto);
        LOGGER.debug("Calling transfer request {} ", transferRequestDto);
        return transferVitamOperationsService.transferRequest(transferRequestDto);
    }

    @PostMapping(RestApi.ELIMINATION_ANALYSIS)
    @Secured(ServicesData.ROLE_ELIMINATION)
    public JsonNode startEliminationAnalysis(final @RequestBody SearchCriteriaDto query)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.debug("Calling elimination analysis by criteria {} ", query);
        return archiveSearchEliminationService.startEliminationAnalysis(query);
    }

    @PostMapping(RestApi.ELIMINATION_ACTION)
    @Secured(ServicesData.ROLE_ELIMINATION)
    public JsonNode startEliminationAction(final @RequestBody SearchCriteriaDto query)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.debug("Calling elimination action by criteria {} ", query);
        return archiveSearchEliminationService.startEliminationAction(query);
    }

    @PostMapping(RestApi.ELIMINATION_UNIT_TREE_ACTION)
    @Secured(ServicesData.ROLE_ELIMINATION)
    public JsonNode startEliminationUnitTreeAction(final @RequestBody SearchCriteriaDto query)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.debug("Calling elimination action by criteria {} ", query);
        //TODO : To be modified after the development of the functionality on the Vitam core side
        return archiveSearchEliminationService.startEliminationAction(query);
    }

    @PostMapping(RestApi.MASS_UPDATE_UNITS_RULES)
    @Secured(ServicesData.ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT_ROLE)
    public String updateArchiveUnitsRules(final @RequestBody RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, ruleSearchCriteriaDto);
        SanityChecker.sanitizeCriteria(ruleSearchCriteriaDto);
        LOGGER.debug("Calling Update Archive Units Rules By Criteria {} ", ruleSearchCriteriaDto);
        return archiveSearchMgtRulesService.updateArchiveUnitsRules(ruleSearchCriteriaDto);
    }

    @PostMapping(RestApi.COMPUTED_INHERITED_RULES)
    @Secured(ServicesData.ROLE_COMPUTED_INHERITED_RULES)
    public String computedInheritedRules(final @RequestBody SearchCriteriaDto searchCriteriaDto)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, searchCriteriaDto);
        SanityChecker.sanitizeCriteria(searchCriteriaDto);
        LOGGER.debug("Calling computed inherited rules By Criteria {} ", searchCriteriaDto);
        return archiveSearchService.computedInheritedRules(searchCriteriaDto);
    }

    @PostMapping(RestApi.UNIT_WITH_INHERITED_RULES)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public ResultsDto selectUnitWithInheritedRules(final @RequestBody SearchCriteriaDto query)
        throws PreconditionFailedException, VitamClientException, IOException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.debug("Calling select Unit With Inherited Rules By Criteria {} ", query);
        return archiveSearchService.selectUnitWithInheritedRules(query);
    }

    @PostMapping(RestApi.RECLASSIFICATION)
    @Secured(ServicesData.ROLE_RECLASSIFICATION)
    public String reclassification(@RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, reclassificationCriteriaDto);
        SanityChecker.sanitizeCriteria(reclassificationCriteriaDto);
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        return archiveSearchService.reclassification(reclassificationCriteriaDto);
    }

    @ApiOperation(
        value = "Upload an ATR file for the transfer acknowledgment",
        consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Secured(ServicesData.ROLE_TRANSFER_ACKNOWLEDGMENT)
    @PostMapping(value = RestApi.TRANSFER_ACKNOWLEDGMENT, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String transferAcknowledgment(InputStream inputStream)
        throws PreconditionFailedException, VitamClientException {
        LOGGER.debug("[EXTERNAL] : Transfer Acknowledgment Operation");
        return transferVitamOperationsService.transferAcknowledgment(inputStream);
    }

    @GetMapping(CommonConstants.EXTERNAL_ONTOLOGIES_LIST)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public List<VitamUiOntologyDto> getExternalOntologiesList() throws IOException {
        LOGGER.debug("[EXTERNAL] : Get External ontologies list");
        return archiveSearchService.readExternalOntologiesFromFile();
    }

    @GetMapping(RestApi.UNITS_PERSISTENT_IDENTIFIER)
    @Secured(ServicesData.ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH_ROLE)
    public PersistentIdentifierResponseDto findUnitsByPersistentIdentifier(
        final @RequestParam(value = "id") String arkId
    ) throws VitamClientException {
        LOGGER.debug("[EXTERNAL] : Get units by persistent identifier {}", arkId);
        final PersistentIdentifierResponseDto persistentIdentifierResponse =
            archiveSearchService.findUnitsByPersistentIdentifier(arkId);
        LOGGER.debug("[EXTERNAL] : persistentIdentifierResponse = {}", persistentIdentifierResponse);
        return persistentIdentifierResponse;
    }

    @GetMapping(RestApi.OBJECTS_PERSISTENT_IDENTIFIER)
    @Secured(ServicesData.ARCHIVE_SEARCH_ROLE_GET_ARCHIVE_BINARY)
    public PersistentIdentifierResponseDto findObjectsByPersistentIdentifier(
        final @RequestParam(value = "id") String arkId
    ) throws VitamClientException {
        LOGGER.debug("[EXTERNAL] : Get objects by persistent identifier {}", arkId);
        final PersistentIdentifierResponseDto persistentIdentifierResponse =
            archiveSearchService.findObjectsByPersistentIdentifier(arkId);
        LOGGER.debug("[EXTERNAL] : persistentIdentifierResponse = {}", persistentIdentifierResponse);
        return persistentIdentifierResponse;
    }
}
