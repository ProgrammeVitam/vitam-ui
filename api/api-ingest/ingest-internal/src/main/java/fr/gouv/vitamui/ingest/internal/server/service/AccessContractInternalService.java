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
package fr.gouv.vitamui.ingest.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.converter.AccessContractConverter;
import fr.gouv.vitamui.commons.api.domain.AccessContractDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccessContractInternalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessContractInternalService.class);

    private AccessContractService accessContractService;

    private ObjectMapper objectMapper;

    @Autowired
    public AccessContractInternalService(AccessContractService accessContractService, ObjectMapper objectMapper) {
        this.accessContractService = accessContractService;
        this.objectMapper = objectMapper;
    }

    public Optional<AccessContractDto> getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.debug("Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<AccessContractModel> requestResponse = accessContractService.findAccessContractById(
                vitamContext,
                identifier
            );
            final AccessContractResponseDto accessContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                AccessContractResponseDto.class
            );
            if (CollectionUtils.isEmpty(accessContractResponseDto.getResults())) {
                return Optional.empty();
            } else {
                return Optional.of(
                    AccessContractConverter.convertVitamToDto(accessContractResponseDto.getResults().get(0))
                );
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Access Contrat", e);
        }
    }
}
