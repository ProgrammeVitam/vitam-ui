/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.archives.search.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.multiple.UpdateMultiQuery;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.archives.search.common.dto.OperationIdDto;
import fr.gouv.vitamui.archives.search.common.dto.UpdateArchiveUnitDto;
import fr.gouv.vitamui.archives.search.common.dto.converter.UpdateArchiveUnitDtoToUpdateMultiQueryConverter;
import fr.gouv.vitamui.archives.search.common.exception.ArchiveUnitUpdateException;
import fr.gouv.vitamui.archives.search.common.model.OperationId;
import fr.gouv.vitamui.archives.search.common.service.ArchiveUnitService;
import fr.gouv.vitamui.commons.api.converter.JsonPatchDtoToUpdateMultiQueryConverter;
import fr.gouv.vitamui.commons.api.converter.UpdateMultiQueriesToBulkCommandDto;
import fr.gouv.vitamui.commons.api.dtos.BulkCommandDto;
import fr.gouv.vitamui.commons.api.dtos.JsonPatchDto;
import fr.gouv.vitamui.commons.api.dtos.MultiJsonPatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_REQUEST_ID_HEADER;

@Service
public class ArchiveSearchUnitServiceImpl implements ArchiveUnitService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveSearchUnitServiceImpl.class);
    private final AccessExternalClient accessExternalClient;
    private final UpdateArchiveUnitDtoToUpdateMultiQueryConverter updateArchiveUnitDtoToUpdateMultiQueryConverter;
    private final ArchiveSearchExternalParametersService archiveSearchExternalParametersService;
    private final JsonPatchDtoToUpdateMultiQueryConverter jsonPatchDtoToUpdateMultiQueryConverter;
    private final UpdateMultiQueriesToBulkCommandDto updateMultiQueriesToBulkCommandDto;

    public ArchiveSearchUnitServiceImpl(
        final AccessExternalClient accessExternalClient,
        final UpdateArchiveUnitDtoToUpdateMultiQueryConverter updateArchiveUnitDtoToUpdateMultiQueryConverter,
        final ArchiveSearchExternalParametersService archiveSearchExternalParametersService,
        final JsonPatchDtoToUpdateMultiQueryConverter jsonPatchDtoToUpdateMultiQueryConverter,
        final UpdateMultiQueriesToBulkCommandDto updateMultiQueriesToBulkCommandDto
    ) {
        this.accessExternalClient = accessExternalClient;
        this.updateArchiveUnitDtoToUpdateMultiQueryConverter = updateArchiveUnitDtoToUpdateMultiQueryConverter;
        this.archiveSearchExternalParametersService = archiveSearchExternalParametersService;
        this.jsonPatchDtoToUpdateMultiQueryConverter = jsonPatchDtoToUpdateMultiQueryConverter;
        this.updateMultiQueriesToBulkCommandDto = updateMultiQueriesToBulkCommandDto;
    }

    @Override
    public OperationIdDto update(Set<UpdateArchiveUnitDto> updateArchiveUnitDtoSet) {
        final Set<UpdateMultiQuery> updateMultiQueries = updateArchiveUnitDtoSet
            .stream()
            .map(updateArchiveUnitDtoToUpdateMultiQueryConverter::convert)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (updateArchiveUnitDtoSet.size() != updateMultiQueries.size()) {
            throw new ArchiveUnitUpdateException("Fail to convert some archive unit updates payload to dsl queries");
        }
        return send(updateMultiQueries);
    }

    @Override
    public OperationIdDto update(JsonPatchDto jsonPatchDto) {
        final UpdateMultiQuery updateMultiQuery = jsonPatchDtoToUpdateMultiQueryConverter.convert(jsonPatchDto);
        if (updateMultiQuery == null) {
            throw new ArchiveUnitUpdateException("Fail to convert json patch payload to dsl query");
        }

        final Set<UpdateMultiQuery> updateMultiQueries = Set.of(updateMultiQuery);
        return send(updateMultiQueries);
    }

    @Override
    public OperationIdDto update(MultiJsonPatchDto multiJsonPatchDto) {
        final Set<UpdateMultiQuery> updateMultiQueries = multiJsonPatchDto
            .stream()
            .map(jsonPatchDtoToUpdateMultiQueryConverter::convert)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (multiJsonPatchDto.size() != updateMultiQueries.size()) {
            throw new ArchiveUnitUpdateException("Fail to convert some json patch payloads to dsl queries");
        }
        return send(updateMultiQueries);
    }

    private OperationIdDto send(Set<UpdateMultiQuery> updateMultiQueries) {
        final VitamContext context = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        final BulkCommandDto bulkCommandDto = updateMultiQueriesToBulkCommandDto.convert(updateMultiQueries);
        try {
            final RequestResponse<JsonNode> payload = accessExternalClient.bulkAtomicUpdateUnits(
                context,
                JsonHandler.toJsonNode(bulkCommandDto)
            );
            final OperationId operationId = new OperationId(payload.getHeaderString(X_REQUEST_ID_HEADER));
            final OperationIdDto operationIdDto = new OperationIdDto().setOperationId(operationId);
            log.info("Operation started: {}", operationIdDto);
            return operationIdDto;
        } catch (VitamClientException | InvalidParseOperationException e) {
            log.error("{}", e);
            throw new ArchiveUnitUpdateException(e);
        }
    }
}
