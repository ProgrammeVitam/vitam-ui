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


package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.dip.DataObjectVersions;
import fr.gouv.vitam.common.model.export.dip.DipExportType;
import fr.gouv.vitam.common.model.export.dip.DipRequest;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Archive-Search export Dip Internal service.
 */
@Service
public class ExportDipInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ExportDipInternalService.class);
    public static final String OPERATION_IDENTIFIER = "itemId";

    private final ExportDipV2Service exportDipV2Service;
    private final ArchiveSearchInternalService archiveSearchInternalService;


    public ExportDipInternalService(final @Lazy ArchiveSearchInternalService archiveSearchInternalService,
        final ExportDipV2Service exportDipV2Service) {
        this.archiveSearchInternalService = archiveSearchInternalService;

        this.exportDipV2Service = exportDipV2Service;
    }

    private JsonNode exportDIP(final VitamContext vitamContext, DipRequest dipRequest)
        throws VitamClientException {
        RequestResponse<JsonNode> response = exportDipV2Service.exportDip(vitamContext, dipRequest);
        return response.toJsonNode();
    }


    private DipRequest prepareDipRequestBody(final ExportDipCriteriaDto exportDipCriteriaDto, JsonNode dslQuery) {
        DipRequest dipRequest = new DipRequest();

        if (exportDipCriteriaDto != null) {
            final DataObjectVersions dataObjectVersionToExport = new DataObjectVersions();
            dataObjectVersionToExport.setDataObjectVersionsPatterns(exportDipCriteriaDto.getDataObjectVersionsPatterns());
            dipRequest.setExportWithLogBookLFC(exportDipCriteriaDto.isLifeCycleLogs());
            dipRequest.setExportWithoutObjects(exportDipCriteriaDto.isWithoutObjects());
            dipRequest.setDslRequest(dslQuery);
            dipRequest.setDipExportType(DipExportType.FULL);
            dipRequest.setDataObjectVersionToExport(dataObjectVersionToExport);
            dipRequest.setDipRequestParameters(exportDipCriteriaDto.getDipRequestParameters());
            dipRequest.setSedaVersion(exportDipCriteriaDto.getSedaVersion());
        }
        return dipRequest;
    }


    public String requestToExportDIP(final ExportDipCriteriaDto exportDipCriteriaDto,
        final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Export DIP by criteria {} ", exportDipCriteriaDto.toString());
        JsonNode dslQuery = archiveSearchInternalService
            .prepareDslQuery(exportDipCriteriaDto.getExportDIPSearchCriteria(), vitamContext);
        LOGGER.debug("Export DIP final DSL query {} ", dslQuery);

        DipRequest dipRequest = prepareDipRequestBody(exportDipCriteriaDto, dslQuery);

        JsonNode response = exportDIP(vitamContext, dipRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }
}
