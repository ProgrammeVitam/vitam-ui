/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.external.server.service;

import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.internal.client.CollectInternalRestClient;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.collect.internal.client.CollectInternalWebClient;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierType;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
@Service
public class ProjectObjectGroupExternalService extends
    AbstractResourceClientService<CollectProjectDto, CollectProjectDto> {

    private final CollectInternalRestClient collectInternalRestClient;
    private final CollectInternalWebClient collectInternalWebClient;

    public ProjectObjectGroupExternalService(CollectInternalRestClient collectInternalRestClient,
        CollectInternalWebClient collectInternalWebClient,
        ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.collectInternalRestClient = collectInternalRestClient;
        this.collectInternalWebClient = collectInternalWebClient;
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, String objectId, String usage, Integer version) {
        // Logic moved here from ui-collect, similar from the one in archive-search
        String fileName = null;
        ResultsDto got = findObjectById(objectId).getBody();
        if (nonNull(got)) {
            QualifiersDto qualifier;
            if (isEmpty(usage)) {
                // find the best qualifier for download
                qualifier = getLastObjectQualifier(got);
            } else {
                String finalUsage = usage;
                qualifier = got.getQualifiers().stream()
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
                    versionsDto = qualifier.getVersions().stream()
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

        return collectInternalWebClient
            .downloadObjectFromUnit(id, usage, version, getInternalHttpContext(), fileName);
    }

    private QualifiersDto getLastObjectQualifier(ResultsDto got) {
        for (String qualifierName : ObjectQualifierType.allValuesOrdered) {
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
        return qualifier.getVersions().stream()
            .max(comparing(ProjectObjectGroupExternalService::extractVersion))
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

    public ResponseEntity<ResultsDto> findObjectById(String id) {
        return collectInternalRestClient.findObjectById(id, getInternalHttpContext());
    }
    @Override
    protected CollectInternalRestClient getClient() {
        return collectInternalRestClient;
    }
}
