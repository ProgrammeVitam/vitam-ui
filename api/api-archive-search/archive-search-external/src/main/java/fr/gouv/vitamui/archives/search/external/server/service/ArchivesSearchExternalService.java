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

package fr.gouv.vitamui.archives.search.external.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.archive.internal.client.ArchiveInternalRestClient;
import fr.gouv.archive.internal.client.ArchiveSearchInternalWebClient;
import fr.gouv.archive.internal.client.ArchiveSearchStreamingInternalRestClient;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierType;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The service to create vitam Archive-Search.
 */
@Getter
@Setter
@Service
public class ArchivesSearchExternalService extends AbstractResourceClientService<ArchiveUnitsDto, ArchiveUnitsDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivesSearchExternalService.class);

    @Autowired
    private final ArchiveInternalRestClient archiveInternalRestClient;

    @Autowired
    private final ArchiveSearchThresholdService archiveSearchThresholdService;

    @Autowired
    private final ArchiveSearchInternalWebClient archiveSearchInternalWebClient;

    @Autowired
    private final ArchiveSearchStreamingInternalRestClient archiveSearchStreamingInternalRestClient;

    public ArchivesSearchExternalService(
        @Autowired ArchiveInternalRestClient archiveInternalRestClient,
        ArchiveSearchInternalWebClient archiveSearchInternalWebClient,
        final ExternalSecurityService externalSecurityService,
        final ArchiveSearchStreamingInternalRestClient archiveSearchStreamingInternalRestClient,
        final ArchiveSearchThresholdService archiveSearchThresholdService
    ) {
        super(externalSecurityService);
        this.archiveInternalRestClient = archiveInternalRestClient;
        this.archiveSearchInternalWebClient = archiveSearchInternalWebClient;
        this.archiveSearchStreamingInternalRestClient = archiveSearchStreamingInternalRestClient;
        this.archiveSearchThresholdService = archiveSearchThresholdService;
    }

    @Override
    protected ArchiveInternalRestClient getClient() {
        return archiveInternalRestClient;
    }

    public VitamUIArchiveUnitResponseDto searchArchiveUnitsByCriteria(final SearchCriteriaDto query) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            query.setThreshold(thresholdOpt.get());
        }
        ArchiveUnitsDto archiveUnitsDto = getClient().searchArchiveUnitsByCriteria(getInternalHttpContext(), query);
        return archiveUnitsDto == null ? null : archiveUnitsDto.getArchives();
    }

    public ResponseEntity<ResultsDto> findUnitById(String id) {
        return archiveInternalRestClient.findUnitById(id, getInternalHttpContext());
    }

    public ResponseEntity<ResultsDto> findObjectById(String id) {
        return archiveInternalRestClient.findObjectById(id, getInternalHttpContext());
    }

    public VitamUISearchResponseDto getFilingHoldingScheme() {
        return archiveInternalRestClient.getFilingHoldingScheme(getInternalHttpContext());
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, String usage, Integer version) {
        // Logic moved here from ui-archive-search
        String fileName = null;
        ResultsDto got = findObjectById(id).getBody();
        if (nonNull(got)) {
            QualifiersDto qualifier;
            if (isEmpty(usage)) {
                // find the best qualifier for download
                qualifier = getLastObjectQualifier(got);
            } else {
                String finalUsage = usage;
                qualifier = got
                    .getQualifiers()
                    .stream()
                    .filter(q -> finalUsage.equals(q.getQualifier()))
                    .findFirst()
                    .orElse(null);
            }
            if (nonNull(qualifier)) {
                usage = qualifier.getQualifier();
                VersionsDto versionsDto;
                if (isNull(version)) {
                    // find the latest version for the qualifier
                    versionsDto = getLastVersion(qualifier);
                } else {
                    Integer finalVersion = version;
                    versionsDto = qualifier
                        .getVersions()
                        .stream()
                        .filter(v -> finalVersion.equals(extractVersion(v)))
                        .findFirst()
                        .orElse(null);
                }
                if (nonNull(versionsDto)) {
                    version = extractVersion(versionsDto);
                    fileName = getFilename(versionsDto);
                }
            }
        }
        return archiveSearchInternalWebClient.downloadObjectFromUnit(
            id,
            usage,
            version,
            getInternalHttpContext(),
            fileName
        );
    }

    private QualifiersDto getLastObjectQualifier(ResultsDto got) {
        for (String qualifierName : ObjectQualifierType.allValuesOrdered) {
            QualifiersDto qualifierFound = got
                .getQualifiers()
                .stream()
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
        return qualifier
            .getVersions()
            .stream()
            .max(comparing(ArchivesSearchExternalService::extractVersion))
            .orElse(null);
    }

    @NotNull
    private static Integer extractVersion(VersionsDto versionsDto) {
        return Integer.parseInt(versionsDto.getDataObjectVersion().split("_")[1]);
    }

    private String getFilename(VersionsDto version) {
        if (isNull(version) || StringUtils.isEmpty(version.getId())) {
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
        if (
            nonNull(version.getFileInfoModel()) &&
            isNotBlank(version.getFileInfoModel().getFilename()) &&
            version.getFileInfoModel().getFilename().contains(".")
        ) {
            filenameExtension = version
                .getFileInfoModel()
                .getFilename()
                .substring(version.getFileInfoModel().getFilename().lastIndexOf('.') + 1);
        }
        if (isNotBlank(filenameExtension)) {
            return "." + filenameExtension;
        } else if (isNotBlank(uriExtension)) {
            return "." + uriExtension;
        }
        return EMPTY;
    }

    public Resource exportCsvArchiveUnitsByCriteria(final SearchCriteriaDto query) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            query.setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.exportCsvArchiveUnitsByCriteria(query, getInternalHttpContext());
    }

    public String exportDIPByCriteria(final ExportDipCriteriaDto exportDipCriteriaDto) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            exportDipCriteriaDto.getExportDIPSearchCriteria().setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.exportDIPByCriteria(exportDipCriteriaDto, getInternalHttpContext());
    }

    public String transferRequest(final TransferRequestDto transferRequestDto) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            transferRequestDto.getSearchCriteria().setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.transferRequest(transferRequestDto, getInternalHttpContext());
    }

    public ResponseEntity<JsonNode> startEliminationAnalysis(final SearchCriteriaDto query) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            query.setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.startEliminationAnalysis(getInternalHttpContext(), query);
    }

    public ResponseEntity<JsonNode> startEliminationAction(final SearchCriteriaDto query) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            query.setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.startEliminationAction(getInternalHttpContext(), query);
    }

    public String updateArchiveUnitsRules(final RuleSearchCriteriaDto ruleSearchCriteriaDto) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            ruleSearchCriteriaDto.getSearchCriteriaDto().setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.updateArchiveUnitsRules(ruleSearchCriteriaDto, getInternalHttpContext());
    }

    public String computedInheritedRules(final SearchCriteriaDto searchCriteriaDto) {
        return archiveInternalRestClient.computedInheritedRules(searchCriteriaDto, getInternalHttpContext());
    }

    public ResultsDto selectUnitWithInheritedRules(final SearchCriteriaDto query) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            query.setThreshold(thresholdOpt.get());
        }
        return getClient().selectUnitWithInheritedRules(getInternalHttpContext(), query);
    }

    public String reclassification(final ReclassificationCriteriaDto reclassificationCriteriaDto) {
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        if (thresholdOpt.isPresent()) {
            reclassificationCriteriaDto.getSearchCriteriaDto().setThreshold(thresholdOpt.get());
        }
        return archiveInternalRestClient.reclassification(reclassificationCriteriaDto, getInternalHttpContext());
    }

    public String transferAcknowledgment(InputStream atrInputStream, final String originalFileName) {
        return archiveSearchStreamingInternalRestClient.transferAcknowledgment(
            getInternalHttpContext(),
            originalFileName,
            atrInputStream
        );
    }

    public List<VitamUiOntologyDto> getExternalOntologiesList() {
        return archiveInternalRestClient.getExternalOntologiesList(getInternalHttpContext());
    }

    public PersistentIdentifierResponseDto findUnitsByPersistentIdentifier(String arkId) {
        return archiveInternalRestClient.findUnitsByPersistentIdentifier(arkId, getInternalHttpContext());
    }

    public PersistentIdentifierResponseDto findObjectsByPersistentIdentifier(String arkId) {
        return archiveInternalRestClient.findObjectsByPersistentIdentifier(arkId, getInternalHttpContext());
    }
}
