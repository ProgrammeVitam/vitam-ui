/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
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

package fr.gouv.vitamui.referential.service;

import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.ProcessDetailDto;
import fr.gouv.vitamui.referential.external.client.LogbookManagementOperationExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogbookManagementOperationService extends AbstractPaginateService<ProcessDetailDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        LogbookManagementOperationService.class
    );

    private LogbookManagementOperationExternalRestClient logbookManagementOperationExternalRestClient;

    private CommonService commonService;

    @Autowired
    public LogbookManagementOperationService(
        LogbookManagementOperationExternalRestClient logbookManagementOperationExternalRestClient,
        CommonService commonService
    ) {
        this.logbookManagementOperationExternalRestClient = logbookManagementOperationExternalRestClient;
        this.commonService = commonService;
    }

    @Override
    protected Integer beforePaginate(final Integer page, final Integer size) {
        return commonService.checkPagination(page, size);
    }

    @Override
    public BasePaginatingAndSortingRestClient<ProcessDetailDto, ExternalHttpContext> getClient() {
        return logbookManagementOperationExternalRestClient;
    }

    public ResponseEntity<ProcessDetailDto> searchOperationsDetails(
        ExternalHttpContext context,
        ProcessQuery processQuery
    ) {
        LOGGER.debug("Get All Operations Details with processQuery = {}", processQuery);
        return logbookManagementOperationExternalRestClient.searchOperationsDetails(context, processQuery);
    }

    public ResponseEntity<ProcessDetailDto> cancelOperationProcessExecution(
        ExternalHttpContext context,
        String operationId
    ) {
        LOGGER.debug("Cancel the operation Id = {}", operationId);
        return logbookManagementOperationExternalRestClient.cancelOperationProcessExecution(context, operationId);
    }

    public ResponseEntity<ProcessDetailDto> updateOperationActionProcess(
        ExternalHttpContext context,
        String actionId,
        String operationId
    ) {
        LOGGER.debug("Update the operation id={} with the Action ={}", operationId, actionId);
        return logbookManagementOperationExternalRestClient.updateOperationActionProcess(
            context,
            actionId,
            operationId
        );
    }
}
