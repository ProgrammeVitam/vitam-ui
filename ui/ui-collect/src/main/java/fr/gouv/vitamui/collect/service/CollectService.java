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

package fr.gouv.vitamui.collect.service;

import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.external.client.CollectExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectStreamingExternalRestClient;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

/**
 * UI Collect Service
 */
@Service
public class CollectService extends AbstractPaginateService<CollectProjectDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CollectService.class);

    private final CollectExternalRestClient collectExternalRestClient;
    private final CollectStreamingExternalRestClient collectStreamingExternalRestClient;
    private final CommonService commonService;

    @Autowired
    public CollectService(CommonService commonService,
        CollectExternalRestClient collectExternalRestClient,
        CollectStreamingExternalRestClient collectStreamingExternalRestClient) {
        this.commonService = commonService;
        this.collectExternalRestClient = collectExternalRestClient;
        this.collectStreamingExternalRestClient = collectStreamingExternalRestClient;
    }

    @Override
    protected Integer beforePaginate(final Integer page, final Integer size) {
        return commonService.checkPagination(page, size);
    }

    public CollectExternalRestClient getClient() {
        return collectExternalRestClient;
    }


    public CollectProjectDto createProject(ExternalHttpContext context, CollectProjectDto collectProjectDto) {
        return collectExternalRestClient.create(context, collectProjectDto);
    }

    public PaginatedValuesDto<CollectProjectDto> getAllProjectsPaginated(ExternalHttpContext context, final Integer page,
        final Integer size, final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        return collectExternalRestClient.getAllPaginated(context, page, size, criteria, orderBy, direction);
    }

    public ResponseEntity<Void> streamingUpload(final ExternalHttpContext context, String fileName,
        String projectId, InputStream inputStream) {
        return collectStreamingExternalRestClient.streamingUpload(context, fileName, projectId, inputStream);
    }
}
