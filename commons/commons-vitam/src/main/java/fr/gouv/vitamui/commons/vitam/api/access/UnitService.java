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
package fr.gouv.vitamui.commons.vitam.api.access;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamResponseHandler;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

public class UnitService {

    @SuppressWarnings("unused")
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UnitService.class);

    private final AccessExternalClient accessExternalClient;

    @Autowired
    public UnitService(final AccessExternalClient accessExternalClient) {
        this.accessExternalClient = accessExternalClient;
    }

    public RequestResponse<JsonNode> searchUnits(final JsonNode dslQuery, final VitamContext vitamContext)
        throws VitamClientException {
        final RequestResponse<JsonNode> result = accessExternalClient.selectUnits(vitamContext, dslQuery);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public RequestResponse<JsonNode> searchUnitsWithErrors(final Optional<String> unitId, final JsonNode dslQuery, final VitamContext vitamContext) 
        throws VitamClientException {
    	final RequestResponse<JsonNode> result;
    	if (unitId.isPresent()) {
            result = accessExternalClient.selectUnitbyId(vitamContext, dslQuery, unitId.get());
    	} else {
            result = accessExternalClient.selectUnits(vitamContext, dslQuery);
    	}
    	return result;
    }
    
    public RequestResponse<JsonNode> searchUnitsWithInheritedRules(final JsonNode dslQuery,
        final VitamContext vitamContext) throws VitamClientException {
        final RequestResponse<JsonNode> result =
            accessExternalClient.selectUnitsWithInheritedRules(vitamContext, dslQuery);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    public RequestResponse<JsonNode> findObjectMetadataById(final String unitId, final VitamContext vitamContext)
        throws VitamClientException {
        final SelectMultiQuery select = new SelectMultiQuery();
        final RequestResponse<JsonNode> result =
            accessExternalClient.selectObjectMetadatasByUnitId(vitamContext, select.getFinalSelectById(), unitId);
        VitamRestUtils.checkResponse(result);
        return result;
    }
    
    public RequestResponse<JsonNode> findObjectMetadataById(final String unitId, final JsonNode dslQuery, final VitamContext vitamContext) throws VitamClientException {
        final RequestResponse<JsonNode> result = accessExternalClient.selectObjectMetadatasByUnitId(vitamContext, dslQuery, unitId);
        VitamRestUtils.checkResponse(result);
        return result;
    }

    /**
     * Search object by id.
     *
     * @param unitId
     * @param objectId
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public Response getObjectStreamByUnitId(final String unitId, final String usage, final int version,
        final VitamContext vitamContext)
        throws VitamClientException {
        final Response response = accessExternalClient.getObjectStreamByUnitId(vitamContext, unitId, usage, version);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Search archive unit by id.
     *
     * @param unitId
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> findUnitById(final String unitId, final VitamContext vitamContext)
        throws VitamClientException {
        final RequestResponse<JsonNode> jsonResponse =
            accessExternalClient.selectUnitbyId(vitamContext, new SelectMultiQuery().getFinalSelectById(), unitId);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    /**
     * Search archive unit by id.
     *
     * @param unitId
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> findUnitWithInheritedRulesById(final String unitId,
        final VitamContext vitamContext) throws VitamClientException {

        final String validatedUnitId = Optional.ofNullable(unitId).filter(StringUtils::isNotEmpty)
            .orElseThrow(() -> new IllegalArgumentException("No unitId has been set."));
        final SelectMultiQuery select = new SelectMultiQuery();
        select.addProjection(JsonHandler.createObjectNode());
        try {
            select.setQuery(eq(VitamFieldsHelper.id(), validatedUnitId));
        } catch (final InvalidCreateOperationException exception) {
            throw new ApplicationServerException("An error occured while creating vitam query", exception);
        }

        final RequestResponse<JsonNode> jsonResponse =
            accessExternalClient.selectUnitsWithInheritedRules(vitamContext, select.getFinalSelect());
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    /**
     * Search archive unit by a list of identifiers.
     *
     * @param unitIds Identifiers of the units.
     * @param vitamContext Vitam context allowing to perform the request.
     * @return The Vitam response
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> findUnitWithInheritedRulesByIds(final List<String> unitIds,
        final VitamContext vitamContext) throws VitamClientException {

        final String[] validatedUnitIds =
            unitIds.stream().filter(StringUtils::isNotEmpty).distinct().toArray(String[]::new);
        if (validatedUnitIds.length < 1) {
            throw new IllegalArgumentException("No unitId has been set.");
        }
        final SelectMultiQuery select = new SelectMultiQuery();
        select.addProjection(JsonHandler.createObjectNode());
        try {
            select.setQuery(in(VitamFieldsHelper.id(), validatedUnitIds));
        } catch (final InvalidCreateOperationException exception) {
            throw new ApplicationServerException("An error occured while creating vitam query", exception);
        }

        final RequestResponse<JsonNode> jsonResponse =
            accessExternalClient.selectUnitsWithInheritedRules(vitamContext, select.getFinalSelect());
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    /**
     * update archive unit by id
     *
     * @param vitamContext
     * @param updateQuery
     * @param unitId
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> updateUnitById(final VitamContext vitamContext, final JsonNode updateQuery,
        final String unitId)
        throws VitamClientException {
        final RequestResponse<JsonNode> jsonResponse =
            accessExternalClient.updateUnitbyId(vitamContext, updateQuery, unitId);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }

    public RequestResponse<JsonNode> findByIdIn(final Collection<String> idList, final VitamContext vitamContext)
        throws VitamClientException {
        final SelectMultiQuery select = new SelectMultiQuery();
        try {
            final BooleanQuery orId = or();
            for (final String id : idList) {
                orId.add(eq(VitamFieldsHelper.id(), id));
            }
            select.addQueries(orId);
        } catch (final InvalidCreateOperationException e) {
            throw new ApplicationServerException("An error occured while creating vitam query", e);
        }
        final ObjectNode query = select.getFinalSelect();
        LOGGER.debug("findByIdIn: vitamContext {}, query {}", vitamContext, query);
        return searchUnits(select.getFinalSelect(), vitamContext);
    }

    public VitamUISearchResponseDto getByIdIn(final Collection<String> idList, final VitamContext vitamContext) {
        VitamUISearchResponseDto response;
        RequestResponse<JsonNode> jsonResponse;
        try {
            jsonResponse = findByIdIn(idList, vitamContext);
        } catch (final VitamClientException e) {
            throw new InternalServerException("Error while calling vitam", e);
        }
        try {
            response = VitamResponseHandler.extractSearchResponse(jsonResponse.toJsonNode());
        } catch (final IOException e) {
            throw new InternalServerException("Error while parsing vitam response", e);
        }
        return response;

    }

    public RequestResponse<JsonNode> startEliminationAction(final EliminationRequestBody eliminationRequest,
        final VitamContext vitamContext) {
        try {
            return accessExternalClient.startEliminationAction(vitamContext, eliminationRequest);
        } catch (final VitamClientException exception) {
            final String message = String.format("Error while calling vitam : %s", exception.getMessage());
            throw new InternalServerException(message, exception);
        }
    }

    public RequestResponse<JsonNode> massUpdateUnits(final VitamContext vitamContext, final JsonNode updateQuery)
        throws VitamClientException {
        final RequestResponse<JsonNode> jsonResponse = accessExternalClient.massUpdateUnits(vitamContext, updateQuery);
        VitamRestUtils.checkResponse(jsonResponse);
        return jsonResponse;
    }
}
