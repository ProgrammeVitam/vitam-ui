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

package fr.gouv.vitamui.archives.search.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.service.ArchivesSearchService;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.VitamuiRoles;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Api(tags = "archives Search")
@RestController
@RequestMapping("${ui-archive-search.prefix}/archive-search")
@Consumes("application/json")
@Produces("application/json")
public class ArchivesSearchController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AbstractUiRestController.class);

    private final ArchivesSearchService archivesSearchService;

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Autowired
    public ArchivesSearchController(final ArchivesSearchService service) {
        this.archivesSearchService = service;
    }

    @ApiOperation(value = "find archive units by criteria")
    @PostMapping(RestApi.SEARCH_PATH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIArchiveUnitResponseDto searchArchiveUnits(@RequestBody final SearchCriteriaDto searchQuery) {
        ArchiveUnitsDto archiveUnits;
        ParameterChecker.checkParameter("The Query is a mandatory parameter: ", searchQuery);

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
                    LOGGER.info("You are not authorized to make a search with DUA criteria");
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
    public VitamUISearchResponseDto findFilingHoldingScheme() {
        LOGGER.debug("find filing holding scheme");
        return archivesSearchService.findFilingHoldingScheme(buildUiHttpContext());
    }


    @ApiOperation(value = "Find the Archive Unit Details")
    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultsDto> findUnitById(final @PathVariable("id") String id) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        LOGGER.debug("Find the Archive Unit with ID {}", id);
        return archivesSearchService.findUnitById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "Find the Object Group by identifier")
    @GetMapping(RestApi.OBJECTGROUP + CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultsDto> findObjectById(final @PathVariable("id") String id) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        LOGGER.debug("Find the Object Group with Identifier {}", id);
        return archivesSearchService.findObjectById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "Download Object from the Archive Unit ")
    @GetMapping(value = RestApi.DOWNLOAD_ARCHIVE_UNIT +
        CommonConstants.PATH_ID, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> downloadObjectFromUnit(final @PathVariable("id") String id,
        @QueryParam("tenantId") Integer tenantId,
        @QueryParam("contractId") String contractId) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        LOGGER.debug("Download the Archive Unit Object with ID {}", id);
        ObjectData objectData = new ObjectData();
        ResponseEntity<Resource> responseResource =
            archivesSearchService.downloadObjectFromUnit(id, objectData, buildUiHttpContext(tenantId, contractId))
                .block();
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
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(@RequestBody final SearchCriteriaDto searchQuery) {
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
    public String exportDIPByCriteria(@RequestBody final ExportDipCriteriaDto exportDipCriteriaDto) {
        LOGGER.debug("Export DIP  with criteria {}", exportDipCriteriaDto);
        String result = archivesSearchService.exportDIPByCriteria(exportDipCriteriaDto, buildUiHttpContext()).getBody();
        return result;
    }

    @ApiOperation(value = "elimination analysis launch")
    @PostMapping(RestApi.ELIMINATION_ANALYSIS)
    @ResponseStatus(HttpStatus.OK)
    public JsonNode startEliminationAnalysis(@RequestBody final SearchCriteriaDto searchQuery) {
        LOGGER.debug("Elimination analysis of query: {}", searchQuery);
        ResponseEntity<JsonNode> jsonNodeResponseEntity =
            archivesSearchService.startEliminationAnalysis(buildUiHttpContext(), searchQuery);
        return jsonNodeResponseEntity.getBody();
    }

    @ApiOperation(value = "launch elimination action")
    @PostMapping(RestApi.ELIMINATION_ACTION)
    @ResponseStatus(HttpStatus.OK)
    public JsonNode startEliminationAction(@RequestBody final SearchCriteriaDto searchQuery) {
        LOGGER.debug("Elimination Action of query: {}", searchQuery);
        ResponseEntity<JsonNode> jsonNodeResponseEntity =
            archivesSearchService.startEliminationAction(buildUiHttpContext(), searchQuery);
        return jsonNodeResponseEntity.getBody();
    }

    @ApiOperation(value = "Update Archive Units Rules by criteria")
    @PostMapping(RestApi.MASS_UPDATE_UNITS_RULES)
    @ResponseStatus(HttpStatus.OK)
    public String updateArchiveUnitsRules(@RequestBody final RuleSearchCriteriaDto ruleSearchCriteriaDto) {
        LOGGER.debug("Update Archive Units Rules  with criteria {} ", ruleSearchCriteriaDto);
        String result =
            archivesSearchService.updateArchiveUnitsRules(ruleSearchCriteriaDto, buildUiHttpContext()).getBody();
        return result;
    }

    @ApiOperation(value = "computed Inherited Rules by criteria")
    @PostMapping(RestApi.COMPUTED_INHERITED_RULES)
    @ResponseStatus(HttpStatus.OK)
    public String computedInheritedRules(@RequestBody final SearchCriteriaDto searchCriteriaDto) {
        LOGGER.debug("Computed Inherited Rules with criteria {}", searchCriteriaDto);
        String result = archivesSearchService.computedInheritedRules(searchCriteriaDto, buildUiHttpContext()).getBody();
        return result;
    }


    @ApiOperation(value = "select Unit With Inherited Rules")
    @PostMapping(RestApi.UNIT_WITH_INHERITED_RULES)
    @ResponseStatus(HttpStatus.OK)
    public ResultsDto selectUnitsWithInheritedRules(@RequestBody final SearchCriteriaDto searchQuery) {
        ArchiveUnitsDto archiveUnits;
        ParameterChecker.checkParameter("The Query is a mandatory parameter: ", searchQuery);
        LOGGER.debug("select Unit With Inherited Rules by criteria = {}", searchQuery);
        ResultsDto resultsDto =
            archivesSearchService.selectUnitsWithInheritedRules(searchQuery, buildUiHttpContext()).getBody();
        return resultsDto;

    }

    @ApiOperation(value = "launch reclassification by criteria")
    @PostMapping(RestApi.RECLASSIFICATION)
    public String reclassification(@RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto) {
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.convertValue(reclassificationCriteriaDto, JsonNode.class);
        LOGGER.debug("Reclassification query JSON {}", node.toPrettyString());
        return archivesSearchService.reclassification(reclassificationCriteriaDto, buildUiHttpContext()).getBody();
    }

    @ApiOperation(value = "Update the Archive Unit descriptive metadata")
    @PutMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> updateUnitById(final @PathVariable("id") String id,
        @RequestBody final UnitDescriptiveMetadataDto unitDescriptiveMetadataDto) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        ParameterChecker
            .checkParameter("The Unit Descriptive Metadata Dto sould not be empty: ", unitDescriptiveMetadataDto);
        LOGGER.debug("Update the Archive Unit with id {}", id);
        LOGGER.debug("Update the Archive Unit update query {}", unitDescriptiveMetadataDto);
        return archivesSearchService.updateUnitById(id, unitDescriptiveMetadataDto, buildUiHttpContext());
    }
}
