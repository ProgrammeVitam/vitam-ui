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

package fr.gouv.vitamui.archive.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchInternalService;
import fr.gouv.vitamui.archive.internal.server.service.ExternalParametersService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping(RestApi.ARCHIVE_SEARCH_PATH)
@Getter
@Setter
@Api(tags = "archives search", value = "Archives units search")
public class ArchiveSearchInternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalController.class);

    private final ArchiveSearchInternalService archiveInternalService;


    private final InternalSecurityService securityService;

    private final ExternalParametersService externalParametersService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ArchiveSearchInternalController(final ArchiveSearchInternalService archiveInternalService,
        final InternalSecurityService securityService, final ObjectMapper objectMapper,
        final ExternalParametersService externalParametersService) {
        this.archiveInternalService = archiveInternalService;
        this.securityService = securityService;
        this.objectMapper = objectMapper;
        this.externalParametersService = externalParametersService;
    }

    @PostMapping(RestApi.SEARCH_PATH)
    public ArchiveUnitsDto searchArchiveUnitsByCriteria(@RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        LOGGER.info("Calling service searchArchiveUnits By Criteria {} ", searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker.checkParameter("the SearchCriteria are mandatory parameters: ", searchQuery);

        return archiveInternalService.searchArchiveUnitsByCriteria(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(RestApi.FILING_HOLDING_SCHEME_PATH)
    public VitamUISearchResponseDto getFillingHoldingScheme() throws VitamClientException, IOException {
        LOGGER.debug("Get filing plan");
        return objectMapper.treeToValue(archiveInternalService.getFillingHoldingScheme(
                externalParametersService.buildVitamContextFromExternalParam()),
            VitamUISearchResponseDto.class);
    }

    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public ResultsDto findUnitById(final @PathVariable("id") String id) throws VitamClientException {
        LOGGER.info("UA Details  {}", id);
        ParameterChecker.checkParameter("The identifier is mandatory parameter: ", id);
        return archiveInternalService.findArchiveUnitById(id,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(RestApi.OBJECTGROUP + CommonConstants.PATH_ID)
    public ResultsDto findObjectById(final @PathVariable("id") String id)
        throws VitamClientException {
        LOGGER.info("Get ObjectGroup By id : {}", id);
        ParameterChecker
            .checkParameter("The identifier is mandatory parameter: ", id);

        return archiveInternalService.findObjectById(id,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(value = RestApi.DOWNLOAD_ARCHIVE_UNIT +
        CommonConstants.PATH_ID, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(final @PathVariable("id") String id,
        final @RequestParam("usage") String usage, final @RequestParam("version") Integer version
    ) {

        ParameterChecker.checkParameter("The identifier is mandatory parameter: ", id);
        LOGGER.info("Download Archive Unit Object with id {}", id);
        final VitamContext vitamContext = externalParametersService.buildVitamContextFromExternalParam();
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
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException {
        LOGGER.info("Export to CSV file Archive Units by criteria {}", searchQuery);

        Resource exportedResult =
            archiveInternalService.exportToCsvSearchArchiveUnitsByCriteria(searchQuery,
                externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(exportedResult, HttpStatus.OK);
    }

    @PostMapping(RestApi.EXPORT_DIP)
    public ResponseEntity<String> exportDIPByCriteria(
        @RequestBody final ExportDipCriteriaDto exportDipCriteriaDto)
        throws VitamClientException {
        LOGGER.info("Export DIP  by criteria {}", exportDipCriteriaDto);
        SanityChecker.sanitizeCriteria(exportDipCriteriaDto);
        ParameterChecker
            .checkParameter(
                "The tenant Id is a mandatory parameter: ",
                exportDipCriteriaDto);

        String result = archiveInternalService.requestToExportDIP(exportDipCriteriaDto,
            externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.ELIMINATION_ANALYSIS)
    public JsonNode startEliminationAnalysis(
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException {
        LOGGER.info("Calling elimination analysis by criteria {} ", searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter("The SearchCriteria is a mandatory parameter: ", searchQuery);

        JsonNode jsonNode = archiveInternalService.startEliminationAnalysis(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
        return jsonNode;
    }

    @PostMapping(RestApi.ELIMINATION_ACTION)
    public JsonNode startEliminationAction(
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException {
        LOGGER.info("Calling elimination action by criteria {} ", searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter(
                "The SearchCriteria is a mandatory parameters: ", searchQuery);

        JsonNode jsonNode = archiveInternalService.startEliminationAction(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
        return jsonNode;
    }

    @PostMapping(RestApi.MASS_UPDATE_UNITS_RULES)
    public ResponseEntity<String> updateArchiveUnitsRules(
        @RequestBody final RuleSearchCriteriaDto ruleSearchCriteriaDto) throws VitamClientException {
        LOGGER.info("Update Archive Units Rules by criteria {}", ruleSearchCriteriaDto);
        SanityChecker.sanitizeCriteria(ruleSearchCriteriaDto);
        ParameterChecker
            .checkParameter("The SearchCriteria is a  mandatory parameter: ", ruleSearchCriteriaDto);

        String result = archiveInternalService.updateArchiveUnitsRules(
            externalParametersService.buildVitamContextFromExternalParam(), ruleSearchCriteriaDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.COMPUTED_INHERITED_RULES)
    public ResponseEntity<String> computedInheritedRules(@RequestBody final SearchCriteriaDto searchCriteriaDto)
        throws VitamClientException {
        LOGGER.info("Computed Inherited Rules  by criteria {}", searchCriteriaDto);
        SanityChecker.sanitizeCriteria(searchCriteriaDto);
        ParameterChecker
            .checkParameter(
                "The SearchCriteria is a mandatory parameter: ",
                searchCriteriaDto);
        String result = archiveInternalService.computedInheritedRules(
            externalParametersService.buildVitamContextFromExternalParam(), searchCriteriaDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(RestApi.UNIT_WITH_INHERITED_RULES)
    public ResultsDto selectUnitsWithInheritedRules(@RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        LOGGER.debug(
            "Calling service select Unit With Inherited Rules  By Criteria {} ",
            searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter("The SearchCriteria is a mandatory parameter: ",
                searchQuery);
        return archiveInternalService.selectUnitWithInheritedRules(searchQuery,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @PostMapping(RestApi.RECLASSIFICATION)
    public ResponseEntity<String> reclassification(
        @RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto) throws VitamClientException {
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        SanityChecker.sanitizeCriteria(reclassificationCriteriaDto);
        ParameterChecker
            .checkParameter("the SearchCriteria is mandatory parameters: ", reclassificationCriteriaDto);
        String result =
            archiveInternalService.reclassification(externalParametersService.buildVitamContextFromExternalParam(),
                reclassificationCriteriaDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public String updateUnitById(final @PathVariable("id") String id,
        @RequestBody final UnitDescriptiveMetadataDto unitDescriptiveMetadataDto)
        throws VitamClientException {
        LOGGER.debug("update archiveUnit id  {}", id);
        ParameterChecker
            .checkParameter("The identifier, is a mandatory parameter: ", id);
        ParameterChecker
            .checkParameter("The request body is mandatory: ", unitDescriptiveMetadataDto);
        return archiveInternalService.updateUnitById(id, unitDescriptiveMetadataDto,
            externalParametersService.buildVitamContextFromExternalParam());
    }
}
