/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.external.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.logbook.LogbookLifecycle;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.BaseRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.logbook.LogbookInternalRestClient;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookLifeCycleResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.client.AbstractInternalClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to interact with logbooks.
 *
 *
 */
@Getter
@Setter
@Service
public class LogbookExternalService extends AbstractInternalClientService {

    private final LogbookInternalRestClient<InternalHttpContext> logbookRestClient;

    @Autowired
    public LogbookExternalService(final LogbookInternalRestClient<InternalHttpContext> logbookRestClient,
            final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.logbookRestClient = logbookRestClient;
    }

    public static <T> T responseMapping(final JsonNode json, final Class<T> clazz) {
        try {
            return JsonUtils.treeToValue(json, clazz, false);
        } catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

    /**
     * Finds an operation by id.
     * @param id
     * @return
     */
    public LogbookOperationsResponseDto findOperationByUnitId(@PathVariable final String id) {
        return responseMapping(logbookRestClient.findOperationById(getInternalHttpContext(), id), LogbookOperationsResponseDto.class);
    }

    /**
     * Finds {@link LogbookLifecycle} by archive unit id.
     * @param id
     * @return
     */
    public LogbookLifeCycleResponseDto findUnitLifeCyclesByUnitId(@PathVariable final String id) {
        return responseMapping(logbookRestClient.findUnitLifeCyclesByUnitId(getInternalHttpContext(), id), LogbookLifeCycleResponseDto.class);
    }

    /**
     * Finds {@link LogbookLifecycle} by archive unit id.
     * @param id
     * @return
     */
    public LogbookLifeCycleResponseDto findObjectGroupLifeCyclesByUnitId(@PathVariable final String id) {
        return responseMapping(logbookRestClient.findObjectLifeCyclesByUnitId(getInternalHttpContext(), id), LogbookLifeCycleResponseDto.class);
    }

    /**
     * Find Operations using a JsonNode.
     *
     * @param select
     * @return
     * @throws VitamClientException
     */
    public LogbookOperationsResponseDto findOperations(@RequestBody final JsonNode select) throws VitamClientException {
        return responseMapping(logbookRestClient.findOperations(getInternalHttpContext(), select), LogbookOperationsResponseDto.class);
    }

    /**
     * Download an operation manifest
     *
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadManifest(final String id) {
        return logbookRestClient.downloadManifest(getInternalHttpContext(), id);
    }

    /**
     * Download an operation ATR
     *
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadAtr(final String id) {
        return logbookRestClient.downloadAtr(getInternalHttpContext(), id);
    }

    /**
     * Download an operation report
     *
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadReport(final String id, final String downloadType) {
        return logbookRestClient.downloadReport(getInternalHttpContext(), id, downloadType);
    }

    @Override
    protected BaseRestClient<InternalHttpContext> getClient() {
        return logbookRestClient;
    }

}
