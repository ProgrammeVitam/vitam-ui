/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.archives.search.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.dip.DataObjectVersions;
import fr.gouv.vitam.common.model.export.dip.DipExportType;
import fr.gouv.vitam.common.model.export.dip.DipRequest;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Archive-Search export Dip service.
 */
@Service
public class ExportDipService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportDipService.class);
    public static final String OPERATION_IDENTIFIER = "itemId";

    private final ExportDipV2Service exportDipV2Service;
    private final ArchiveSearchService archiveSearchService;

    private final ArchiveSearchThresholdService archiveSearchThresholdService;

    private final ArchiveSearchExternalParametersService archiveSearchExternalParametersService;

    public ExportDipService(
        final @Lazy ArchiveSearchService archiveSearchService,
        final ExportDipV2Service exportDipV2Service,
        ArchiveSearchThresholdService archiveSearchThresholdService,
        ArchiveSearchExternalParametersService archiveSearchExternalParametersService
    ) {
        this.archiveSearchService = archiveSearchService;

        this.exportDipV2Service = exportDipV2Service;
        this.archiveSearchThresholdService = archiveSearchThresholdService;
        this.archiveSearchExternalParametersService = archiveSearchExternalParametersService;
    }

    private JsonNode exportDIP(final VitamContext vitamContext, DipRequest dipRequest) throws VitamClientException {
        RequestResponse<JsonNode> response = exportDipV2Service.exportDip(vitamContext, dipRequest);
        return response.toJsonNode();
    }

    private DipRequest prepareDipRequestBody(final ExportDipCriteriaDto exportDipCriteriaDto, JsonNode dslQuery) {
        DipRequest dipRequest = new DipRequest();

        if (exportDipCriteriaDto != null) {
            final DataObjectVersions dataObjectVersionToExport = new DataObjectVersions();
            dataObjectVersionToExport.setDataObjectVersionsPatterns(
                exportDipCriteriaDto.getDataObjectVersionsPatterns()
            );
            dipRequest.setExportWithLogBookLFC(exportDipCriteriaDto.isLifeCycleLogs());
            dipRequest.setExportWithoutObjects(exportDipCriteriaDto.isWithoutObjects());
            dipRequest.setDslRequest(dslQuery);
            dipRequest.setDipExportType(DipExportType.FULL);
            dipRequest.setDataObjectVersionToExport(dataObjectVersionToExport);
            dipRequest.setDipRequestParameters(exportDipCriteriaDto.getDipRequestParameters());
            dipRequest.setSedaVersion(exportDipCriteriaDto.getSedaVersion());
            dipRequest.setExportWithTree(exportDipCriteriaDto.isExportWithTree());
            dipRequest.setUseOriginalFilenames(exportDipCriteriaDto.isExportWithTree());
        }
        return dipRequest;
    }

    public String requestToExportDIP(final ExportDipCriteriaDto exportDipCriteriaDto) throws VitamClientException {
        LOGGER.debug("Export DIP by criteria {} ", exportDipCriteriaDto.toString());
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(aLong -> exportDipCriteriaDto.getExportDIPSearchCriteria().setThreshold(aLong));

        JsonNode dslQuery = archiveSearchService.prepareDslQuery(
            exportDipCriteriaDto.getExportDIPSearchCriteria(),
            vitamContext
        );
        LOGGER.debug("Export DIP final DSL query {} ", dslQuery);

        DipRequest dipRequest = prepareDipRequestBody(exportDipCriteriaDto, dslQuery);

        JsonNode response = exportDIP(vitamContext, dipRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }
}
