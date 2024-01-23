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
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalRestClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalWebClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchStreamingExternalRestClient;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierType;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


/**
 * UI
 * Archive-Search Service
 */
@Service
public class ArchivesSearchService extends AbstractPaginateService<ArchiveUnitsDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchivesSearchService.class);

    private final ArchiveSearchExternalRestClient archiveSearchExternalRestClient;
    private final ArchiveSearchExternalWebClient archiveSearchExternalWebClient;

    private final ArchiveSearchStreamingExternalRestClient archiveSearchStreamingExternalRestClient;
    private final CommonService commonService;

    @Autowired
    public ArchivesSearchService(final CommonService commonService,
        final ArchiveSearchExternalRestClient archiveSearchExternalRestClient,
        final ArchiveSearchExternalWebClient archiveSearchExternalWebClient,
        ArchiveSearchStreamingExternalRestClient archiveSearchStreamingExternalRestClient) {
        this.commonService = commonService;
        this.archiveSearchExternalRestClient = archiveSearchExternalRestClient;
        this.archiveSearchExternalWebClient = archiveSearchExternalWebClient;
        this.archiveSearchStreamingExternalRestClient = archiveSearchStreamingExternalRestClient;
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

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String unitId, String qualifier, Integer version,
        ObjectData objectData, ExternalHttpContext context) {
        LOGGER.debug("Download the Archive Unit Object with id {}", unitId);
        ResultsDto got = findObjectById(unitId, context).getBody();
        setObjectData(Objects.requireNonNull(got), objectData);
        if (isNotBlank(qualifier) && nonNull(version)) {
            objectData.setQualifier(qualifier);
            objectData.setVersion(version);
        }
        return archiveSearchExternalWebClient.downloadObjectFromUnit(unitId,
            objectData.getQualifier(),
            objectData.getVersion(),
            context);
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

    public void setObjectData(ResultsDto got, ObjectData objectData) {
        QualifiersDto qualifier = getLastObjectQualifier(got);
        if (isNull(qualifier)) {
            return;
        }
        objectData.setQualifier(qualifier.getQualifier());
        VersionsDto version = getLastVersion(qualifier);
        if (isNull(version)) {
            return;
        }
        objectData.setFilename(getFilename(version));
        objectData.setMimeType(getMimeType(version));
        objectData.setVersion(getVersion(version));
    }

    private QualifiersDto getLastObjectQualifier(ResultsDto got) {
        for (String qualifierName : ObjectQualifierType.downloadableValuesOrdered) {
            QualifiersDto qualifierFound = got.getQualifiers().stream()
                .filter(qualifier -> qualifierName.equals(qualifier.getQualifier()))
                .reduce((first, second) -> second)
                .orElse(null);
            if (nonNull(qualifierFound)) {
                return qualifierFound;
            }
        }
        return null;
    }

    private VersionsDto getLastVersion(QualifiersDto qualifier) {
        if (isNull(qualifier) || CollectionUtils.isEmpty(qualifier.getVersions())) {
            return null;
        }
        return qualifier.getVersions().stream()
            .reduce((first, second) -> second)
            .orElse(null);
    }

    private String getFilename(VersionsDto version) {
        if (isNull(version) || isEmpty(version.getId())) {
            return null;
        }
        return version.getId() + getExtension(version);
    }

    private String getExtension(VersionsDto version) {
        String uriExtension = EMPTY;
        if (isNotBlank(version.getUri()) && version.getUri().contains(".")) {
            uriExtension = version.getUri().substring(version.getUri().lastIndexOf('.') + 1);
        }
        String filenameExtension = EMPTY;
        if (nonNull(version.getFileInfoModel()) && isNotBlank(version.getFileInfoModel().getFilename()) &&
            version.getFileInfoModel().getFilename().contains(".")) {
            filenameExtension = version.getFileInfoModel().getFilename()
                .substring(version.getFileInfoModel().getFilename().lastIndexOf('.') + 1);
        }
        if (isNotBlank(filenameExtension)) {
            return "." + filenameExtension;
        } else if (isNotBlank(uriExtension)) {
            return "." + uriExtension;
        }
        return EMPTY;
    }

    private String getMimeType(VersionsDto version) {
        if (isNull(version) || isNull(version.getFormatIdentification())) {
            return null;
        }
        return version.getFormatIdentification().getMimeType();
    }

    private Integer getVersion(VersionsDto version) {
        if (isNull(version) || isNull(version.getDataObjectVersion())) {
            return null;
        }
        return Integer.parseInt(version.getDataObjectVersion().split("_")[1]);
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

    public ResponseEntity<String> transferAcknowledgment(final ExternalHttpContext context, String fileName,
        InputStream inputStream) {
        LOGGER.debug("transfer acknowledgment");
        return archiveSearchStreamingExternalRestClient
            .transferAcknowledgment(context, fileName, inputStream);
    }

    public List<VitamUiOntologyDto> getExternalOntologyFieldsList(ExternalHttpContext context) {
        LOGGER.debug("Get All External Ontologies");
        return archiveSearchExternalRestClient.getExternalOntologyFieldsList(context);
    }

    public PersistentIdentifierResponseDto findUnitsByPersistentIdentifier(String arkId, ExternalHttpContext context) {
        return archiveSearchExternalRestClient.findUnitsByPersistentIdentifier(arkId, context);
    }

}
