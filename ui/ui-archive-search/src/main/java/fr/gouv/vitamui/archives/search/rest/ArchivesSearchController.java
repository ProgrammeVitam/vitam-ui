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

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.service.ArchivesSearchService;
import fr.gouv.vitamui.commons.api.CommonConstants;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;


@Api(tags = "archives Search")
@RestController
@RequestMapping("${ui-archive-search.prefix}/archive-search")
@Consumes("application/json")
@Produces("application/json")
public class ArchivesSearchController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AbstractUiRestController.class);

    private final ArchivesSearchService archivesSearchService;

    @Autowired
    public ArchivesSearchController(final ArchivesSearchService service) {
        this.archivesSearchService = service;
    }

    @ApiOperation(value = "find archive units by criteria")
    @PostMapping(RestApi.SEARCH_PATH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIArchiveUnitResponseDto searchArchiveUnits(@RequestBody final SearchCriteriaDto searchQuery) {
        LOGGER.debug("search archives Units by criteria = {}", searchQuery);
        VitamUIArchiveUnitResponseDto archiveResponseDtos = new VitamUIArchiveUnitResponseDto();
        ArchiveUnitsDto archiveUnits = archivesSearchService.findArchiveUnits(searchQuery, buildUiHttpContext());
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
    public ResponseEntity<ResultsDto> findUnitById(final @PathVariable("id") String id){
        LOGGER.debug("Find the Archive Unit with ID {}", id);
        return archivesSearchService.findUnitById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "Download Object from the Archive Unit ")
    @GetMapping(RestApi.DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> downloadObjectFromUnit(final @PathVariable("id") String id) {
        LOGGER.debug("Donwload the Archive Unit Object with ID {}", id);
        Resource body = archivesSearchService.downloadObjectFromUnit(id, buildUiHttpContext()).getBody();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment")
            .body(body);
    }
}
