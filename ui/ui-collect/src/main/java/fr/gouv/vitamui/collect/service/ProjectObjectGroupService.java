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

package fr.gouv.vitamui.collect.service;

import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.external.client.CollectExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectExternalWebClient;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierType;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * UI Collect Object group Service
 */
@Service
public class ProjectObjectGroupService extends AbstractPaginateService<CollectProjectDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectObjectGroupService.class);
    private final CollectExternalRestClient collectExternalRestClient;
    private final CollectExternalWebClient collectExternalWebClient;

    public ProjectObjectGroupService(
        CollectExternalRestClient collectExternalRestClient,
        CollectExternalWebClient collectExternalWebClient
    ) {
        this.collectExternalRestClient = collectExternalRestClient;
        this.collectExternalWebClient = collectExternalWebClient;
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(
        String unitId,
        String objectId,
        String qualifier,
        Integer version,
        ObjectData objectData,
        ExternalHttpContext context
    ) {
        LOGGER.debug("Download the Archive Unit Object with id {}", unitId);

        ResultsDto got = findObjectById(objectId, context).getBody();
        setObjectData(Objects.requireNonNull(got), objectData);
        if (isNotBlank(qualifier) && nonNull(version)) {
            objectData.setQualifier(qualifier);
            objectData.setVersion(version);
        }
        return collectExternalWebClient.downloadObjectFromUnit(
            unitId,
            objectData.getQualifier(),
            objectData.getVersion(),
            context
        );
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
        if (isNull(qualifier) || CollectionUtils.isEmpty(qualifier.getVersions())) {
            return null;
        }
        return qualifier.getVersions().stream().reduce((first, second) -> second).orElse(null);
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

    public ResponseEntity<ResultsDto> findObjectById(String id, ExternalHttpContext context) {
        LOGGER.debug("Get the Object Group with Identifier {}", id);
        return collectExternalRestClient.findObjectById(id, context);
    }

    public CollectExternalRestClient getClient() {
        return collectExternalRestClient;
    }

    @Override
    protected Integer beforePaginate(Integer page, Integer size) {
        return null;
    }
}
