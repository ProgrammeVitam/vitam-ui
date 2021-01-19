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



import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.external.server.service.ArchivesSearchExternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * UI Archive-Search External controller
 */
@Api(tags = "Archives search")
@RequestMapping(RestApi.ARCHIVE_SEARCH_PATH)
@RestController
@ResponseBody
public class ArchivesSearchExternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchExternalController.class);

    private final ArchivesSearchExternalService archivesSearchExternalService;

    @Autowired
    public ArchivesSearchExternalController(ArchivesSearchExternalService archivesSearchExternalService) {
        this.archivesSearchExternalService = archivesSearchExternalService;
    }

    @PostMapping(RestApi.SEARCH_PATH)
    @Secured(ServicesData.ROLE_GET_ARCHIVE)
    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final @RequestBody SearchCriteriaDto query) {
        LOGGER.info("Calling search archive Units By Criteria {} ", query);
        ParameterChecker.checkParameter("The query is a mandatory parameter: ", query);
        SanityChecker.sanitizeCriteria(query);
        return archivesSearchExternalService.searchArchiveUnitsByCriteria(query);
    }

    @GetMapping(RestApi.FILING_HOLDING_SCHEME_PATH)
    @Secured(ServicesData.ROLE_GET_ARCHIVE)
    public VitamUISearchResponseDto getFillingHoldingScheme() {
        return archivesSearchExternalService.getFilingHoldingScheme();
    }

    @GetMapping(RestApi.DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_ARCHIVE)
    public ResponseEntity<Resource> downloadObjectFromUnit(final @PathVariable("id") String id) {
        LOGGER.info("Download the Archive Unit Object with id {} ", id);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        return archivesSearchExternalService.downloadObjectFromUnit(id);
    }

    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_ARCHIVE)
    public ResponseEntity<ResultsDto> findUnitById(final @PathVariable("id") String id) {
        LOGGER.info("the UA by id {} ", id);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        return archivesSearchExternalService.findUnitById(id);
    }

}
