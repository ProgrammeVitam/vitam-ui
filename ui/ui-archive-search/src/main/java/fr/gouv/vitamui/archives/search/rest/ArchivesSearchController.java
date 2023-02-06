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
package fr.gouv.vitamui.archives.search.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.service.ArchivesSearchService;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.VitamuiRoles;
import fr.gouv.vitamui.commons.api.dtos.OntologyDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;


@Api(tags = "archives Search")
@RestController
@RequestMapping("${ui-archive-search.prefix}/archive-search")
@Consumes("application/json")
@Produces("application/json")
public class ArchivesSearchController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AbstractUiRestController.class);

    public static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";
    public static final String MANDATORY_QUERY = "The Query is a mandatory parameter: ";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    private final ArchivesSearchService archivesSearchService;

    @Autowired
    public ArchivesSearchController(final ArchivesSearchService service) {
        this.archivesSearchService = service;
    }

    @ApiOperation(value = "find archive units by criteria")
    @PostMapping(RestApi.SEARCH_PATH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIArchiveUnitResponseDto searchArchiveUnits(@RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        ArchiveUnitsDto archiveUnits;
        ParameterChecker.checkParameter(MANDATORY_QUERY, searchQuery);

        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("search archives Units by criteria = {}", searchQuery);
        VitamUIArchiveUnitResponseDto archiveResponseDtos = new VitamUIArchiveUnitResponseDto();
        if (searchQuery != null && !CollectionUtils.isEmpty(searchQuery.getCriteriaList())) {
            final boolean containsCriteriaByRuleRole = searchQuery.getCriteriaList().stream().anyMatch(
                criteria -> ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(criteria.getCategory()));
            if (containsCriteriaByRuleRole) {
                final boolean hasSearchByRuleRole = getAuthenticatedUser().getProfileGroup().getProfiles().stream()
                    .filter(Objects::nonNull)
                    .flatMap(profileDto -> profileDto.getRoles().stream())
                    .anyMatch(role -> VitamuiRoles.ROLE_SEARCH_WITH_RULES.equals(role.getName()));

                if (!hasSearchByRuleRole) {
                    LOGGER.debug("You are not authorized to make a search with DUA criteria");
                    throw new ForbiddenException("You are not authorized to make a search with DUA criteria");
                }
            }
        }
        archiveUnits = archivesSearchService.findArchiveUnits(searchQuery, buildUiHttpContext());

        if (archiveUnits != null) {
            archiveResponseDtos = archiveUnits.getArchives();
        }
        return archiveResponseDtos;

    }


    @ApiOperation(value = "Get filing plan")
    @GetMapping("/filingholdingscheme")
    @ResponseStatus(HttpStatus.OK)
    public VitamUISearchResponseDto findFilingHoldingScheme()
        throws InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug("find filing holding scheme");
        return archivesSearchService.findFilingHoldingScheme(buildUiHttpContext());
    }


    @ApiOperation(value = "Find the Archive Unit Details")
    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultsDto> findUnitById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Archive Unit with ID {}", id);
        return archivesSearchService.findUnitById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "Find the Object Group by identifier")
    @GetMapping(RestApi.OBJECTGROUP + PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultsDto> findObjectById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Object Group with Identifier {}", id);
        return archivesSearchService.findObjectById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "Download Object from the Archive Unit ")
    @GetMapping(value = RestApi.DOWNLOAD_ARCHIVE_UNIT + PATH_ID, produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> downloadObjectFromUnit(final @PathVariable("id") String id,
                                                           @QueryParam("qualifier") String qualifier,
                                                           @QueryParam("version") Integer version,
                                                           @QueryParam("tenantId") Integer tenantId,
                                                           @QueryParam("contractId") String contractId) throws PreconditionFailedException,
        InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier, The contractId and The tenantId are mandatory parameters: ",
            id, contractId, String.valueOf(tenantId));
        SanityChecker.checkSecureParameter(id, contractId, String.valueOf(tenantId));
        LOGGER.debug("Download the Archive Unit Object with ID {}", id);
        ObjectData objectData = new ObjectData();
        ResponseEntity<Resource> responseResource = archivesSearchService.downloadObjectFromUnit(id, qualifier, version,
            objectData, buildUiHttpContext(tenantId, contractId)).block();
        List<String> headersValuesContentDispo = responseResource.getHeaders().get(CONTENT_DISPOSITION);
        LOGGER.info("Content-Disposition value is {} ", headersValuesContentDispo);
        String fileNameHeader = isNotEmpty(objectData.getFilename())
            ? "attachment;filename=" + objectData.getFilename()
            : "attachment";
        MediaType contentType = isNotEmpty(objectData.getMimeType())
            ? new MediaType(MimeType.valueOf(objectData.getMimeType()))
            : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(CONTENT_DISPOSITION, fileNameHeader)
            .body(responseResource.getBody());
    }

    @ApiOperation(value = "export into csv format archive units by criteria")
    @PostMapping(RestApi.EXPORT_CSV_SEARCH_PATH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(@RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Export search archives Units by criteria into csv format = {}", searchQuery);
        Resource exportedCsvResult =
            archivesSearchService.exportCsvArchiveUnitsByCriteria(searchQuery, buildUiHttpContext()).getBody();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(CONTENT_DISPOSITION, "attachment")
            .body(exportedCsvResult);
    }

    @ApiOperation(value = "export DIP by criteria")
    @PostMapping(RestApi.EXPORT_DIP)
    @ResponseStatus(HttpStatus.OK)
    public String exportDIPByCriteria(@RequestBody final ExportDipCriteriaDto exportDipCriteriaDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, exportDipCriteriaDto);
        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        LOGGER.debug("Export DIP  with criteria {}", exportDipCriteriaDto);
        return archivesSearchService.exportDIPByCriteria(exportDipCriteriaDto, buildUiHttpContext()).getBody();
    }

    @ApiOperation(value = "Transfer request")
    @PostMapping(RestApi.TRANSFER_REQUEST)
    @ResponseStatus(HttpStatus.OK)
    public String transferRequest(@RequestBody final TransferRequestDto transferRequestDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, transferRequestDto);
        SanityChecker.sanitizeCriteria(transferRequestDto);
        LOGGER.debug("Transfer request: {}", transferRequestDto);
        return archivesSearchService.transferRequest(transferRequestDto, buildUiHttpContext()).getBody();
    }

    @ApiOperation(value = "elimination analysis launch")
    @PostMapping(RestApi.ELIMINATION_ANALYSIS)
    @ResponseStatus(HttpStatus.OK)
    public JsonNode startEliminationAnalysis(@RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter(MANDATORY_QUERY, searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Elimination analysis of query: {}", searchQuery);
        ResponseEntity<JsonNode> jsonNodeResponseEntity =
            archivesSearchService.startEliminationAnalysis(buildUiHttpContext(), searchQuery);
        return jsonNodeResponseEntity.getBody();
    }

    @ApiOperation(value = "launch elimination action")
    @PostMapping(RestApi.ELIMINATION_ACTION)
    @ResponseStatus(HttpStatus.OK)
    public JsonNode startEliminationAction(@RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Elimination Action of query: {}", searchQuery);
        ResponseEntity<JsonNode> jsonNodeResponseEntity =
            archivesSearchService.startEliminationAction(buildUiHttpContext(), searchQuery);
        return jsonNodeResponseEntity.getBody();
    }

    @ApiOperation(value = "Update Archive Units Rules by criteria")
    @PostMapping(RestApi.MASS_UPDATE_UNITS_RULES)
    @ResponseStatus(HttpStatus.OK)
    public String updateArchiveUnitsRules(@RequestBody final RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, ruleSearchCriteriaDto);
        LOGGER.debug("Update Archive Units Rules  with criteria {} ", ruleSearchCriteriaDto);
        SanityChecker.sanitizeCriteria(ruleSearchCriteriaDto);
        return archivesSearchService.updateArchiveUnitsRules(ruleSearchCriteriaDto, buildUiHttpContext()).getBody();
    }

    @ApiOperation(value = "computed Inherited Rules by criteria")
    @PostMapping(RestApi.COMPUTED_INHERITED_RULES)
    @ResponseStatus(HttpStatus.OK)
    public String computedInheritedRules(@RequestBody final SearchCriteriaDto searchCriteriaDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, searchCriteriaDto);
        SanityChecker.sanitizeCriteria(searchCriteriaDto);
        LOGGER.debug("Computed Inherited Rules with criteria {}", searchCriteriaDto);
        return archivesSearchService.computedInheritedRules(searchCriteriaDto, buildUiHttpContext()).getBody();
    }


    @ApiOperation(value = "select Unit With Inherited Rules")
    @PostMapping(RestApi.UNIT_WITH_INHERITED_RULES)
    @ResponseStatus(HttpStatus.OK)
    public ResultsDto selectUnitsWithInheritedRules(@RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("select Unit With Inherited Rules by criteria = {}", searchQuery);
        return archivesSearchService.selectUnitsWithInheritedRules(searchQuery, buildUiHttpContext()).getBody();

    }

    @ApiOperation(value = "launch reclassification by criteria")
    @PostMapping(RestApi.RECLASSIFICATION)
    public String reclassification(@RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(reclassificationCriteriaDto);
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        return archivesSearchService.reclassification(reclassificationCriteriaDto, buildUiHttpContext()).getBody();
    }

    @ApiOperation(value = "Update the Archive Unit descriptive metadata")
    @PutMapping(RestApi.ARCHIVE_UNIT_INFO + PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> updateUnitById(final @PathVariable("id") String id,
                                                 @RequestBody final UnitDescriptiveMetadataDto unitDescriptiveMetadataDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        ParameterChecker
            .checkParameter("The Unit Descriptive Metadata Dto sould not be empty: ", unitDescriptiveMetadataDto);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(unitDescriptiveMetadataDto);
        LOGGER.debug("Update the Archive Unit with id {}", id);
        LOGGER.debug("Update the Archive Unit update query {}", unitDescriptiveMetadataDto);
        return archivesSearchService.updateUnitById(id, unitDescriptiveMetadataDto, buildUiHttpContext());
    }

    @ApiOperation(value = "Transfer Acknowledgment", consumes = APPLICATION_OCTET_STREAM_VALUE)
    @Consumes(APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(RestApi.TRANSFER_ACKNOWLEDGMENT)
    public ResponseEntity<String> transferAcknowledgmentOperation(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final String tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestHeader(value = "fileName") final String fileName,
        final InputStream inputStream) throws InvalidParseOperationException, PreconditionFailedException {

        LOGGER.debug("[UI] : Transfer Acknowledgment Operation");
        ParameterChecker.checkParameter(
            "The access Contract , the tenant Id and the fileName are mandatory parameters: ",
            accessContractId, tenantId, fileName);
        SafeFileChecker.checkSafeFilePath(fileName);
        SanityChecker.isValidFileName(fileName);
        SanityChecker.checkSecureParameter(tenantId, accessContractId);
        LOGGER.debug("Start uploading file ...{} ", fileName);
        ResponseEntity<String> response =
            archivesSearchService.transferAcknowledgment(buildUiHttpContext(), fileName, inputStream);
        LOGGER.debug("The transfer acknowledgment operation id : {} ", response.toString());
        return response;
    }

    @ApiOperation(value = "get external ontologies list")
    @GetMapping(CommonConstants.EXTERNAL_ONTOLOGIES_LIST)
    @ResponseStatus(HttpStatus.OK)
    public List<OntologyDto> getExternalOntologiesList() throws InvalidParseOperationException {
        LOGGER.debug("[UI] : Get All External Ontologies");
        return archivesSearchService.getExternalOntologiesList(buildUiHttpContext());
    }
}
