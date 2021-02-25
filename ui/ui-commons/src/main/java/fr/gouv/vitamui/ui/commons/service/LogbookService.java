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
package fr.gouv.vitamui.ui.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.logbook.LogbookExternalRestClient;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookLifeCycleResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import lombok.Getter;
import lombok.Setter;

/**
 *  UI logbook service.
 *
 *
 */
@Getter
@Setter
public class LogbookService {

    private final LogbookExternalRestClient logbookRestClient;

    @Autowired
    public LogbookService(final LogbookExternalRestClient logbookRestClient) {
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
     * Find logbook unit life cycle by unit archive id.
     *
     * @param context
     * @param unitId
     * @return
     */
    public LogbookLifeCycleResponseDto findUnitLifeCyclesByUnitId(final ExternalHttpContext context, final String unitId) {

        return responseMapping(getLogbookRestClient().findUnitLifeCyclesByUnitId(context, unitId), LogbookLifeCycleResponseDto.class);
    }

    /**
     * Find logbook object life cycle by unit archive id.
     *
     * @param context
     * @param unitId
     * @return
     */
    public LogbookLifeCycleResponseDto findObjectLifeCyclesByUnitId(final ExternalHttpContext context, final String unitId) {

        return responseMapping(getLogbookRestClient().findObjectLifeCyclesByUnitId(context, unitId), LogbookLifeCycleResponseDto.class);
    }

    /**
     * Find logbook operation by operation id.
     *
     * @param context
     * @param id
     * @return
     */
    public LogbookOperationsResponseDto findOperationById(final ExternalHttpContext context, final String id) {
        return responseMapping(getLogbookRestClient().findOperationById(context, id), LogbookOperationsResponseDto.class);
    }

    /**
     * Find logbook operations by json select.
     *
     * @param context
     * @param id
     * @return
     */
    public LogbookOperationsResponseDto findOperations(final ExternalHttpContext context, final JsonNode select) {
        return responseMapping(getLogbookRestClient().findOperations(context, select), LogbookOperationsResponseDto.class);
    }

    /**
     * Download an operation manifest
     *
     * @param context
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadManifest(final ExternalHttpContext context, final String id) {
        return getLogbookRestClient().downloadManifest(context, id);
    }

    /**
     * Download an operation ATR
     *
     * @param context
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadAtr(final ExternalHttpContext context, final String id) {
        return getLogbookRestClient().downloadAtr(context, id);
    }

    /**
     * Download an operation report
     *
     * @param context
     * @param id
     * @return
     */
    public ResponseEntity<Resource> downloadReport(final ExternalHttpContext context, final String id, final String downloadType) {
        return getLogbookRestClient().downloadReport(context, id, downloadType);
    }

}
