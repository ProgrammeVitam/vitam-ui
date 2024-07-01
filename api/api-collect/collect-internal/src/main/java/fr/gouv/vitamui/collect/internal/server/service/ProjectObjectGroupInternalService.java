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

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;

public class ProjectObjectGroupInternalService {

    private final CollectService collectService;
    private final ObjectMapper objectMapper;
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ProjectObjectGroupInternalService.class
    );

    private static final String RESULTS = "$results";

    public ProjectObjectGroupInternalService(CollectService collectService, ObjectMapper objectMapper) {
        this.collectService = collectService;
        this.objectMapper = objectMapper;
    }

    public Response downloadObjectFromUnit(String id, String usage, Integer version, final VitamContext vitamContext)
        throws VitamClientException {
        LOGGER.debug("Download Archive Unit Object with id {} , usage {} and version {}  ", id, usage, version);
        return collectService.getObjectStreamByUnitId(id, usage, version, vitamContext);
    }

    public ResultsDto findObjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.debug("Get Object Group");
            String resultStringValue = StringUtils.chop(
                collectService.getObjectById(vitamContext, id).toJsonNode().get(RESULTS).toString().substring(1)
            );
            return objectMapper.readValue(resultStringValue, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the object group {} ", e);
            throw new InternalServerException("Unable to find the ObjectGroup", e);
        }
    }
}
