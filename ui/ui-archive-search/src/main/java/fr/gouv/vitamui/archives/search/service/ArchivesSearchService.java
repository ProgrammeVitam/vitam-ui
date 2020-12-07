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
package fr.gouv.vitamui.archives.search.service;


import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalRestClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalWebClient;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * UI
 * Archive-Search Service
 */
@Service
public class ArchivesSearchService extends AbstractPaginateService<ArchiveUnitsDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchivesSearchService.class);

    private final ArchiveSearchExternalWebClient archiveSearchExternalWebClient;
    private final ArchiveSearchExternalRestClient archiveSearchExternalRestClient;
    private CommonService commonService;

    @Autowired
    public ArchivesSearchService(final CommonService commonService,
        final ArchiveSearchExternalRestClient archiveSearchExternalRestClient,
        final ArchiveSearchExternalWebClient archiveSearchExternalWebClient) {
        this.commonService = commonService;
        this.archiveSearchExternalRestClient = archiveSearchExternalRestClient;
        this.archiveSearchExternalWebClient = archiveSearchExternalWebClient;
    }

    @Override
    protected Integer beforePaginate(final Integer page, final Integer size) {
        return commonService.checkPagination(page, size);
    }

    public ArchiveSearchExternalRestClient getClient() {
        return archiveSearchExternalRestClient;
    }

    public ArchiveUnitsDto findArchiveUnits(final SearchCriteriaDto searchQuery,
        final ExternalHttpContext context) {
        LOGGER.info("calling find archive units by criteria {} ", searchQuery);
        ArchiveUnitsDto archiveUnits = getClient().searchArchiveUnitsByCriteria(context, searchQuery);
        return archiveUnits;
    }

    public VitamUISearchResponseDto findFilingHoldingScheme(ExternalHttpContext context) {
        return archiveSearchExternalRestClient.getFilingHoldingScheme(context);
    }

    public ResponseEntity<Resource> downloadObjectFromUnit(String id, ExternalHttpContext context) {
        LOGGER.info("Download the Archive Unit Object with id {}", id);
        return archiveSearchExternalRestClient.downloadObjectFromUnit(id, context);
    }

    public ResponseEntity<ResultsDto> findUnitById(String id, ExternalHttpContext context) {
        LOGGER.info("Get the Archive Unit with ID {}", id);
        return archiveSearchExternalRestClient.findUnitById(id, context);
    }


}
