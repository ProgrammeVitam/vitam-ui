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
package fr.gouv.vitamui.archives.search.service;


import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalRestClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalWebClient;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierTypeEnum;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * UI
 * Archive-Search Service
 */
@Service
public class ArchivesSearchService extends AbstractPaginateService<ArchiveUnitsDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchivesSearchService.class);

    private final ArchiveSearchExternalRestClient archiveSearchExternalRestClient;
    private final ArchiveSearchExternalWebClient archiveSearchExternalWebClient;
    private final CommonService commonService;

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
        LOGGER.debug("calling find archive units by criteria {} ", searchQuery);
        return getClient().searchArchiveUnitsByCriteria(context, searchQuery);
    }

    public VitamUISearchResponseDto findFilingHoldingScheme(ExternalHttpContext context) {
        return archiveSearchExternalRestClient.getFilingHoldingScheme(context);
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, ObjectData objectData,
        ExternalHttpContext context) {
        LOGGER.debug("Download the Archive Unit Object with id {}", id);

        ResultsDto got = findObjectById(id, context).getBody();
        String usage = getUsage(Objects.requireNonNull(got), objectData);
        return archiveSearchExternalWebClient
                .downloadObjectFromUnit(id, usage, getVersion(got.getQualifiers(), usage), context);
    }

    public ResponseEntity<ResultsDto> findUnitById(String id, ExternalHttpContext context) {
        LOGGER.debug("Get the Archive Unit with ID {}", id);
        return archiveSearchExternalRestClient.findUnitById(id, context);
    }

    public ResponseEntity<ResultsDto> findObjectById(String id, ExternalHttpContext context) {
        LOGGER.debug("Get the Object Group with Identifier {}", id);
        return archiveSearchExternalRestClient.findObjectById(id, context);
    }

    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        ExternalHttpContext context) {
        LOGGER.debug("export search archives Units by criteria into csv format with criteria {}", searchQuery);
        return archiveSearchExternalRestClient.exportCsvArchiveUnitsByCriteria(searchQuery, context);
    }

    public String getUsage(ResultsDto got, ObjectData objectData) {
        String finalUsage = null;
        for (QualifiersDto qualifier : got.getQualifiers()) {
            finalUsage = Arrays.stream(ObjectQualifierTypeEnum.values())
                .map(ObjectQualifierTypeEnum::getValue)
                .filter(value -> value.equals(qualifier.getQualifier()))
                .findFirst().orElse(null);
            String filename = getObjectDataFilename(objectData, qualifier);
            if (filename == null) {
                LOGGER.debug("Objectdata with first FileInfoModel.filename null : {}", objectData);
                continue;
            }
            objectData.setFilename(filename);
            objectData.setMimeType(qualifier.getVersions().get(0).getFormatIdentification().getMimeType());
        }
        return finalUsage;
    }

    private String getObjectDataFilename(ObjectData objectData, QualifiersDto qualifier) {
        if (qualifier.getVersions() != null && qualifier.getVersions().get(0).getFileInfoModel() == null
            || StringUtils.isNotEmpty(objectData.getFilename())) {
            return null;
        }
        return qualifier.getVersions().get(0).getFileInfoModel().getFilename();
    }

    public Integer getVersion(List<QualifiersDto> qualifiers, String usage) {
        List<VersionsDto> versions =
            qualifiers.stream().filter(q -> q.getQualifier().equals(usage)).findFirst().orElseThrow().getVersions();
        return Integer.parseInt(versions.get(0).getDataObjectVersion().split("_")[1]);
    }

    public ResponseEntity<String> exportDIPByCriteria(final ExportDipCriteriaDto exportDipCriteriaDto,
        ExternalHttpContext context) {
        LOGGER.info("export DIP with criteria {}", exportDipCriteriaDto);
        return archiveSearchExternalRestClient.exportDIPCriteria(exportDipCriteriaDto, context);
    }

    public ResponseEntity<String> transferRequest(final TransferRequestDto transferRequestDto,
        ExternalHttpContext context) {
        LOGGER.debug("Transfer request: {}", transferRequestDto);
        return archiveSearchExternalRestClient.transferRequest(transferRequestDto, context);
    }

    public ResponseEntity<JsonNode> startEliminationAnalysis(ExternalHttpContext context,
        final SearchCriteriaDto searchQuery) {
        LOGGER.info("elimination analysis with query : {}", searchQuery);
        return archiveSearchExternalRestClient.startEliminationAnalysis(context, searchQuery);
    }

    public ResponseEntity<JsonNode> startEliminationAction(ExternalHttpContext context,
        final SearchCriteriaDto searchQuery) {
        LOGGER.info("elimination action with query : {}", searchQuery);
        return archiveSearchExternalRestClient.startEliminationAction(context, searchQuery);
    }

    public ResponseEntity<String> updateArchiveUnitsRules(final RuleSearchCriteriaDto ruleSearchCriteriaDto,
        ExternalHttpContext context) {
        LOGGER.info("Update Archive Units Rules  with criteria {}", ruleSearchCriteriaDto);
        return archiveSearchExternalRestClient.updateArchiveUnitsRules(ruleSearchCriteriaDto, context);
    }

    public ResponseEntity<String> computedInheritedRules(final SearchCriteriaDto searchCriteriaDto,
        ExternalHttpContext context) {
        LOGGER.info("computed Inherited Rules with criteria {}", searchCriteriaDto);
        return archiveSearchExternalRestClient.computedInheritedRules(searchCriteriaDto, context);
    }

    public ResponseEntity<ResultsDto> selectUnitsWithInheritedRules(final SearchCriteriaDto searchQuery,
        final ExternalHttpContext context) {
        LOGGER.debug("calling select Unit With Inherited Rules by criteria {} ", searchQuery);
        return archiveSearchExternalRestClient.selectUnitWithInheritedRules(context, searchQuery);
    }

    public ResponseEntity<String> reclassification(final ReclassificationCriteriaDto reclassificationCriteriaDto,
        ExternalHttpContext context) {
        LOGGER.info("Reclassification with criteria {}", reclassificationCriteriaDto);
        return archiveSearchExternalRestClient.reclassification(reclassificationCriteriaDto, context);
    }

    public ResponseEntity<String> updateUnitById(String id, final UnitDescriptiveMetadataDto unitDescriptiveMetadataDto,
        ExternalHttpContext context) {
        LOGGER.debug("Update the Archive Unit with id {}", id);
        return archiveSearchExternalRestClient.updateUnitById(id, unitDescriptiveMetadataDto, context);
    }
}
