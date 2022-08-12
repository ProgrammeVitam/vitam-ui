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
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierTypeEnum;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * UI Collect Object group Service
 */
@Service
public class ProjectObjectGroupService extends AbstractPaginateService<CollectProjectDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectObjectGroupService.class);
    private final CollectExternalRestClient collectExternalRestClient;
    private final CollectExternalWebClient collectExternalWebClient;


    public ProjectObjectGroupService(CollectExternalRestClient collectExternalRestClient,
        CollectExternalWebClient collectExternalWebClient) {
        this.collectExternalRestClient = collectExternalRestClient;
        this.collectExternalWebClient = collectExternalWebClient;
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, ObjectData objectData,
        ExternalHttpContext context) {
        LOGGER.debug("Download the Archive Unit Object with id {}", id);

        ResultsDto got = findObjectById(id, context).getBody();
        String usage = getUsage(Objects.requireNonNull(got), objectData);
        Integer version = getVersion(got.getQualifiers(), usage);
        return collectExternalWebClient.downloadObjectFromUnit(id, usage, version, context);
    }

    public ResponseEntity<ResultsDto> findObjectById(String id, ExternalHttpContext context) {
        LOGGER.debug("Get the Object Group with Identifier {}", id);
        return collectExternalRestClient.findObjectById(id, context);
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
        if (qualifier.getVersions().get(0).getFileInfoModel() == null || StringUtils.isNotEmpty(objectData.getFilename())) {
            return null;
        }
        return qualifier.getVersions().get(0).getFileInfoModel().getFilename();
    }

    public Integer getVersion(List<QualifiersDto> qualifiers, String usage) {
        List<VersionsDto> versions =
            qualifiers.stream().filter(q -> q.getQualifier().equals(usage)).findFirst().orElseThrow().getVersions();
        return Integer.parseInt(versions.get(0).getDataObjectVersion().split("_")[1]);
    }

    public CollectExternalRestClient getClient() {
        return collectExternalRestClient;
    }

    @Override
    protected Integer beforePaginate(Integer page, Integer size) {
        return null;
    }
}