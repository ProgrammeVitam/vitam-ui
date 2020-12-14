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

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Archive-Search Internal service communication with VITAM.
 */
@Service
public class ArchiveInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveInternalService.class);
    public static final String INGEST_ARCHIVE_TYPE = "INGEST";

    private final ObjectMapper objectMapper;

    final private UnitService unitService;



    @Autowired
    public ArchiveInternalService(final ObjectMapper objectMapper, final UnitService unitService) {
        this.unitService = unitService;
        this.objectMapper = objectMapper;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException, IOException {
        LOGGER.info("calling find archive units by criteria {} ", searchQuery.toString());
        List<String> archiveUnits = Arrays.asList(INGEST_ARCHIVE_TYPE);
        JsonNode response = searchUnits(mapRequestToDslQuery(archiveUnits, searchQuery), vitamContext);
        return new ArchiveUnitsDto(objectMapper.treeToValue(response, VitamUISearchResponseDto.class));
    }

    /**
     * Map search query to DSl Query Json node
     *
     * @param searchQuery
     * @return
     */
    private JsonNode mapRequestToDslQuery(List<String> archiveUnits, SearchCriteriaDto searchQuery) {

        Optional<String> orderBy = Optional.empty();
        Optional<DirectionDto> direction = Optional.empty();
        Map<String, List<String>> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (searchQuery != null && searchQuery.getCriteriaList() != null &&
                !searchQuery.getCriteriaList().isEmpty()) {
                searchQuery.getCriteriaList().stream()
                    .filter(criteria -> criteria.getValues() != null && !criteria.getValues().isEmpty())
                    .forEach(criteria -> vitamCriteria.put(criteria.getCriteria(), criteria.getValues()));
            }
            if (searchQuery.getSortingCriteria() != null) {
                direction = Optional.of(searchQuery.getSortingCriteria().getSorting());
                orderBy = Optional.of(searchQuery.getSortingCriteria().getCriteria());
            }
            query = VitamQueryHelper
                .createQueryDSL(archiveUnits, searchQuery.getNodes(), vitamCriteria, searchQuery.getPageNumber(),
                    searchQuery.getSize(), orderBy,
                    direction);
        } catch (InvalidParseOperationException ioe) {
            LOGGER.error("Unable to find archive units with pagination " + ioe.getMessage());
            throw new BadRequestException("Unable to find archive units with pagination", ioe);
        } catch (InvalidCreateOperationException e) {
            LOGGER.error("Can't parse criteria as Vitam query" + e.getMessage());
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        }
        return query;
    }

    public JsonNode searchUnits(final JsonNode dslQuery, final VitamContext vitamContext) throws VitamClientException {
        RequestResponse<JsonNode> response = unitService.searchUnits(dslQuery, vitamContext);
        return response.toJsonNode();
    }

    public Response downloadArchiveUnit(String id,Map<String, String> texte, final VitamContext vitamContext) throws VitamClientException {

        LOGGER.info("contrat acces  {}", vitamContext);
        LOGGER.info("Download UA with id {} ", id);
        try {
            return unitService.getObjectStreamByUnitId(id,texte.get("usage"),Integer.valueOf(texte.get("version")) ,vitamContext);
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to find the UA", e);
        }
    }

}
