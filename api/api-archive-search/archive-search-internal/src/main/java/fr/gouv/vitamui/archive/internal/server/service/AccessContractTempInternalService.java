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
package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.archives.search.common.dto.AccessContractDto;
import fr.gouv.vitamui.archives.search.common.dto.AccessContractResponseDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessContractTempInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessContractTempInternalService.class);

    private AccessContractService accessContractService;


    private ObjectMapper objectMapper;

    private AccessContractConverter converter;

    private LogbookService logbookService;

    @Autowired
    public AccessContractTempInternalService(AccessContractService accessContractService, ObjectMapper objectMapper, AccessContractConverter converter, LogbookService logbookService) {
        this.accessContractService = accessContractService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public AccessContractDto getOne(VitamContext vitamContext, String identifier) {
        try {
            RequestResponse<AccessContractModel> requestResponse = accessContractService.findAccessContractById(vitamContext, identifier);
            final AccessContractResponseDto accessContractResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);
            if (accessContractResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Access Contrat", e);
        }
    }

    public List<AccessContractDto> getAll(VitamContext vitamContext) {
        final RequestResponse<AccessContractModel> requestResponse;
        try {
            requestResponse = accessContractService
                    .findAccessContracts(vitamContext, new Select().getFinalSelect());
            final AccessContractResponseDto accessContractResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class);

            return converter.convertVitamsToDtos(accessContractResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
        	throw new InternalServerException("Unable to get Access Contrats", e);
        }
    }

}
